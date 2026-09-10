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
            WHERE pi.board.id IN :boardIds AND pi.imgOrder = 0 AND pi.isDeleted = false
        """)
    List<ProductImg> findThumbnailImagesByBoardIds(@Param("boardIds") List<Long> boardId);

    /**
     * 게시글 상세 조회용: 삭제되지 않은 이미지를 imgOrder 오름차순(0번=썸네일)으로 조회.
     * 컬렉션 하나만 단독으로 조회하므로 N+1, 카티션 곱 걱정 없이 단순 파생 쿼리로 충분하다.
     */
    List<ProductImg> findAllByBoardIdAndIsDeletedFalseOrderByImgOrderAsc(Long boardId);
}
