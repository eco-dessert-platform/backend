package com.bbangle.bbangle.board.seller.controller.mapper;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardContentDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardDetailDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardImgDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.DeliveryDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductInfoNoticeDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductOptionDTO;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SellerBoardDetailMapper {

    /**
     * 별도로 조회된 Board / ProductImg 목록 / Product 목록을 하나의 상세 응답 DTO로 조립한다.
     * 컬렉션 두 개(products, productImgs)를 각각 독립적으로 조회했기 때문에
     * 여기서만 조합하고, 나머지 단건 매핑은 개별 메서드에 위임한다.
     */
    default SellerBoardDetailResponse toResponse(
        Board board,
        List<ProductImg> productImgs,
        List<Product> products
    ) {
        return SellerBoardDetailResponse.builder()
            .boardDetailDTO(toBoardDetailDTO(board))
            .deliveryDTO(toDeliveryDTO(board))
            .boardImgDTO(toBoardImgDTO(productImgs))
            .options(products.stream().map(this::toProductOptionDTO).toList())
            .boardContent(toBoardContentDTO(board.getBoardDetail()))
            .productInfoNotice(toProductInfoNoticeDTO(board.getProductInfoNotice()))
            .build();
    }

    // ===== 상품 기본 정보 =====

    @Mapping(source = "id", target = "boardId")
    @Mapping(source = "title", target = "name")
    @Mapping(target = "price", expression = "java(toPriceDTO(board))")
    BoardDetailDTO toBoardDetailDTO(Board board);

    /**
     * discountType에 따라 표시할 할인값이 달라지므로 Board의 도메인 메서드에 위임한다.
     */
    default BoardDetailDTO.PriceDTO toPriceDTO(Board board) {
        return BoardDetailDTO.PriceDTO.builder()
            .base(board.getPrice())
            .discountType(board.getDiscountType())
            .discountValue(board.getDisplayDiscountValue())
            .build();
    }

    // ===== 배송 정보 (필드명이 동일해 자동 매핑) =====
    DeliveryDTO toDeliveryDTO(Board board);

    // ===== 이미지 정보 =====

    /**
     * imgOrder = 0(썸네일) 여부로 대표 이미지와 추가 이미지를 분리한다.
     * 썸네일이 없으면 데이터 정합성 오류이므로 예외를 던진다(Board.getThumbnail()과 동일한 정책).
     */
    default BoardImgDTO toBoardImgDTO(List<ProductImg> productImgs) {
        String thumbnailUrl = productImgs.stream()
            .filter(ProductImg::isThumbnail)
            .findFirst()
            .map(ProductImg::getUrl)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_WITH_IMAGE_NOTFOUND));

        List<String> additionalImgs = productImgs.stream()
            .filter(img -> !img.isThumbnail())
            .map(ProductImg::getUrl)
            .toList();

        return BoardImgDTO.builder()
            .thumbnailImg(thumbnailUrl)
            .additionalImgs(additionalImgs)
            .build();
    }

    // ===== 상품 옵션 정보 =====

    @Mapping(source = "id", target = "optionId")
    @Mapping(source = "title", target = "optionName")
    @Mapping(source = "price", target = "addedPrice")
    @Mapping(target = "tags", expression = "java(product.getTagEnums())")
    @Mapping(target = "week", expression = "java(toWeekDTO(product))")
    ProductOptionDTO toProductOptionDTO(Product product);

    default ProductOptionDTO.WeekDTO toWeekDTO(Product product) {
        return ProductOptionDTO.WeekDTO.builder()
            .monday(product.isMonday())
            .tuesday(product.isTuesday())
            .wednesday(product.isWednesday())
            .thursday(product.isThursday())
            .friday(product.isFriday())
            .saturday(product.isSaturday())
            .sunday(product.isSunday())
            .build();
    }

    // Nutrition -> NutritionDTO : 필드명이 전부 동일해 자동 매핑
    ProductOptionDTO.NutritionDTO toNutritionDTO(Nutrition nutrition);

    // ===== 상세 페이지 (필드명 동일, 자동 매핑) =====
    BoardContentDTO toBoardContentDTO(BoardDetail boardDetail);

    // ===== 상품 정보 고시 (필드명 전부 동일, 자동 매핑) =====
    ProductInfoNoticeDTO toProductInfoNoticeDTO(ProductInfoNotice productInfoNotice);
}
