package com.bbangle.bbangle.fixture.board.seller.controller.dto.response;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.ProductionStartTime;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardContentDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardDetailDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardDetailDTO.PriceDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.BoardImgDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.DeliveryDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductInfoNoticeDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductOptionDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductOptionDTO.NutritionDTO;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse.ProductOptionDTO.WeekDTO;
import java.util.List;

public final class SellerBoardResponseFixture {

    private SellerBoardResponseFixture() {}

    /**
     * 컨트롤러 슬라이스 테스트용 상세 응답 Fixture.
     * boardId만 가변으로 받고 나머지는 고정값으로 채운다.
     */
    public static SellerBoardDetailResponse defaultResponse(Long boardId) {
        return SellerBoardDetailResponse.builder()
            .BoardDetailDTO(defaultBoardDetailDTO(boardId))
            .deliveryDTO(defaultDeliveryDTO())
            .boardImgDTO(defaultBoardImgDTO())
            .Options(List.of(defaultProductOptionDTO()))
            .boardContent(new BoardContentDTO("<p>글루텐프리 식빵입니다.</p>"))
            .productInfoNotice(defaultProductInfoNoticeDTO())
            .build();
    }

    private static BoardDetailDTO defaultBoardDetailDTO(Long boardId) {
        return BoardDetailDTO.builder()
            .boardId(boardId)
            .name("글루텐 프리 케이크")
            .isFresh(false)
            .productionStartTime(ProductionStartTime.T_03_04)
            .price(new PriceDTO(10000, DiscountType.RATE, 20))
            .build();
    }

    private static DeliveryDTO defaultDeliveryDTO() {
        return new DeliveryDTO("유료", "CJ대한통운", 3000, 50000);
    }

    private static BoardImgDTO defaultBoardImgDTO() {
        return new BoardImgDTO("thumbnail.png", List.of("sub1.png", "sub2.png"));
    }

    private static ProductOptionDTO defaultProductOptionDTO() {
        return ProductOptionDTO.builder()
            .optionId(1L)
            .category(Category.BREAD)
            .optionName("기본 옵션")
            .tags(List.of(TagEnum.GLUTEN_FREE, TagEnum.HIGH_PROTEIN))
            .addedPrice(0)
            .stock(30)
            .week(defaultWeekDTO())
            .nutrition(defaultNutritionDTO())
            .build();
    }

    private static WeekDTO defaultWeekDTO() {
        return WeekDTO.builder()
            .monday(true)
            .tuesday(true)
            .wednesday(true)
            .thursday(true)
            .friday(true)
            .saturday(false)
            .sunday(false)
            .build();
    }

    private static NutritionDTO defaultNutritionDTO() {
        return NutritionDTO.builder()
            .weight(150)
            .servingWeight(150)
            .carbohydrates(20)
            .sugars(5)
            .protein(30)
            .fat(15)
            .calories(350)
            .build();
    }

    private static ProductInfoNoticeDTO defaultProductInfoNoticeDTO() {
        return ProductInfoNoticeDTO.builder()
            .productName("빵그리의 식빵")
            .foodType("빵")
            .manufacturer("빵그리")
            .originLocation("서울특별시 강남구")
            .manufactureDate("주문 후 익일 제조")
            .expirationDate("출고 후 냉동 2주")
            .storageGuide("상세페이지 첨부")
            .packagingQuantityUnit("상세페이지 첨부")
            .rawMaterialName("상세페이지 첨부")
            .nutritionInfo("해당사항 없음")
            .transgenic("해당사항 없음")
            .customerWarning("배송 즉시 냉장보관 해주세요.")
            .importFood("해당사항 없음")
            .build();
    }
}
