package com.bbangle.bbangle.board.seller.facade.command;

import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.seller.controller.dto.request.BoardDetailRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.NutritionInfoRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductInfoNoticeRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductRequest;
import com.bbangle.bbangle.board.seller.service.command.BoardDetailCommand;
import com.bbangle.bbangle.board.seller.service.command.CreateBoardServiceCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductImgCommand;
import com.bbangle.bbangle.board.seller.service.command.ProductInfoNoticeCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record CreateBoardFacadeCommand(
    Long sellerId,
    Long storeId,
    String title,
    Boolean isFresh,
    String productionStartTime,
    Integer price,
    Integer discountValue,
    String discountType,
    String deliveryCondition,
    String deliveryCompany,
    Integer deliveryFee,
    Integer freeShippingConditions,
    MultipartFile thumbnailImgFile,
    List<MultipartFile> productImgs,
    List<MultipartFile> boardDetailImages,
    List<ProductRequest> products,
    BoardDetailRequest boardDetailRequest,
    ProductInfoNoticeRequest productInfoNoticeRequest
) {
    public CreateBoardServiceCommand toServiceCommand(
        Store store,
        List<ProductImgCommand> productImgs,
        BoardDetailCommand boardDetailCommand
    ) {
        return CreateBoardServiceCommand.builder()
            .store(store)
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
            .products(toProductCommands())
            .productImgs(productImgs)
            .boardDetail(boardDetailCommand)
            .productInfoNotice(toProductInfoNoticeCommand())
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
            .customerWarning(productInfoNoticeRequest.getCustomerWaring())
            .importFood(productInfoNoticeRequest.getImportFood())
            .build();
    }

    private List<ProductCommand> toProductCommands() {
        return products.stream()
            .map(this::toProductCommand)
            .toList();
    }

    private ProductCommand toProductCommand(ProductRequest request) {
        if (request.getDietaryTags() == null || request.getAvailability() == null || request.getNutritionInfo() == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_REQUEST);
        }

        NutritionInfoRequest nutritionInfo = request.getNutritionInfo();
        Nutrition nutrition = new Nutrition(
            nutritionInfo.getTotalWeight(),
            nutritionInfo.getServingSize(),
            nutritionInfo.getCarbohydrates(),
            nutritionInfo.getSugars(),
            nutritionInfo.getProtein(),
            nutritionInfo.getFat(),
            nutritionInfo.getCalories()
        );

        return ProductCommand.builder()
            .title(request.getTitle())
            .category(request.getCategory())
            .plusPriceWithBoardPrice(request.getPlusPriceWithBoardPrice())
            .stock(request.getStock())
            .glutenFreeTag(request.getDietaryTags().isGlutenFreeTag())
            .highProteinTag(request.getDietaryTags().isHighProteinTag())
            .sugarFreeTag(request.getDietaryTags().isSugarFreeTag())
            .veganTag(request.getDietaryTags().isVeganTag())
            .ketogenicTag(request.getDietaryTags().isKetogenicTag())
            .monday(request.getAvailability().isMonday())
            .tuesday(request.getAvailability().isTuesday())
            .wednesday(request.getAvailability().isWednesday())
            .thursday(request.getAvailability().isThursday())
            .friday(request.getAvailability().isFriday())
            .saturday(request.getAvailability().isSaturday())
            .sunday(request.getAvailability().isSunday())
            .nutrition(nutrition)
            .build();
    }
}
