package com.bbangle.bbangle.board.controller.mapper;

import com.bbangle.bbangle.board.dto.BoardDetailResponse;
import com.bbangle.bbangle.board.service.dto.BoardDetailCommand;
import com.bbangle.bbangle.board.service.dto.BoardDetailInfo;
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
public interface BoardDetailMapper {

    @Mapping(target = "boardId", source = "boardId")
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "ipAddress", expression = "java(request.getRemoteAddr())")
    BoardDetailCommand.Main toBoardDetailMain(Long boardId, Long memberId, HttpServletRequest request);

    BoardDetailResponse.Main toBoardDetailMainResponse(BoardDetailInfo.Main info);



}
