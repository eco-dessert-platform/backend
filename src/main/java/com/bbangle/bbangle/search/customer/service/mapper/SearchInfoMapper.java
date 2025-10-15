package com.bbangle.bbangle.search.customer.service.mapper;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SearchInfoMapper {

    @Mapping(target = "storeName", source = "board.store.name")
    @Mapping(target = "storeId", source = "board.store.id")
    @Mapping(target = "reviewRate", source = "board.boardStatistic.boardReviewGrade")
    @Mapping(target = "reviewCount", source = "board.boardStatistic.boardReviewCount")
    @Mapping(target = "isWished", source = "isWished")
    @Mapping(target = "isSoldOut", expression = "java(board.isSoldOut())")
    @Mapping(target = "isBundled", expression = "java(board.isBundled())")
    @Mapping(target = "isBbangcketing", expression = "java(board.isBbangketing())")
    @Mapping(target = "tags", expression = "java(board.getTags())")
    @Mapping(target = "boardId", source = "board.id")
    @Mapping(target = "thumbnail", source = "board.thumbnail")
    @Mapping(target = "title", source = "board.title")
    @Mapping(target = "price", source = "board.price")
    @Mapping(target = "discountRate", source = "board.discountRate")
    SearchInfo.Select toSearchSelectInfo(Board board, Boolean isWished);

    @Mapping(target = "boardLimitSize", source = "limitSize")
    @Mapping(target = "boardWishedMap", ignore = true)
    SearchInfo.BoardsInfo toBoardsInfo(List<Board> boards, Long boardCount, int limitSize);

}
