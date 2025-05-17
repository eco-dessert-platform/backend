package com.bbangle.bbangle.store.service.mapper;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.service.dto.StoreInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StoreMapper {

        @Mapping(target = "reviewRate", source = "board.boardStatistic.boardReviewGrade")
        @Mapping(target = "reviewCount", source = "board.boardStatistic.boardReviewCount")
        @Mapping(target = "isWished", source = "isWished")
        @Mapping(target = "isSoldOut", expression = "java(board.isSoldOut())")
        @Mapping(target = "isBundled", expression = "java(board.isBundled())")
        @Mapping(target = "isBbangketing", expression = "java(board.isBbangketing())")
        @Mapping(target = "boardId", source = "board.id")
        StoreInfo.BestBoard toBestBoard(Board board, Boolean isWished);

}
