package com.bbangle.bbangle.search.customer.controller.mapper;

import com.bbangle.bbangle.board.domain.constant.SortType;
import com.bbangle.bbangle.board.customer.dto.FilterRequest;
import com.bbangle.bbangle.search.customer.service.dto.SearchCommand;
import org.mapstruct.*;

import java.util.Objects;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SearchMapper {

    @Mapping(target = "limitSize", source = "limitSize", qualifiedByName = "limitSize")
    @Mapping(target = "isExcludedProduct", expression = "java(isExcludedProduct(memberId, sort))")
    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "keyword", source = "keyword")
    @Mapping(target = "filterRequest", source = "filterRequest")
    @Mapping(target = "cursorId", source = "cursorId")
    @Mapping(target = "memberId", source = "memberId")
    SearchCommand.Main toSearchMain(FilterRequest filterRequest, SortType sort, String keyword, Long cursorId, Long memberId, Long limitSize);

    @Named("isExcludedProduct")
    default Boolean isExcludedProduct(Long memberId, SortType sort) {
        return Objects.nonNull(memberId) && sort.equals(SortType.RECOMMEND);
    }

    @Named("limitSize")
    default long limitSize(Long limitSize) {
        return limitSize > 31 ? 30 : limitSize;
    }
}
