package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import com.bbangle.bbangle.member.repository.MemberDeliveryAddressRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.dto.request.OrderCreateRequest;
import com.bbangle.bbangle.order.customer.dto.request.OrderPreviewRequest;
import com.bbangle.bbangle.order.customer.dto.response.OrderCreateResponse;
import com.bbangle.bbangle.order.customer.dto.response.OrderPreviewResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerOrderService {

    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MemberRepository memberRepository;
    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 주문서 미리보기 — 스토어별 상품 그룹, 배송비, 합계를 계산해 반환합니다.
     */
    public OrderPreviewResponse getOrderPreview(Long memberId, OrderPreviewRequest request) {
        // 요청 상품 ID → 수량 맵
        Map<Long, Integer> quantityByProductId = buildQuantityMap(request.items()
            .stream()
            .map(item -> new ProductQuantityEntry(item.productId(), item.quantity()))
            .toList());

        List<Product> products = fetchAndValidateProducts(new ArrayList<>(quantityByProductId.keySet()));

        // 썸네일 조회 (Board ID별 첫 번째 이미지)
        List<Long> boardIds = products.stream()
            .map(p -> p.getBoard().getId())
            .distinct()
            .toList();
        Map<Long, String> thumbnailByBoardId = productImgRepository.findThumbnailsByBoardIdIn(boardIds)
            .stream()
            .collect(Collectors.toMap(pi -> pi.getBoard().getId(), ProductImg::getUrl, (a, b) -> a));

        // 스토어별로 상품 그룹핑
        Map<Long, List<Product>> productsByStoreId = products.stream()
            .collect(Collectors.groupingBy(p -> p.getBoard().getStore().getId()));

        // 기본 배송지 조회
        MemberDeliveryAddress defaultAddress = null;
        if (request.items() != null) {
            defaultAddress = memberDeliveryAddressRepository
                .findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
                .orElse(null);
        }

        List<OrderPreviewResponse.StoreOrderGroup> storeGroups = new ArrayList<>();
        int totalProductAmount = 0;
        int totalDeliveryFee = 0;

        for (Map.Entry<Long, List<Product>> entry : productsByStoreId.entrySet()) {
            List<Product> storeProducts = entry.getValue();
            Store store = storeProducts.get(0).getBoard().getStore();

            // Board별로 재그룹핑하여 배송비 계산
            Map<Long, List<Product>> productsByBoardId = storeProducts.stream()
                .collect(Collectors.groupingBy(p -> p.getBoard().getId()));

            List<OrderPreviewResponse.OrderItemInfo> itemInfos = new ArrayList<>();
            int storeSubtotal = 0;
            int storeDeliveryFee = 0;

            for (Map.Entry<Long, List<Product>> boardEntry : productsByBoardId.entrySet()) {
                List<Product> boardProducts = boardEntry.getValue();
                Board board = boardProducts.get(0).getBoard();
                int boardSubtotal = 0;

                for (Product product : boardProducts) {
                    int quantity = quantityByProductId.getOrDefault(product.getId(), 0);
                    int unitPrice = calcUnitPrice(product);
                    int totalPrice = unitPrice * quantity;
                    boardSubtotal += totalPrice;

                    String thumbnail = thumbnailByBoardId.get(board.getId());
                    itemInfos.add(new OrderPreviewResponse.OrderItemInfo(
                        product.getId(),
                        product.getTitle(),
                        thumbnail,
                        quantity,
                        unitPrice,
                        totalPrice
                    ));
                }

                storeDeliveryFee += calcBoardDeliveryFee(board, boardSubtotal);
                storeSubtotal += boardSubtotal;
            }

            totalProductAmount += storeSubtotal;
            totalDeliveryFee += storeDeliveryFee;

            storeGroups.add(new OrderPreviewResponse.StoreOrderGroup(
                store.getId(),
                store.getName(),
                itemInfos,
                storeSubtotal,
                storeDeliveryFee
            ));
        }

        // 스토어 ID 기준으로 정렬하여 일관된 응답 순서 보장
        storeGroups.sort(Comparator.comparing(OrderPreviewResponse.StoreOrderGroup::storeId));

        OrderPreviewResponse.DeliveryAddressInfo addressInfo = toDeliveryAddressInfo(defaultAddress);

        int totalPaymentAmount = totalProductAmount + totalDeliveryFee;
        return new OrderPreviewResponse(storeGroups, addressInfo, totalProductAmount, totalDeliveryFee, totalPaymentAmount);
    }

    /**
     * 주문 생성 — 스토어별로 Order 엔티티를 각각 생성합니다.
     */
    @Transactional
    public OrderCreateResponse createOrders(Long memberId, OrderCreateRequest request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        MemberDeliveryAddress address = resolveDeliveryAddress(memberId, request.deliveryAddressId());

        Map<Long, Integer> quantityByProductId = buildQuantityMap(request.items()
            .stream()
            .map(item -> new ProductQuantityEntry(item.productId(), item.quantity()))
            .toList());

        List<Product> products = fetchAndValidateProducts(new ArrayList<>(quantityByProductId.keySet()));

        // 스토어별 상품 그룹핑
        Map<Long, List<Product>> productsByStoreId = products.stream()
            .collect(Collectors.groupingBy(p -> p.getBoard().getStore().getId()));

        // Seller 조회 (Store ID → Seller)
        List<Long> storeIds = new ArrayList<>(productsByStoreId.keySet());
        Map<Long, Seller> sellerByStoreId = sellerRepository.findByStoreIdIn(storeIds)
            .stream()
            .collect(Collectors.toMap(s -> s.getStore().getId(), Function.identity()));

        Receiver receiver = Receiver.of(
            address.getRecipientName(),
            address.getPhone(),
            null,
            address.getAddress(),
            address.getAddressDetail(),
            address.getZipCode()
        );

        List<Order> createdOrders = new ArrayList<>();
        String representativeOrderNumber = null;

        for (Map.Entry<Long, List<Product>> entry : productsByStoreId.entrySet()) {
            Long storeId = entry.getKey();
            List<Product> storeProducts = entry.getValue();
            Store store = storeProducts.get(0).getBoard().getStore();

            Seller seller = sellerByStoreId.get(storeId);
            if (seller == null) {
                throw new BbangleException(BbangleErrorCode.STORE_NOT_FOUND);
            }

            // 1st pass: Board별 배송비 계산
            Map<Long, List<Product>> productsByBoardId = storeProducts.stream()
                .collect(Collectors.groupingBy(p -> p.getBoard().getId()));

            int storeDeliveryFee = 0;
            int storeProductAmount = 0;

            for (Map.Entry<Long, List<Product>> boardEntry : productsByBoardId.entrySet()) {
                List<Product> boardProducts = boardEntry.getValue();
                Board board = boardProducts.get(0).getBoard();
                int boardSubtotal = 0;

                for (Product product : boardProducts) {
                    int quantity = quantityByProductId.getOrDefault(product.getId(), 0);
                    int unitPrice = calcUnitPrice(product);
                    boardSubtotal += unitPrice * quantity;
                }

                storeDeliveryFee += calcBoardDeliveryFee(board, boardSubtotal);
                storeProductAmount += boardSubtotal;
            }

            String orderNumber = generateOrderNumber();

            // 2nd pass: Order 엔티티 생성 (올바른 배송비/총액 포함)
            Order order = Order.builder()
                .orderNumber(orderNumber)
                .orderDate(LocalDateTime.now())
                .buyerName(member.getName())
                .buyerPhone(member.getPhone())
                .buyerSubPhone(null)
                .deliveryFee(storeDeliveryFee)
                .totalAmount(storeProductAmount + storeDeliveryFee)
                .member(member)
                .seller(seller)
                .build();

            Sender sender = Sender.of(
                store.getName(),
                store.getPhoneNumberVO().getPhoneNumber(),
                store.getOriginAddressLine(),
                store.getOriginAddressDetail(),
                ""
            );

            // OrderItem + OrderDelivery 생성 후 Order에 연결
            for (Product product : storeProducts) {
                int quantity = quantityByProductId.getOrDefault(product.getId(), 0);
                int unitPrice = calcUnitPrice(product);
                int totalPrice = unitPrice * quantity;

                OrderItem orderItem = OrderItem.builder()
                    .quantity(quantity)
                    .productPrice(product.getPrice())
                    .unitPrice(unitPrice)
                    .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                    .orderDeliveryStatus(OrderDeliveryStatus.PREPARING)
                    .totalPrice(totalPrice)
                    .order(order)
                    .product(product)
                    .build();

                OrderDelivery orderDelivery = OrderDelivery.create(
                    sender,
                    receiver,
                    null,
                    OrderDeliveryStatus.PREPARING,
                    orderItem
                );
                orderItem.addOrderDelivery(orderDelivery);
                order.addOrderItem(orderItem);
            }

            Order savedOrder = orderRepository.save(order);

            Payment payment = Payment.create(
                savedOrder,
                PaymentStatus.PENDING,
                request.paymentMethod(),
                null
            );
            paymentRepository.save(payment);

            createdOrders.add(savedOrder);

            if (representativeOrderNumber == null) {
                representativeOrderNumber = orderNumber;
            }
        }

        List<Long> orderIds = createdOrders.stream()
            .map(Order::getId)
            .toList();

        return new OrderCreateResponse(orderIds, representativeOrderNumber);
    }

    /**
     * Board 단위 배송비를 계산합니다. 소계가 무료배송 조건 이상이면 0을 반환합니다.
     */
    private int calcBoardDeliveryFee(Board board, int boardSubtotal) {
        Integer boardDeliveryFee = board.getDeliveryFee();
        if (boardDeliveryFee == null) {
            return 0;
        }
        Integer freeShippingCondition = board.getFreeShippingConditions();
        if (freeShippingCondition != null && boardSubtotal >= freeShippingCondition) {
            return 0;
        }
        return boardDeliveryFee;
    }

    /**
     * 할인 유형에 따라 단가를 계산합니다.
     * Product.price = Board.price + 추가금액(plusPrice)
     */
    private int calcUnitPrice(Product product) {
        Board board = product.getBoard();
        int productPrice = product.getPrice();
        DiscountType discountType = board.getDiscountType();

        if (discountType == null) {
            return productPrice;
        }

        return switch (discountType) {
            case RATE -> {
                Integer rate = board.getDiscountRate();
                yield rate == null ? productPrice : productPrice * (100 - rate) / 100;
            }
            case AMOUNT -> Math.max(0, productPrice - board.getDiscountValue());
        };
    }

    /**
     * 주문번호 생성: "ORD" + yyyyMMddHHmmss + UUID 앞 8자리 (총 25자, VARCHAR(30) 이내)
     */
    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(ORDER_DATE_FORMAT);
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD" + datePart + uuidPart;
    }

    /**
     * 요청 상품을 조회하고 누락 상품이 있으면 예외를 던집니다.
     */
    private List<Product> fetchAndValidateProducts(List<Long> productIds) {
        List<Product> products = productRepository.findAllWithBoardAndStoreByIdIn(productIds);
        if (products.size() != new HashSet<>(productIds).size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_PRODUCT_EMPTY);
        }
        return products;
    }

    /**
     * 배송지 ID가 주어지면 해당 배송지를, 없으면 기본 배송지를 반환합니다.
     */
    private MemberDeliveryAddress resolveDeliveryAddress(Long memberId, Long deliveryAddressId) {
        if (deliveryAddressId != null) {
            return memberDeliveryAddressRepository.findById(deliveryAddressId)
                .filter(a -> a.getMember().getId().equals(memberId) && !a.isDeleted())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
        }
        return memberDeliveryAddressRepository
            .findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
    }

    /**
     * 상품 ID → 수량 맵 생성 (동일 상품 중복 요청 시 수량 합산)
     */
    private Map<Long, Integer> buildQuantityMap(List<ProductQuantityEntry> items) {
        return items.stream()
            .collect(Collectors.toMap(
                ProductQuantityEntry::productId,
                ProductQuantityEntry::quantity,
                Integer::sum
            ));
    }

    private OrderPreviewResponse.DeliveryAddressInfo toDeliveryAddressInfo(MemberDeliveryAddress address) {
        if (address == null) {
            return null;
        }
        return new OrderPreviewResponse.DeliveryAddressInfo(
            address.getId(),
            address.getAddressName(),
            address.getRecipientName(),
            address.getPhone(),
            address.getAddress(),
            address.getAddressDetail(),
            address.getZipCode(),
            address.isDefault()
        );
    }

    private record ProductQuantityEntry(Long productId, Integer quantity) {}

}
