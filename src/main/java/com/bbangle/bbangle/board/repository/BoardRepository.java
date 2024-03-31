package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE Board b SET b.detail = :detailUrl WHERE b.id = :boardId ")
//    int updateDetailWhereStoreIdEqualsBoardId(
//        @Param("boardId")
//        Long boardId,
//        @Param("detailUrl")
//        String detailUrl
//    );

}
