package com.bbangle.bbangle.board.repository.folder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.repository.util.BoardPageGenerator;
import com.bbangle.bbangle.fixture.TagDaoFixture;
import com.bbangle.bbangle.page.BoardCustomPage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardPageGeneratorTest {

    static List<BoardResponseDao> testList = List.of(
        new BoardResponseDao(1L, 1L, "store1", "thumbnail1", "title1", 1000,
            Category.BREAD, TagDaoFixture.getSugarFreeTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(1L, 1L, "store1", "thumbnail1", "title1", 1000,
            Category.YOGURT, TagDaoFixture.getGlutenFreeTagAndHighProteinTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(2L, 2L, "store2", "thumbnail2", "title2", 2000,
            Category.BREAD, TagDaoFixture.getKetogenicTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(2L, 2L, "store2", "thumbnail2", "title2", 2000,
            Category.BREAD, TagDaoFixture.getKetogenicTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(2L, 2L, "store2", "thumbnail2", "title2", 2000,
            Category.BREAD, TagDaoFixture.getProteinHighTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(3L, 3L, "store3", "thumbnail3", "title3", 3000,
            Category.SNACK, TagDaoFixture.getSugarFreeTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(3L, 3L, "store3", "thumbnail3", "title3", 3000,
            Category.JAM, TagDaoFixture.getGlutenFreeTagAndHighProteinTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10),
        new BoardResponseDao(3L, 3L, "store3", "thumbnail3", "title3", 3000,
            Category.SNACK, TagDaoFixture.getVeganTagDao(), BigDecimal.ONE, 1L, LocalDateTime.now(), true, 10)
    );

    List<String> tag1 = List.of("sugarFree", "glutenFree", "highProtein");
    List<String> tag2 = List.of("ketogenic", "highProtein");
    List<String> tag3 = List.of("sugarFree", "glutenFree", "highProtein", "vegan");

    @Test
    @DisplayName("정상적으로 BoardResponseDao로부터 BoardResponseDto 페이지 리스트를 반환한다.")
    void convertToBoardResponseDtoPageFromBoardResponseDao() {
        //given, when
        BoardCustomPage<List<BoardResponseDto>> boardPage = BoardPageGenerator.getBoardPage(
            testList, false);

        //then
        assertAll(
            () -> assertThat(boardPage.getNextCursor()).isEqualTo(-1),
            () -> assertThat(boardPage.getContent()).hasSize(3),
            () -> assertThat(boardPage.getContent().get(0).getBoardId()).isOne(),
            () -> assertThat(boardPage.getContent().get(0).getStoreId()).isOne(),
            () -> assertThat(boardPage.getContent().get(0).getStoreName()).isEqualTo("store1"),
            () -> assertThat(boardPage.getContent().get(0).getThumbnail()).isEqualTo("thumbnail1"),
            () -> assertThat(boardPage.getContent().get(0).getTitle()).isEqualTo("title1"),
            () -> assertThat(boardPage.getContent().get(0).getPrice()).isEqualTo(1000),
            () -> assertThat(boardPage.getContent().get(0).getIsBundled()).isTrue(),
            () -> assertThat(boardPage.getContent().get(0).getTags()).hasSize(3),
            () -> boardPage.getContent().get(0).getTags().forEach(tag -> assertThat(tag1).contains(tag)),
            () -> assertThat(boardPage.getContent().get(1).getBoardId()).isEqualTo(2L),
            () -> assertThat(boardPage.getContent().get(1).getStoreName()).isEqualTo("store2"),
            () -> assertThat(boardPage.getContent().get(1).getStoreId()).isEqualTo(2L),
            () -> assertThat(boardPage.getContent().get(1).getThumbnail()).isEqualTo("thumbnail2"),
            () -> assertThat(boardPage.getContent().get(1).getTitle()).isEqualTo("title2"),
            () -> assertThat(boardPage.getContent().get(1).getPrice()).isEqualTo(2000),
            () -> assertThat(boardPage.getContent().get(1).getIsBundled()).isFalse(),
            () -> assertThat(boardPage.getContent().get(1).getTags()).hasSize(2),
            () -> boardPage.getContent().get(1).getTags().forEach(tag -> assertThat(tag2).contains(tag)),
            () -> assertThat(boardPage.getContent().get(2).getBoardId()).isEqualTo(3L),
            () -> assertThat(boardPage.getContent().get(2).getStoreId()).isEqualTo(3L),
            () -> assertThat(boardPage.getContent().get(2).getStoreName()).isEqualTo("store3"),
            () -> assertThat(boardPage.getContent().get(2).getThumbnail()).isEqualTo("thumbnail3"),
            () -> assertThat(boardPage.getContent().get(2).getTitle()).isEqualTo("title3"),
            () -> assertThat(boardPage.getContent().get(2).getPrice()).isEqualTo(3000),
            () -> assertThat(boardPage.getContent().get(2).getIsBundled()).isTrue(),
            () -> assertThat(boardPage.getContent().get(2).getTags()).hasSize(4),
            () -> boardPage.getContent().get(2).getTags().forEach(tag -> assertThat(tag3).contains(tag))
            );
    }


}
