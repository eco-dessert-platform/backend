package com.bbangle.bbangle.board.repository.folder.cursor;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardInFolderCursorGeneratorMappingTest {

    private BoardInFolderCursorGeneratorMapping mapper;

    private Long testMemberId;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    void setup(){
        Random random = new Random();
        testMemberId = random.nextLong(1_000_000) + 1;
    }

    @Test
    @DisplayName("정렬 타입이 null 인 경우 위시리스트 담은 순 CursorGenerator를 반환한다.")
    void createCursorGeneratorWithNullSort() {
        //given
        FolderBoardSortType folderBoardSortType = null;

        mapper = new BoardInFolderCursorGeneratorMapping(testMemberId, testMemberId, queryFactory, folderBoardSortType);

        //when
        CursorGenerator cursorGenerator = mapper.mappingCursorGenerator();

        //then
        assertThat(cursorGenerator).isInstanceOf(WishListRecentCursorGenerator.class);
    }

    @Test
    @DisplayName("정렬 타입이 WISHLIST_RECENT 인 경우 위시리스트 담은 순 CursorGenerator를 반환한다.")
    void createCursorGeneratorWithWishListRecentSort() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.WISHLIST_RECENT;

        mapper = new BoardInFolderCursorGeneratorMapping(testMemberId, testMemberId, queryFactory, folderBoardSortType);

        //when
        CursorGenerator cursorGenerator = mapper.mappingCursorGenerator();

        //then
        assertThat(cursorGenerator).isInstanceOf(WishListRecentCursorGenerator.class);
    }

    @Test
    @DisplayName("정렬 타입이 POPULAR 인 경우 인기순 CursorGenerator를 반환한다.")
    void createCursorGeneratorWithPopularSort() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.POPULAR;

        mapper = new BoardInFolderCursorGeneratorMapping(testMemberId, testMemberId, queryFactory, folderBoardSortType);

        //when
        CursorGenerator cursorGenerator = mapper.mappingCursorGenerator();

        //then
        assertThat(cursorGenerator).isInstanceOf(PopularCursorGenerator.class);
    }

    @Test
    @DisplayName("정렬 타입이 LOW_PRICE 인 경우 낮은가격순 CursorGenerator를 반환한다.")
    void createCursorGeneratorWithLowPriceSort() {
        //given
        FolderBoardSortType folderBoardSortType = FolderBoardSortType.LOW_PRICE;

        mapper = new BoardInFolderCursorGeneratorMapping(testMemberId, testMemberId, queryFactory, folderBoardSortType);

        //when
        CursorGenerator cursorGenerator = mapper.mappingCursorGenerator();

        //then
        assertThat(cursorGenerator).isInstanceOf(LowPriceInFolderCursorGenerator.class);
    }

}

