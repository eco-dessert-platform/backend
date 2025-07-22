package com.bbangle.bbangle.board.service.mapper;


import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.service.dto.BoardDetailInfo;
import com.bbangle.bbangle.common.service.CommonMapper;
import org.mapstruct.*;

import java.util.List;
import java.util.Objects;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BoardDetailInfoMapper extends CommonMapper {

    @Mapping(target = "storeTitle", source = "board.store.name")
    @Mapping(target = "storeProfile", source = "board.store.profile")
    @Mapping(target = "storeId", source = "board.store.id")
    @Mapping(target = "isStoreWished", source = "isWishedStore")
    BoardDetailInfo.Store toStoreInfo(Board board, Boolean isWishedStore);

    @Mapping(target = "boardTitle", source = "board.title")
    @Mapping(target = "isSoldout", expression = "java(board.isSoldOut())")
    @Mapping(target = "boardProfile", expression = "java(board.getThumbnail())")
    @Mapping(target = "boardPrice", source = "board.price")
    @Mapping(target = "boardImages", expression = "java(board.getSupportingImages())")
    @Mapping(target = "boardDetail", source = "board.boardDetail", qualifiedByName = "getBoardDetailUrl")
    @Mapping(target = "isBundled", expression = "java(board.isBundled())")
    @Mapping(target = "isBoardWished", source = "isWishedBoard")
    @Mapping(target = "boardId", source = "board.id")
    BoardDetailInfo.Board toBoardInfo(Board board, Boolean isWishedBoard);

    @Named("getBoardDetailUrl")
    default String getBoardDetailUrl(BoardDetail boardDetail) {
        return Objects.nonNull(boardDetail) ? boardDetail.getFullUrl() : "";
    }

    @Mapping(target = "orderType", expression = "java(product.getOrderType())")
    BoardDetailInfo.ProductOrderType toProductOrderTypeInfo(Product product);

    @Mapping(target = "nutrient", source = "product.nutrition")
    @Mapping(target = "isSoldout", expression = "java(product.isSoldout())")
    @Mapping(target = "isBbangketting", expression = "java(isBbangketting(product.getId(), bbangkettingProductIds))")
    @Mapping(target = "orderType", expression = "java(toProductOrderTypeInfo(product))")
    BoardDetailInfo.Product toProductInfo(Product product, @Context List<Long> bbangkettingProductIds);

    @Named("isBbangketting")
    default Boolean isBbangketting(Long productId, List<Long> bbangkettingProductIds) {
        return bbangkettingProductIds != null && bbangkettingProductIds.contains(productId);
    }

    @IterableMapping(elementTargetType = BoardDetailInfo.Product.class)
    List<BoardDetailInfo.Product> toProductInfo(List<Product> products, @Context List<Long> bbangkettingProductIds);

    default BoardDetailInfo.Product createProductInstance() {
        return new BoardDetailInfo.Product(); // 구체적인 클래스 제공
    }

    @Mapping(target = "store", expression = "java(toStoreInfo(board, isWishedStore))")
    @Mapping(target = "board", expression = "java(toBoardInfo(board, isWishedBoard))")
    @Mapping(target = "products", expression = "java(toProductInfo(board.getProducts(), bbangkettingProductIds))")
    BoardDetailInfo.Main toMainInfo(Board board, Boolean isWishedStore, Boolean isWishedBoard, List<Long> bbangkettingProductIds);

}
