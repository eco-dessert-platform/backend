package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.ProductImg;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImgRepository extends JpaRepository<ProductImg, Long> {
    List<ProductImg> findAllByIdInOrderByIdAsc(List<Long> imageIds);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ProductImg pi
            SET pi.isDeleted = true
            WHERE pi.board.id IN :boardIds
        """)
    void softDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Query("""
            SELECT pi
            FROM ProductImg pi
            WHERE pi.board.id IN :boardIds AND pi.imgOrder = 0
        """)
    List<ProductImg> findThumbnailImagesByBoardIds(@Param("boardIds") List<Long> boardId);
}
