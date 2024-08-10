package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

    /*
    * 실행 순서
    * 1. Product, Board INNER JOIN
    * 2. WishListBoard LEFT JOIN
    * 3. boardIds로 WHERE 조건절 수행
    * 4. GROUP BY boardId로 집계
    * 5. ORDER BY로 정렬
    * */
    @Query("SELECT new com.bbangle.bbangle.board.dto.BoardInfoDto(" +
        "b.id, " +
        "MIN(b.profile), " +
        "MIN(b.title), " +
        "MIN(b.price), " +
        "MAX(CASE WHEN p.soldout = false THEN 0 ELSE 1 END), " +
        "CASE WHEN COUNT(p.orderStartDate) > 0 THEN true ELSE false END, " +
        "MAX(CASE WHEN p.glutenFreeTag = true THEN 1 ELSE 0 END), " +
        "MAX(CASE WHEN p.highProteinTag = true THEN 1 ELSE 0 END), " +
        "MAX(CASE WHEN p.sugarFreeTag = true THEN 1 ELSE 0 END), " +
        "MAX(CASE WHEN p.veganTag = true THEN 1 ELSE 0 END), " +
        "MAX(CASE WHEN p.ketogenicTag = true THEN 1 ELSE 0 END), " +
        "CASE WHEN COUNT(DISTINCT p.category) > 1 THEN true ELSE false END, " +
        "CASE WHEN w.wishlistFolderId IS NOT NULL THEN true ELSE false END) " +
        "FROM Product p " +
        "JOIN Board b ON p.board.id = b.id " +
        "LEFT JOIN WishListBoard w ON b.id = w.boardId AND w.memberId = :memberId " +
        "WHERE b.id IN :boardIds " +
        "GROUP BY b.id " +
        "ORDER BY b.id desc ")
    List<BoardInfoDto> findTagCategoriesByBoardIds(@Param("boardIds") List<Long> boardIds, @Param("memberId") Long memberId);
}
