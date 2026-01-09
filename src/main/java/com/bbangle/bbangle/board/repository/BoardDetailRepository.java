package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.BoardDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDetailRepository extends JpaRepository<BoardDetail, Long>, BoardDetailQueryDSLRepository {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE BoardDetail bd
            SET bd.isDeleted = true
            WHERE bd.board.id IN :boardIds
        """)
    void softDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);


}
