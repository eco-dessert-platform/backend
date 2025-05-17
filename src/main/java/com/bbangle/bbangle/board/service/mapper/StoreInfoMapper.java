package com.bbangle.bbangle.board.service.mapper;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.board.service.dto.StoreInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StoreInfoMapper {

        @Mapping(target = "reviewRate", source = "board.boardStatistic.boardReviewGrade")
        @Mapping(target = "reviewCount", source = "board.boardStatistic.boardReviewCount")
        @Mapping(target = "isWished", source = "isWished")
        @Mapping(target = "isSoldOut", expression = "java(board.isSoldOut())")
        @Mapping(target = "isBundled", expression = "java(board.isBundled())")
        @Mapping(target = "isBbangketing", expression = "java(board.isBbangketing())")
        @Mapping(target = "boardId", source = "board.id")
        StoreInfo.BestBoard toBestBoard(Board board, Boolean isWished);

        @Mapping(target = "title", source = "name")
        @Mapping(target = "isWished", ignore = true)
        StoreInfo.Store toStoreInfo(Store store);

        @Mapping(target = "storeId", source = "store.id")
        @Mapping(target = "storeName", source = "store.name")
        @Mapping(target = "isWished", ignore = true)
        StoreInfo.StoreDetail toStoreDetail(Store store);

        @Mapping(target = "boardId", source = "board.id")
        @Mapping(target = "thumbnail", expression = "java(board.getThumbnail())")
        @Mapping(target = "title", source = "board.title")
        @Mapping(target = "price", source = "board.price")
        @Mapping(target = "discountRate", source = "board.discountRate")
        @Mapping(target = "reviewRate", source = "board.boardStatistic.boardReviewGrade")
        @Mapping(target = "reviewCount", source = "board.boardStatistic.boardReviewCount")
        @Mapping(target = "isSoldOut", expression = "java(board.isSoldOut())")
        @Mapping(target = "isBbangcketing", expression = "java(board.isBbangketing())")
        @Mapping(target = "tags", expression = "java(board.getTags())")
        @Mapping(target = "isBundled", expression = "java(board.isBundled())")
        StoreInfo.AllBoard toAllBoard(Board board, Boolean isWished);

}
