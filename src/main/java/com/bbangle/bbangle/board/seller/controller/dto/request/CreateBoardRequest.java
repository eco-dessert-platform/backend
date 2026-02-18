package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.seller.facade.command.CreateBoardFacadeCommand;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CreateBoardRequest {

    private Long storeId;
    private String title;
    private Boolean isFresh;
    private String productionStartTime;
    private Integer price;
    private Integer discountValue;
    private String discountType;
    private String deliveryCondition;
    private String deliveryCompany;
    private Integer deliveryFee;
    private Integer freeShippingConditions;
    private List<ProductRequest> products;
    @Valid
    private BoardDetailRequest boardDetailRequest;
    private ProductInfoNoticeRequest productInfoNoticeRequest;
    private MultipartFile thumbnailImgFile;
    private List<MultipartFile> productImgs;
    private List<MultipartFile> boardDetailImages;

    public CreateBoardFacadeCommand toCommand(Long sellerId) {
        return CreateBoardFacadeCommand.builder()
            .sellerId(sellerId)
            .storeId(storeId)
            .title(title)
            .isFresh(isFresh)
            .productionStartTime(productionStartTime)
            .price(price)
            .discountValue(discountValue)
            .discountType(discountType)
            .deliveryCondition(deliveryCondition)
            .deliveryCompany(deliveryCompany)
            .deliveryFee(deliveryFee)
            .freeShippingConditions(freeShippingConditions)
            .thumbnailImgFile(thumbnailImgFile)
            .productImgs(productImgs)
            .boardDetailImages(boardDetailImages)
            .products(products)
            .boardDetailRequest(boardDetailRequest)
            .productInfoNoticeRequest(productInfoNoticeRequest)
            .build();
    }
}
