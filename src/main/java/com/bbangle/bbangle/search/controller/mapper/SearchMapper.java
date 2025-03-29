package com.bbangle.bbangle.search.controller.mapper;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.service.dto.BoardDetailCommand;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import jakarta.servlet.http.HttpServletRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SearchMapper {

    @Mapping(target = "sort", source = "sort")
    @Mapping(target = "keyword", source = "keyword")
    @Mapping(target = "filterRequest", source = "filterRequest")
    @Mapping(target = "cursorId", source = "cursorId")
    @Mapping(target = "memberId", source = "memberId")
    SearchCommand.Main toSearchMain(FilterRequest filterRequest, SortType sort, String keyword, Long cursorId, Long memberId);

}
