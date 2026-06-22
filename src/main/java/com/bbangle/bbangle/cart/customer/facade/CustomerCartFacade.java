package com.bbangle.bbangle.cart.customer.facade;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
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
import java.util.ArrayList;
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

    @Transactional
    public void deleteCartOptions(Long memberId, CartRequest.DeleteCartOptionsRequest request) {
        Member member = memberService.findById(memberId);
        Cart cart = customerCartService.findCartByMember(member);

        List<CartOption> cartOptions = customerCartOptionService.findAllByIdsWithCart(request.cartOptionIds());
        validateAllOptionsFound(cartOptions, request.cartOptionIds());

        Set<CartItem> affectedCartItems = cartOptions.stream()
            .map(option -> {
                validateCartOptionOwnership(option, cart);
                return option.getCartItem();
            })
            .collect(Collectors.toSet());

        customerCartOptionService.deleteAll(cartOptions);

        for (CartItem cartItem : affectedCartItems) {
            if (!customerCartOptionService.existsByCartItem(cartItem)) {
                customerCartItemService.delete(cartItem);
            }
        }
    }

    private void validateAllOptionsFound(List<CartOption> cartOptions, List<Long> requestedIds) {
        if (cartOptions.size() != requestedIds.size()) {
            throw new BbangleException(BbangleErrorCode.NOT_FOUND_CART_OPTION);
        }
    }

    private void validateCartOptionOwnership(CartOption option, Cart cart) {
        if (!option.getCartItem().getCart().getId().equals(cart.getId())) {
            throw new BbangleException(BbangleErrorCode.CART_OPTION_ACCESS_DENIED);
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
}
