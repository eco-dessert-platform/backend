package com.bbangle.bbangle.board.recommend.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.domain.MemberSegment;
import com.bbangle.bbangle.board.recommend.repository.MemberSegmentRepository;
import com.bbangle.bbangle.board.recommend.repository.RecommendBoardRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendBoardService {

    private static final Boolean DEFAULT_BOARD = false;

    private final MemberSegmentRepository memberSegmentRepository;
    private final RecommendBoardRepository recommendboardRepository;
    private final BoardService boardService;
    private final BoardRepository boardRepository;

    public CursorPageResponse<BoardResponse> getBoardList(
            FilterRequest filterRequest,
            Long cursorId,
            Long memberId
    ) {
        //TODO: 1. MEMBER 의 SEGMENT 정보 가져오기(member_segment_and_intolerance)
        MemberSegment memberSegment = memberSegmentRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND));

        //TODO: 2. SEGMENT 정보와 일치하는 게시글 id 가져오기(커서 기반 페이지네이션 포함)
        List<Long> boardIds = recommendboardRepository.getRecommendBoardList(
                filterRequest, cursorId, memberSegment);

        List<BoardResponseDao> daos = boardRepository.getThumbnailBoardsByIds(boardIds,
                SortType.MOST_WISHED.getOrderExpression(), memberId);

        return boardService.getResponseFromDao(daos, DEFAULT_BOARD);
    }


}
