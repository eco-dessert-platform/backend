//package com.bbangle.bbangle.board.repository;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//
//import com.bbangle.bbangle.AbstractIntegrationTest;
//import com.bbangle.bbangle.board.domain.Board;
//import com.bbangle.bbangle.board.domain.Category;
//import com.bbangle.bbangle.board.domain.Product;
//import com.bbangle.bbangle.board.dto.BoardResponseDto;
//import com.bbangle.bbangle.board.dto.CursorInfo;
//import com.bbangle.bbangle.board.dto.FilterRequest;
//import com.bbangle.bbangle.board.sort.SortType;
//import com.bbangle.bbangle.page.BoardCustomPage;
//import java.time.DayOfWeek;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import org.assertj.core.util.Lists;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.EnumSource;
//import org.junit.jupiter.params.provider.ValueSource;
//
//class BoardRepositoryFilterTest extends AbstractIntegrationTest {
//
//    private final SortType DEFAULT_SORT = SortType.RECOMMEND;
//    private final CursorInfo DEFAULT_CURSOR = new CursorInfo(null, null);
//
//    @ParameterizedTest
//    @DisplayName("[게시글조회] Tag 필터 정상 확인")
//    @ValueSource(
//        strings = {"glutenFreeTag", "highProteinTag", "sugarFreeTag", "veganTag", "ketogenicTag"}
//    )
//    void test1(String tagName) {
//        // given
//        Product target = fixtureProduct(Map.of(tagName, true));
//        Product nonTarget = fixtureProduct(Map.of(tagName, false));
//        Board targetboard = fixtureBoard(Map.of("productList", Lists.newArrayList(target)));
//        Board nonTargetBoard = fixtureBoard(Map.of("productList", Lists.newArrayList(nonTarget)));
//
//        fixtureRanking(Map.of("board", targetboard));
//        fixtureRanking(Map.of("board", nonTargetBoard));
//
//        FilterRequest filter = FilterRequest.builder()
//            .glutenFreeTag(target.isGlutenFreeTag())
//            .highProteinTag(target.isHighProteinTag())
//            .sugarFreeTag(target.isSugarFreeTag())
//            .veganTag(target.isVeganTag())
//            .ketogenicTag(target.isKetogenicTag())
//            .build();
//
//        // when
//        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
//            .getBoardResponseList(filter, DEFAULT_SORT, DEFAULT_CURSOR);
//        List<BoardResponseDto> result = resultPage.getContent();
//
//        // then
//        String expectedTagName = tagName.replace("Tag", "");
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getTags()).contains(expectedTagName);
//    }
//
//    @ParameterizedTest
//    @DisplayName("[게시글조회] 카테고리 필터 정상 확인")
//    @EnumSource(
//        value = Category.class
//    )
//    void test2(Category category) {
//        // given
//        Category nonTargetCategory = Arrays.stream(Category.values())
//            .filter(it -> !it.equals(category))
//            .findFirst()
//            .get();
//
//        Product target = fixtureProduct(Map.of("category", category));
//        Product nonTarget = fixtureProduct(Map.of("category", nonTargetCategory));
//        Board targetboard = fixtureBoard(Map.of("productList", Lists.newArrayList(target)));
//        Board nonTargetBoard = fixtureBoard(Map.of("productList", Lists.newArrayList(nonTarget)));
//
//        fixtureRanking(Map.of("board", targetboard));
//        fixtureRanking(Map.of("board", nonTargetBoard));
//
//        FilterRequest filter = FilterRequest.builder()
//            .category(category)
//            .build();
//
//        // when
//        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
//            .getBoardResponseList(filter, DEFAULT_SORT, DEFAULT_CURSOR);
//        List<BoardResponseDto> result = resultPage.getContent();
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getBoardId()).isEqualTo(targetboard.getId());
//    }
//
//
//    @Test
//    @DisplayName("[게시글조회] 금액 필터 정상 확인")
//    void test3() {
//        // given
//        int minPrice = 100;
//        int maxPrice = 200;
//
//        Board nonTargetBoard1 = fixtureBoard(Map.of("price", minPrice - 1));
//        Board nonTargetBoard2 = fixtureBoard(Map.of("price", maxPrice + 1));
//
//        Board targetBoard1 = fixtureBoard(Map.of("price", minPrice));
//        Board targetBoard2 = fixtureBoard(Map.of("price", maxPrice));
//
//        fixtureRanking(Map.of("board", targetBoard1));
//        fixtureRanking(Map.of("board", targetBoard2));
//        fixtureRanking(Map.of("board", nonTargetBoard1));
//        fixtureRanking(Map.of("board", nonTargetBoard2));
//
//        FilterRequest filter = FilterRequest.builder()
//            .minPrice(minPrice)
//            .maxPrice(maxPrice)
//            .build();
//
//        // when
//        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
//            .getBoardResponseList(filter, DEFAULT_SORT, DEFAULT_CURSOR);
//        List<BoardResponseDto> result = resultPage.getContent();
//
//        // then
//        assertThat(result).hasSize(2);
//        List<Long> idList = result.stream().map(BoardResponseDto::getBoardId)
//            .collect(Collectors.toList());
//        assertThat(idList).contains(targetBoard1.getId(), targetBoard2.getId());
//    }
//
//    @Test
//    @DisplayName("[게시글조회] 오늘주문가능 필터 정상 확인")
//    void test4() {
//        // given
//        DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
//
//        Board nonTargetBoard1 = fixtureBoard(Map.of(getDaysOfWeekFieldName(dayOfWeek), false));
//        Board targetBoard1 = fixtureBoard(Map.of(getDaysOfWeekFieldName(dayOfWeek), true));
//
//        fixtureRanking(Map.of("board", nonTargetBoard1));
//        fixtureRanking(Map.of("board", targetBoard1));
//
//        FilterRequest filter = FilterRequest.builder()
//            .orderAvailableToday(true)
//            .build();
//
//        // when
//        BoardCustomPage<List<BoardResponseDto>> resultPage = boardRepository
//            .getBoardResponseList(filter, DEFAULT_SORT, DEFAULT_CURSOR);
//        List<BoardResponseDto> result = resultPage.getContent();
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getBoardId()).isEqualTo(targetBoard1.getId());
//    }
//
//    private String getDaysOfWeekFieldName(DayOfWeek dayOfWeek) {
//        return switch (dayOfWeek) {
//            case MONDAY -> "monday";
//            case TUESDAY -> "tuesday";
//            case WEDNESDAY -> "wednesday";
//            case THURSDAY -> "thursday";
//            case FRIDAY -> "friday";
//            case SATURDAY -> "saturday";
//            case SUNDAY -> "sunday";
//        };
//    }
//}
