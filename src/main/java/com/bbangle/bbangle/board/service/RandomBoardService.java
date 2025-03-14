package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomBoardService {

    private final BoardRepository boardRepository;

    public BoardCustomPage<List<BoardResponse>> getRandomBoardList(
        Long cursorId,
        Long memberId,
        Integer setNumber
    ) {
        List<BoardResponseDao> boardResponseDaoList = boardRepository.getRandomboardList(cursorId, memberId, setNumber);
        BoardCustomPage<List<BoardResponse>> boardPage = BoardPageGenerator.getBoardPage(
            boardResponseDaoList, false);
        if (Objects.nonNull(memberId)) {
            updateLikeStatus(boardPage, memberId);
        }
        return boardPage;
    }

    private void updateLikeStatus(
        BoardCustomPage<List<BoardResponse>> boardResponseDto,
        Long memberId
    ) {
        List<Long> responseList = extractIds(boardResponseDto);
        List<Long> likedContentIds = boardRepository.getLikedContentsIds(responseList, memberId);

        boardResponseDto.getContent()
            .stream()
            .filter(board -> likedContentIds.contains(board.getBoardId()))
            .forEach(board -> board.updateLike(true));
    }

    private List<Long> extractIds(
        BoardCustomPage<List<BoardResponse>> boardResponseDto
    ) {
        return boardResponseDto.getContent()
            .stream()
            .map(BoardResponse::getBoardId)
            .toList();
    }
}
