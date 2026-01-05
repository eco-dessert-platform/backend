package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductInfoNoticeRepository extends JpaRepository<ProductInfoNotice, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE ProductInfoNotice pin
            SET pin.isDeleted = true
            WHERE pin.id IN (
                SELECT b.productInfoNotice.id
                FROM Board b
                WHERE b.id IN :boardIds
            )
        """)
    void softDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);

}
