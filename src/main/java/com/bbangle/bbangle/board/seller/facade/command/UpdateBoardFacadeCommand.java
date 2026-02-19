package com.bbangle.bbangle.board.seller.facade.command;

import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.seller.controller.dto.request.BoardDetailRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.NutritionInfoRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductInfoNoticeRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.UpdateProductRequest;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductInfoNoticeCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.UpdateProductCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UpdateBoardFacadeCommand(
    Long sellerId,
    Long boardId,
    String title,
    Boolean isFresh,
    String productionStartTime,
    Integer price,
    Integer discountValue,
    String discountType,
    String deliveryCondition,
    String deliveryCompany,
    Integer freeShippingConditions,
    Integer deliveryFee,
    MultipartFile thumbnailImgFile,
    String existingThumbnailUrl,
    List<MultipartFile> newSubImages,
    List<String> existingSubImageUrls,
    List<MultipartFile> boardDetailImages,
    BoardDetailRequest boardDetailRequest,
    ProductInfoNoticeRequest productInfoNoticeRequest,
    List<UpdateProductRequest> products
) {

    public UpdateBoardServiceCommand toServiceCommand(
        List<ProductImgCommand> productImgCommands,
        BoardDetailCommand boardDetailCommand
    ) {
        return UpdateBoardServiceCommand.builder()
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
            .freeShippingConditions(freeShippingConditions)
            .deliveryFee(deliveryFee)
            .productImgs(productImgCommands)
            .boardDetail(boardDetailCommand)
            .productInfoNotice(toProductInfoNoticeCommand())
            .products(toUpdateProductCommands())
            .build();
    }

    private ProductInfoNoticeCommand toProductInfoNoticeCommand() {
        return ProductInfoNoticeCommand.builder()
            .productName(productInfoNoticeRequest.getProductName())
            .foodType(productInfoNoticeRequest.getFoodType())
            .manufacturer(productInfoNoticeRequest.getManufacturer())
            .originLocation(productInfoNoticeRequest.getOriginLocation())
            .manufactureDate(productInfoNoticeRequest.getManufactureDate())
            .expirationDate(productInfoNoticeRequest.getExpirationDate())
            .storageGuide(productInfoNoticeRequest.getStorageGuide())
            .packagingQuantityUnit(productInfoNoticeRequest.getPackagingQuantityUnit())
            .rawMaterialName(productInfoNoticeRequest.getRawMaterialName())
            .nutritionInfo(productInfoNoticeRequest.getNutritionInfo())
            .transgenic(productInfoNoticeRequest.getTransgenic())
            .customerWarning(productInfoNoticeRequest.getCustomerWarning())
            .importFood(productInfoNoticeRequest.getImportFood())
            .build();
    }

    private List<UpdateProductCommand> toUpdateProductCommands() {
        return products.stream()
            .map(this::toUpdateProductCommand)
            .toList();
    }

    private UpdateProductCommand toUpdateProductCommand(UpdateProductRequest req) {
        if (req.getDietaryTags() == null || req.getAvailability() == null || req.getNutritionInfo() == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_REQUEST);
        }
        NutritionInfoRequest ni = req.getNutritionInfo();
        Nutrition nutrition = new Nutrition(
            ni.getTotalWeight(),
            ni.getServingSize(),
            ni.getCarbohydrates(),
            ni.getSugars(),
            ni.getProtein(),
            ni.getFat(),
            ni.getCalories()
        );
        return UpdateProductCommand.builder()
            .productId(req.getProductId())
            .title(req.getTitle())
            .category(req.getCategory())
            .plusPriceWithBoardPrice(req.getPlusPriceWithBoardPrice())
            .stock(req.getStock())
            .glutenFreeTag(req.getDietaryTags().isGlutenFreeTag())
            .highProteinTag(req.getDietaryTags().isHighProteinTag())
            .sugarFreeTag(req.getDietaryTags().isSugarFreeTag())
            .veganTag(req.getDietaryTags().isVeganTag())
            .ketogenicTag(req.getDietaryTags().isKetogenicTag())
            .monday(req.getAvailability().isMonday())
            .tuesday(req.getAvailability().isTuesday())
            .wednesday(req.getAvailability().isWednesday())
            .thursday(req.getAvailability().isThursday())
            .friday(req.getAvailability().isFriday())
            .saturday(req.getAvailability().isSaturday())
            .sunday(req.getAvailability().isSunday())
            .nutrition(nutrition)
            .build();
    }
}
