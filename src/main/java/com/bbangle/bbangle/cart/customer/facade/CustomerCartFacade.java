package com.bbangle.bbangle.cart.customer.facade;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductImgService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest.UpdateCartOptionRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse.AvailableOptionDTO;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse.CartItemDTO;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse.CartItemDTO.PriceDTO;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse.CartStoreDTO;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse.SelectedOptionDTO;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.UpdateCartOptionResponse;
import com.bbangle.bbangle.cart.customer.service.CustomerCartItemService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartOptionService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartService;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerCartFacade {

    private final CustomerCartService customerCartService;
    private final CustomerCartItemService customerCartItemService;
    private final CustomerCartOptionService customerCartOptionService;
    private final MemberService memberService;
    private final BoardService boardService;
    private final ProductService productService;
    private final ProductImgService productImgService;

    @Transactional
    public void addCartItem(Long memberId, CartRequest.AddCartRequest request) {

        // 1. 프론트에서 온 요청에 상품 옵션 Id가 중복되는지 검증
        validateDuplicateOptions(request.options());

        Member member = memberService.findById(memberId);
        Cart cart = customerCartService.findCartByMember(member);
        Board board = boardService.getBoard(request.boardId());

        // 2. 장바구니에 담긴 상품 조회 -> 없으면 생성
        CartItem cartItem = customerCartItemService.findCartItem(cart, board)
            .orElseGet(() -> customerCartItemService.createCartItem(cart, board));

        // 3. 장바구니에 담긴 현재 상품의 옵션 목록 조회
        Map<Long, CartOption> optionMap = getCartOptionMap(cartItem);
        // 4. 요청에 포함된 상품 옵션들을 한번에 조회
        Map<Long, Product> productMap = getProductMap(request.options());

        for (CartRequest.AddCartRequest.SelectedOptions selectedOption : request.options()) {

            // 5. 요청에 포함된 상품 옵션이 실제 해당 상품의 옵션인지 검증
            Product product = productMap.get(selectedOption.productId());
            product.validateBelongsTo(board);

            // 6. 해당 상품 옵션이 이미 장바구니에 존재하는지 확인
            CartOption existingOption = optionMap.get(product.getId());
            // 7. 장바구니에 없을 경우 재고를 확인한 후 장바구니에 해당 상품 옵션 추가
            if (existingOption == null) {
                product.validateStock(selectedOption.quantity());
                customerCartOptionService.createCartOption(cartItem, product, selectedOption.quantity());
                continue;
            }

            // 8. 장바구니에 존재할 경우 재고를 확인한 후 장바구니의 해당 상품 옵션 수량 증가
            int newQuantity = existingOption.getQuantity() + selectedOption.quantity();
            product.validateStock(newQuantity);
            customerCartOptionService.updateQuantity(existingOption, newQuantity);
        }
    }

    // 요청에서 동일한 상품 옵션 Id가 들어왔는지 검증
    private void validateDuplicateOptions(List<CartRequest.AddCartRequest.SelectedOptions> options) {
        Set<Long> productIds = options.stream()
            .map(CartRequest.AddCartRequest.SelectedOptions::productId)
            .collect(Collectors.toSet());

        if (productIds.size() != options.size()) {
            throw new BbangleException(BbangleErrorCode.DUPLICATED_PRODUCT_OPTION);
        }
    }

    /**
     * 요청에 포함된 상품 옵션들을 조회하여 Map으로 변환
     */
    private Map<Long, Product> getProductMap(List<CartRequest.AddCartRequest.SelectedOptions> options) {
        Set<Long> productIds = options.stream()
            .map(CartRequest.AddCartRequest.SelectedOptions::productId)
            .collect(Collectors.toSet());
        List<Product> products = productService.findAllByIds(new ArrayList<>(productIds));

        if (products.size() != productIds.size()) {
            throw new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND);
        }

        return products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    /**
     * 장바구니에 담긴 상품의 옵션 목록을 Map으로 변환
     */
    private Map<Long, CartOption> getCartOptionMap(CartItem cartItem) {
        return customerCartOptionService.findAllByCartItem(cartItem)
            .stream()
            .collect(Collectors.toMap(
                option -> option.getOption().getId(),
                Function.identity()
            ));
    }

    @Transactional
    public void deleteCartOptions(Long memberId, CartRequest.DeleteCartOptionsRequest request) {
        List<Long> optionIds = request.cartOptionIds();

        List<CartItem> cartItems = customerCartItemService.findAllWithOptionsByMemberIdAndOptionIds(memberId,
            optionIds);
        validateAllOptionsFound(cartItems, optionIds);

        Set<Long> targetOptionIds = new HashSet<>(optionIds);
        cartItems.forEach(cartItem -> cartItem.removeOptions(targetOptionIds));

        cartItems.stream()
            .filter(CartItem::hasNoOptions)
            .forEach(customerCartItemService::delete);
    }

    private void validateAllOptionsFound(List<CartItem> cartItems, List<Long> requestedIds) {
        Set<Long> requestedSet = new HashSet<>(requestedIds);
        long foundCount = cartItems.stream()
            .flatMap(ci -> ci.getOptions().stream())
            .map(CartOption::getId)
            .filter(requestedSet::contains)
            .count();
        if (foundCount != requestedIds.size()) {
            throw new BbangleException(BbangleErrorCode.NOT_FOUND_CART_OPTION);
        }
    }

    @Transactional(readOnly = true)
    public CartListResponse getCart(Long memberId) {

        // 1. 장바구니 상품 조회
        Member member = memberService.findById(memberId);
        List<CartItem> cartItems = customerCartItemService.findAllByMember(member);

        if (cartItems.isEmpty()) {
            return CartListResponse.builder()
                .carts(List.of())
                .build();
        }

        // 2. 조회에 필요한 ID 추출
        List<Long> boardIds = cartItems.stream().map(cartItem -> cartItem.getItem().getId()).distinct().toList();
        List<Long> cartItemIds = cartItems.stream().map(CartItem::getId).toList();

        // 3. 추가 데이터 조회
        Map<Long, String> thumbnailMap = getThumbnailsMap(boardIds);
        Map<Long, List<Product>> productMap = getProductsMap(boardIds);
        Map<Long, List<CartOption>> cartOptionMap = getCartOptionsMap(cartItemIds);

        // 4. Store 정보와 Item 그룹핑
        List<CartStoreDTO> cartStores = getCartStores(cartItems, thumbnailMap, productMap, cartOptionMap);

        // 5. 응답 생성
        return CartListResponse.builder()
            .carts(cartStores)
            .build();
    }

    private Map<Long, String> getThumbnailsMap(List<Long> boardIds) {
        return productImgService.findAllByBoardIds(boardIds)
            .stream()
            .collect(Collectors.toMap(
                productImg -> productImg.getBoard().getId(),
                ProductImg::getUrl,
                (existing, ignored) -> existing     // 동일한 Board의 이미지가 중복 조회되더라도 첫번째 이미지를 사용
            ));
    }

    private Map<Long, List<Product>> getProductsMap(List<Long> boardIds) {
        return productService.findAllByBoardIds(boardIds)
            .stream()
            .collect(Collectors.groupingBy(
                product -> product.getBoard().getId()
            ));
    }

    private Map<Long, List<CartOption>> getCartOptionsMap(List<Long> cartItemIds) {
        return customerCartOptionService.findAllByCartItemIds(cartItemIds)
            .stream()
            .collect(Collectors.groupingBy(
                cartOption -> cartOption.getCartItem().getId()
            ));
    }

    private List<CartStoreDTO> getCartStores(
        List<CartItem> cartItems,
        Map<Long, String> thumbnailMap,
        Map<Long, List<Product>> productMap,
        Map<Long, List<CartOption>> cartOptionMap
    ) {
        Map<Long, Store> storeMap = new LinkedHashMap<>();
        Map<Long, List<CartItemDTO>> cartItemMap = new LinkedHashMap<>();

        for (CartItem cartItem : cartItems) {
            Board board = cartItem.getItem();
            Store store = board.getStore();
            storeMap.putIfAbsent(store.getId(), store);

            List<AvailableOptionDTO> availableOptions = productMap.getOrDefault(board.getId(), List.of())
                .stream()
                .map(product -> AvailableOptionDTO.builder()
                    .optionId(product.getId())
                    .optionName(product.getTitle())
                    .addedPrice(product.getPrice())
                    .stock(product.getStock())
                    .build()
                )
                .toList();

            List<SelectedOptionDTO> selectedOptions = cartOptionMap.getOrDefault(cartItem.getId(), List.of())
                .stream()
                .map(cartOption -> SelectedOptionDTO.builder()
                    .cartOptionId(cartOption.getId())
                    .optionId(cartOption.getOption().getId())
                    .optionName(cartOption.getOption().getTitle())
                    .addedPrice(cartOption.getOption().getPrice())
                    .quantity(cartOption.getQuantity())
                    .build()
                )
                .toList();

            CartItemDTO cartItemDTO = CartItemDTO.builder()
                .cartItemId(cartItem.getId())
                .itemId(board.getId())
                .itemName(board.getTitle())
                .itemImg(thumbnailMap.get(board.getId()))
                .status(board.getSaleStatus())
                .price(
                    PriceDTO.builder()
                        .base(board.getPrice())
                        .discountType(board.getDiscountType())
                        .discountValue(board.getDiscountValue())
                        .deliveryFee(board.getDeliveryFee())
                        .build()
                )
                .availableOptions(availableOptions)
                .selectedOptions(selectedOptions)
                .build();

            cartItemMap.computeIfAbsent(
                store.getId(),
                id -> new ArrayList<>()
            ).add(cartItemDTO);
        }

        return storeMap.values().stream()
            .map(store -> CartStoreDTO.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .storeProfile(store.getProfile())
                .items(cartItemMap.getOrDefault(store.getId(), List.of()))
                .build()
            ).toList();
    }

    // TODO : Test
    @Transactional
    public UpdateCartOptionResponse updateQuantity(Long memberId, Long cartOptionId, UpdateCartOptionRequest request) {
        CartOption cartOption = customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId);
        Product option = cartOption.getOption();

        option.validateStock(request.quantity());
        customerCartOptionService.updateQuantity(cartOption, request.quantity());

        return UpdateCartOptionResponse.builder()
            .cartOptionId(cartOption.getId())
            .optionId(option.getId())
            .optionName(option.getTitle())
            .quantity(cartOption.getQuantity())
            .build();
    }
}
