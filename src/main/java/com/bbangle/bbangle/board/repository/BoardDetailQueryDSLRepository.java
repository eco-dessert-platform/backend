package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDetailQueryDSLRepository {

    String findByBoardId(Long boardId);

    List<SimilarityBoardDto> findSimilarityBoardByBoardId(Long memberId, List<Long> boardIds);

    List<Long> findSimilarityBoardIdsByNotSoldOut(Long boardId, int limit);
}
