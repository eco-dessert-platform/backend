package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDetailQueryDSLRepository {

    List<String> findByBoardId(Long boardId);

    List<SimilarityBoardDto> findSimilarityBoardByBoardId(Long memberId, Long boardId);
}
