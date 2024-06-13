package com.bbangle.bbangle.board.repository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDetailQueryDSLRepository {

    List<String> findByBoardId(Long boardId);
}
