package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishListBoardRepository extends JpaRepository<WishListBoard, Long>, WishListBoardQueryDSLRepository {

    @Query(value = "select wish from WishListBoard wish where wish.memberId = :memberId")
    Optional<List<WishListBoard>> findByMemberId(@Param("memberId") Long memberId);

    boolean existsByBoardIdAndMemberId(Long boardId, Long memberId);

    Optional<WishListBoard> findByBoardIdAndMemberId(Long boardId, Long memberId);

}
