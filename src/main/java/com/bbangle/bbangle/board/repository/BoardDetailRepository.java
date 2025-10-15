package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.BoardDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDetailRepository extends JpaRepository<BoardDetail, Long>,
    BoardDetailQueryDSLRepository {

}
