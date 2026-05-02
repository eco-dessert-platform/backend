package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.customer.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.customer.dto.TitleDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.MainCategory;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.SortType;
import com.bbangle.bbangle.board.repository.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.repository.dao.SellerBoardDao;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardQueryDSLRepository {

    List<BoardThumbnailDao> getThumbnailBoardsByIds(List<Long> boardIds,
        OrderSpecifier<?>[] orderCondition, Long memberId);

    List<TitleDto> findAllTitle();

    List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId);

    List<Board> checkingNullRanking();

    List<BoardWithTagDao> checkingNullWithPreferenceRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

    List<BoardThumbnailDao> getRandomboardList(Long cursorId, Long memberId, Integer setNumber);

    Page<SellerBoardDao> findSellerBoards(
        Long storeId,
        SaleStatus saleStatus,
        MainCategory mainCategory,
        Category category,
        String keyword,
        SortType sortBy,
        Pageable pageable
    );

    Map<SaleStatus, Long> countSellerBoardsBySaleStatus(Long storeId);

}

