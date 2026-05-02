package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.seller.facade.command.UpdateBoardFacadeCommand;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UpdateBoardRequest {

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
    private List<UpdateProductRequest> products;
    @Valid
    private BoardDetailRequest boardDetailRequest;
    private ProductInfoNoticeRequest productInfoNoticeRequest;
    private MultipartFile thumbnailImgFile;
    private String existingThumbnailUrl;
    private List<MultipartFile> newSubImages;
    private List<String> existingSubImageUrls;
    private List<MultipartFile> boardDetailImages;

    public UpdateBoardFacadeCommand toCommand(Long sellerId, Long boardId) {
        return UpdateBoardFacadeCommand.builder()
            .sellerId(sellerId)
            .boardId(boardId)
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
            .existingThumbnailUrl(existingThumbnailUrl)
            .newSubImages(newSubImages)
            .existingSubImageUrls(existingSubImageUrls)
            .boardDetailImages(boardDetailImages)
            .products(products)
            .boardDetailRequest(boardDetailRequest)
            .productInfoNoticeRequest(productInfoNoticeRequest)
            .build();
    }
}
