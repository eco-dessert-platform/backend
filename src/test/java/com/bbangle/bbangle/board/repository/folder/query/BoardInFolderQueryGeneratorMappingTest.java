package com.bbangle.bbangle.board.repository.folder.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class BoardInFolderQueryGeneratorMappingTest {

    private JPAQueryFactory queryFactory;
    private BooleanBuilder cursorBuilder;
    private OrderSpecifier<?> order;
    private WishListFolder wishListFolder;

    @Test
    @DisplayName("sort가 null인 경우 default QueryGenerator를 만든다.")
    void createDefaultQueryGeneratorWithNullSort() {
        //given
        FolderBoardSortType folderBoardSortType = null;

        //when
        BoardInFolderQueryGeneratorMapping queryMapper = new BoardInFolderQueryGeneratorMapping(
            cursorBuilder, queryFactory, order, wishListFolder, folderBoardSortType);
        QueryGenerator queryGenerator = queryMapper.mappingQueryGenerator();

        //then
        assertThat(queryGenerator).isInstanceOf(WishListRecentBoardQueryProvider.class);
    }

    @Test
    @DisplayName("sort가 WISHLIST_RECENT 경우 최신 담은 순 QueryGenerator를 만든다.")
    void createQueryGeneratorWithWishListRecent() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.WISHLIST_RECENT;

        //when
        BoardInFolderQueryGeneratorMapping queryMapper = new BoardInFolderQueryGeneratorMapping(
            cursorBuilder, queryFactory, order, wishListFolder, folderBoardSortType);
        QueryGenerator queryGenerator = queryMapper.mappingQueryGenerator();

        //then
        assertThat(queryGenerator).isInstanceOf(WishListRecentBoardQueryProvider.class);
    }

    @Test
    @DisplayName("sort가 POPULAR 인 경우 인기순 QueryGenerator를 만든다.")
    void createQueryGeneratorWithPopularSort() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.POPULAR;

        //when
        BoardInFolderQueryGeneratorMapping queryMapper = new BoardInFolderQueryGeneratorMapping(
            cursorBuilder, queryFactory, order, wishListFolder, folderBoardSortType);
        QueryGenerator queryGenerator = queryMapper.mappingQueryGenerator();

        //then
        assertThat(queryGenerator).isInstanceOf(PopularBoardQueryProvider.class);
    }

    @Test
    @DisplayName("sort가 LOW_PRICE 인 경우 낮은 가격순 QueryGenerator를 만든다.")
    void createQueryGeneratorWithLowPriceSort() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.LOW_PRICE;

        //when
        BoardInFolderQueryGeneratorMapping queryMapper = new BoardInFolderQueryGeneratorMapping(
            cursorBuilder, queryFactory, order, wishListFolder, folderBoardSortType);
        QueryGenerator queryGenerator = queryMapper.mappingQueryGenerator();

        //then
        assertThat(queryGenerator).isInstanceOf(LowPriceBoardQueryProvider.class);
    }

}
