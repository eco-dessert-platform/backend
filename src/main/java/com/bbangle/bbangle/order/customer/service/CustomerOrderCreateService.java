package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse.StoreOrderResponse;
import com.bbangle.bbangle.order.customer.service.OrderAmountCalculator.OrderLine;
import com.bbangle.bbangle.order.customer.service.OrderAmountCalculator.StoreAmount;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.OptionOrder;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.PaymentAmount;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.ProductOrder;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.StoreOrder;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderNumberGenerator;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소비자 주문 생성.
 *
 * <p>요청은 스토어 &gt; 상품(게시글) &gt; 옵션 3단 구조로 들어오며, 스토어 1건당 주문 1건을 만들어
 * 결제 1건으로 묶는다.
 *
 * <p>클라이언트가 보낸 그룹핑과 금액은 신뢰하지 않고 <b>대조만</b> 한다.
 * 저장되는 값은 전부 {@link OrderAmountCalculator} 가 다시 계산한 결과다.
 *
 * <p>이 시점의 주문은 결제 전이므로 {@code PAYMENT_PENDING} 으로 저장되어 목록에 노출되지 않는다.
 * 재고는 검증만 하고 차감은 결제 승인 시점에 한다.
 */
@Service
@RequiredArgsConstructor
public class CustomerOrderCreateService {

    private static final String ORDER_CREATE_REDIS_KEY_PREFIX = "customer:order:create:";
    private static final Duration ORDER_CREATE_REDIS_TTL = Duration.ofMinutes(5);

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    /** 요청 한 줄. 클라이언트가 주장한 소속·단가와 실제 조회된 옵션을 함께 들고 있다. */
    private record RequestedLine(Long storeId, Long productId, OptionOrder request, Product option) {

        OrderLine toOrderLine() {
            return new OrderLine(option, request.quantity());
        }
    }

    @Transactional
    public CreateOrderResponse create(CreateOrderCommand command) {
        // TODO: privacyAgreed - 동의 이력 저장 미구현. 동의 이력 테이블 설계 후 반영한다.
        // TODO: shippingAddress.saveAsDefault - 배송지 도메인 미구현. 배송지 CRUD 구현 후 반영한다.

        preventDuplicateRequest(command.transactionId(), command.memberId());

        Member member = findMember(command.memberId());

        Map<Long, Product> optionMap = loadOptions(command);
        List<RequestedLine> lines = flatten(command, optionMap);

        validateStructure(lines);
        validateOrderable(lines);
        validateOptionPrices(lines);

        Map<Long, List<OrderLine>> linesByStoreId = groupByStoreId(lines);
        Map<Long, Seller> sellerByStoreId = findSellers(linesByStoreId.keySet());
        Map<Long, StoreAmount> amountByStoreId = calculateAmounts(linesByStoreId);

        validateDeliveryFees(command, amountByStoreId);
        validatePaymentAmount(command, amountByStoreId);

        long totalAmount = sum(amountByStoreId, StoreAmount::totalAmount);
        Payment payment = paymentRepository.save(Payment.pending(
            OrderNumberGenerator.paymentNumber(),
            member,
            Math.toIntExact(totalAmount),
            command.paymentMethod()));

        List<StoreOrderResponse> storeOrders = new ArrayList<>();
        linesByStoreId.forEach((storeId, storeLines) -> {
            StoreAmount amount = amountByStoreId.get(storeId);
            Order order = persistStoreOrder(
                member, sellerByStoreId.get(storeId), payment, command, storeLines, amount);

            Store store = storeLines.get(0).board().getStore();
            storeOrders.add(new StoreOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                storeId,
                store.getName(),
                amount.productAmount(),
                amount.discountAmount(),
                amount.deliveryFee(),
                amount.totalAmount()));
        });

