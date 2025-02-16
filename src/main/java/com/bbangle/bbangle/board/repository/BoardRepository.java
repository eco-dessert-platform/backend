package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

    @Query("SELECT new com.bbangle.bbangle.board.dto.BoardInfoDto(" +
        "b.id, " +
        "MIN(b.profile), " +
        "MIN(b.title), " +
        "MIN(b.price), " +
        "MIN(b.discountRate), " +
        "MIN(s.boardReviewGrade), " +
        "MIN(s.boardReviewCount), " +
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
        "JOIN Board b ON b.store.id = :storeId AND p.board.id = b.id " +
        "JOIN BoardStatistic s ON s.board = b " +
        "LEFT JOIN WishListBoard w ON b.id = w.boardId AND w.memberId = :memberId " +
        "WHERE b.id <= :cursorId " +
        "GROUP BY b.id " +
        "ORDER BY b.id desc " +
        "LIMIT 11")
    List<BoardInfoDto> findBoardsByStoreWithCursor(@Param("storeId") Long storeId,
        @Param("memberId") Long memberId, @Param("cursorId") Long cursorId);

    @Query("SELECT new com.bbangle.bbangle.board.dto.BoardInfoDto(" +
        "b.id, " +
        "MIN(b.profile), " +
        "MIN(b.title), " +
        "MIN(b.price), " +
        "MIN(b.discountRate), " +
        "MIN(s.boardReviewGrade), " +
        "MIN(s.boardReviewCount), " +
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
        "JOIN Board b ON p.board.id = b.id AND b.store.id = :storeId " +
        "JOIN BoardStatistic s ON s.board = b " +
        "LEFT JOIN WishListBoard w ON b.id = w.boardId AND w.memberId = :memberId " +
        "GROUP BY s.basicScore, b.id " +
        "ORDER BY s.basicScore desc " +
        "LIMIT 3")
    List<BoardInfoDto> findBestBoards(@Param("memberId") Long memberId,
        @Param("storeId") Long storeId);
}