        return new CreateOrderResponse(
            payment.getPaymentNumber(),
            buildOrderName(lines),
            totalAmount,
            storeOrders);
    }

    /**
     * 같은 거래 ID 로 들어온 재요청을 막는다. (따닥·재시도)
     *
     * <p>선점한 키는 트랜잭션이 실패해도 TTL 동안 남으므로, 실패 후 재시도는 새 거래 ID 로 보내야 한다.
     */
    private void preventDuplicateRequest(String transactionId, Long memberId) {
        String redisKey = ORDER_CREATE_REDIS_KEY_PREFIX + transactionId;
        boolean isNew = Boolean.TRUE.equals(
            redisTemplate.opsForValue()
                .setIfAbsent(redisKey, String.valueOf(memberId), ORDER_CREATE_REDIS_TTL)
        );
        if (!isNew) {
            throw new BbangleException(BbangleErrorCode.ORDER_DUPLICATED_REQUEST);
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND));
    }

    /** 요청 전체에서 옵션 중복을 막고 한 번에 조회한다. */
    private Map<Long, Product> loadOptions(CreateOrderCommand command) {
        List<Long> optionIds = command.allOptions().stream()
            .map(OptionOrder::optionId)
            .toList();

        if (optionIds.size() != Set.copyOf(optionIds).size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_DUPLICATED_OPTION);
        }

        Map<Long, Product> optionMap = productRepository.findAllWithBoardAndStoreByIdIn(optionIds)
            .stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        if (optionMap.size() != optionIds.size()) {
            throw new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND);
        }
        return optionMap;
    }

    private List<RequestedLine> flatten(CreateOrderCommand command, Map<Long, Product> optionMap) {
        List<RequestedLine> lines = new ArrayList<>();
        for (StoreOrder store : command.stores()) {
            for (ProductOrder product : store.products()) {
                for (OptionOrder option : product.options()) {
                    lines.add(new RequestedLine(
                        store.storeId(), product.productId(), option, optionMap.get(option.optionId())));
                }
            }
        }
        return lines;
    }

    /** 클라이언트가 주장한 상품·스토어 소속이 실제와 같은지 대조한다. */
    private void validateStructure(List<RequestedLine> lines) {
        for (RequestedLine line : lines) {
            if (!line.option().getBoard().getId().equals(line.productId())) {
                throw new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND);
            }
            if (!line.option().getBoard().getStore().getId().equals(line.storeId())) {
                throw new BbangleException(BbangleErrorCode.ORDER_INVALID_STORE);
            }
        }
    }

    /** 재고는 검증만 한다. 차감은 결제 승인 시점에 비관적 락과 함께 수행한다. */
    private void validateOrderable(List<RequestedLine> lines) {
        for (RequestedLine line : lines) {
            if (line.option().getBoard().getSaleStatus() != SaleStatus.ON_SALE) {
                throw new BbangleException(BbangleErrorCode.ORDER_PRODUCT_NOT_ON_SALE);
            }
            line.option().validateStock(line.request().quantity());
        }
    }

    /** 화면에 노출된 단가가 현재 판매가와 같은지 대조한다. (판매자가 가격을 바꾼 경우 감지) */
    private void validateOptionPrices(List<RequestedLine> lines) {
        for (RequestedLine line : lines) {
            if (line.toOrderLine().unitPrice() != line.request().price()) {
                throw new BbangleException(BbangleErrorCode.ORDER_PRICE_MISMATCH);
            }
        }
    }

    /** 요청에 담긴 스토어 순서를 유지한 채 묶는다. */
    private Map<Long, List<OrderLine>> groupByStoreId(List<RequestedLine> lines) {
        return lines.stream()
            .collect(Collectors.groupingBy(
                RequestedLine::storeId,
                LinkedHashMap::new,
                Collectors.mapping(RequestedLine::toOrderLine, Collectors.toList())));
    }

    private Map<Long, Seller> findSellers(Set<Long> storeIds) {
        Map<Long, Seller> sellerByStoreId = sellerRepository.findByStoreIdIn(List.copyOf(storeIds))
            .stream()
            .collect(Collectors.toMap(seller -> seller.getStore().getId(), Function.identity()));

        if (sellerByStoreId.size() != storeIds.size()) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }
        return sellerByStoreId;
    }

    private Map<Long, StoreAmount> calculateAmounts(Map<Long, List<OrderLine>> linesByStoreId) {
        Map<Long, StoreAmount> amounts = new LinkedHashMap<>();
        linesByStoreId.forEach((storeId, storeLines) ->
            amounts.put(storeId, OrderAmountCalculator.calculate(storeLines)));
        return amounts;
    }

    private void validateDeliveryFees(CreateOrderCommand command, Map<Long, StoreAmount> amounts) {
        for (StoreOrder store : command.stores()) {
            StoreAmount amount = amounts.get(store.storeId());
            if (amount.deliveryFee() != store.deliveryFee()) {
                throw new BbangleException(BbangleErrorCode.ORDER_DELIVERY_FEE_MISMATCH);
            }
        }
    }

    private void validatePaymentAmount(CreateOrderCommand command, Map<Long, StoreAmount> amounts) {
        PaymentAmount requested = command.paymentAmount();

        if (sum(amounts, StoreAmount::productAmount) != requested.productAmount()
            || sum(amounts, StoreAmount::discountAmount) != requested.discountAmount()
            || sum(amounts, StoreAmount::deliveryFee) != requested.deliveryFee()
            || sum(amounts, StoreAmount::totalAmount) != requested.totalAmount()) {
            throw new BbangleException(BbangleErrorCode.ORDER_AMOUNT_MISMATCH);
        }
    }

    private long sum(Map<Long, StoreAmount> amounts, ToLongFunction<StoreAmount> field) {
        return amounts.values().stream().mapToLong(field).sum();
    }

    private Order persistStoreOrder(
        Member member,
        Seller seller,
        Payment payment,
        CreateOrderCommand command,
        List<OrderLine> lines,
        StoreAmount amount
    ) {
        Order order = orderRepository.save(Order.create(
            OrderNumberGenerator.orderNumber(),
            member,
            seller,
            payment,
            command.orderer().name(),
            command.orderer().phone(),
            command.orderer().email(),
            Math.toIntExact(amount.deliveryFee()),
            Math.toIntExact(amount.totalAmount())));

        Sender sender = toSender(lines.get(0).board().getStore());
        Receiver receiver = toReceiver(command.shippingAddress());

        for (OrderLine line : lines) {
            OrderItem orderItem = OrderItem.createPending(
                line.option(),
                line.quantity(),
                line.productPrice(),
                line.unitPrice(),
                Math.toIntExact(line.totalPrice()));

            order.addOrderItem(orderItem);
            orderItemRepository.save(orderItem);

            orderDeliveryRepository.save(OrderDelivery.create(
                sender,
                receiver,
                Shipping.ofOrder(command.shippingAddress().deliveryMemo()),
                OrderDeliveryStatus.NONE,
                orderItem));

            orderItemHistoryRepository.save(OrderItemHistory.create(orderItem));
        }

        return order;
    }

    private Sender toSender(Store store) {
        return Sender.of(
            store.getName(),
            store.getPhoneNumberVO() != null ? store.getPhoneNumberVO().getPhoneNumber() : null,
            store.getOriginAddressLine(),
            store.getOriginAddressDetail(),
            null);
    }

    private Receiver toReceiver(CreateOrderCommand.ShippingAddress address) {
        return Receiver.of(
            address.recipientName(),
            address.recipientPhone(),
            null,
            address.address(),
            address.addressDetail(),
            address.zipCode());
    }

    /** PG 결제창에 표시할 주문명. */
    private String buildOrderName(List<RequestedLine> lines) {
        String firstTitle = lines.get(0).option().getBoard().getTitle();
        if (lines.size() == 1) {
            return firstTitle;
        }
        return "%s 외 %d건".formatted(firstTitle, lines.size() - 1);
    }
}
