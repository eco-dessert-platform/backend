package com.bbangle.bbangle.board.controller;

import static java.util.Collections.emptyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Transactional
class BoardControllerTest extends AbstractIntegrationTest {

    @Autowired
    TokenProvider tokenProvider;

    Board board;
    Board board2;

    @Nested
    @DisplayName("기본 게시글 조회 API 테스트")
    class BasicBoard {

        @BeforeEach
        void setup() {
            Store store =  storeRepository.save(fixtureStore(emptyMap()));

            Map<String, Object> boardParams = new HashMap<>();
            boardParams.put("store", store);
            board = boardRepository.save(fixtureBoard(boardParams));
            board2 = boardRepository.save(fixtureBoard(boardParams));
        }

        @Test
        @DisplayName("순서나 필터링 조건이 없어도 정상적으로 조회한다.")
        void getBoardListSuccessWithoutAnyCondition() throws Exception {
            mockMvc.perform(get("/api/v1/boards"))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @ParameterizedTest
        @ValueSource(
                strings = {
                        "glutenFreeTag", "highProteinTag", "sugarFreeTag", "veganTag", "ketogenicTag"
                }
        )
        @DisplayName("순서가 없고 필터링 조건이 있어도 정상적으로 조회한다.")
        void getBoardListSuccessWithIngredientFilteringCondition(String ingredient)
                throws Exception {
            mockMvc.perform(get("/api/v1/boards")
                            .param(ingredient, "true"))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @ParameterizedTest
        @EnumSource(value = Category.class)
        @DisplayName("순서가 없고 카테고리 필터링 조건이 있어도 정상적으로 조회한다.")
        void getBoardListSuccessWithCategoryCondition(Category category) throws Exception {
            mockMvc.perform(get("/api/v1/boards")
                            .param("category", category.name()))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @ParameterizedTest
        @EnumSource(Category.class)
        @DisplayName("순서가 없고 필터링 조건 둘 이상 있어도 정상적으로 조회한다.")
        void getBoardListSuccessWithCategoryAndIngredientCondition(Category category)
                throws Exception {
            // given
            MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
            info.add("ketogenicTag", "true");
            info.add("category", category.name());
            // TODO: 이 부분 sort가 추가되면 500 error가 발생하는 문제
            // info.add("sort", "POPULAR");

            // when, then
            mockMvc.perform(get("/api/v1/boards")
                            .params(info))
                    .andExpect(status().isOk())
                    .andDo(print());
        }

        @ParameterizedTest
        @ValueSource(strings = {"bread", "school", "SOCCER", "잼"})
        @DisplayName("잘못된 카테고리로 카테고리 필터링 검색을 하면 조회한다.")
        void getBoardListFailWithWrongCategory(String category) throws Exception {
            mockMvc.perform(get("/api/v1/boards")
                            .param("category", category))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Nested
        @DisplayName("getProduct 메서드는")
        class GetProduct {

            @Test
            @DisplayName("유효한 boardId로 상품 정보를 가져올 수 있다")
            void getProductInfo() throws Exception {
                Long boardId = board.getId();
                mockMvc.perform(get("/api/v1/boards/" + boardId + "/product"))
                        .andExpect(status().isOk())
                        .andDo(print());
            }

            @Test
            @DisplayName("유효하지 않은 boardId를 요청 시 400에 에러를 발생시킨다")
            void throwError() throws Exception {
                mockMvc.perform(get("/api/v1/boards/9999/product"))
                        .andExpect(status().is4xxClientError())
                        .andDo(print());
            }
        }

        @Test
        @DisplayName("")
        void getProductTest() throws Exception {
            Long boardId = board.getId();
            mockMvc.perform(get("/api/v1/boards/" + boardId + "/product"))
                    .andExpect(status().isOk())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("폴더 내 게시글을 조회하는 API test")
    class BoardInFolderApiTest {

        Long memberId;
        Board createdBoard;
        Long wishListFolderId;

        @BeforeEach
        void setup() {
            // Member 생성
            Member member = memberRepository.save(fixtureMember(emptyMap()));
            memberId = member.getId();
            List<WishListFolder> wishListFolders = member.getWishListFolders();
            wishListFolderId = wishListFolders.get(0).getId();

            // Store 생성
            Store store =  storeRepository.save(fixtureStore(emptyMap()));

            // Board 생성
            Map<String, Object> boardParams = new HashMap<>();
            boardParams.put("store", store);
            createdBoard = boardRepository.save(fixtureBoard(boardParams));

            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                    new WishListBoardRequest(wishListFolderId));
        }

        @Test
        @DisplayName("정상적으로 폴더 안의 게시글을 조회할 수 있다.")
        @Transactional
        void getBoardInFolder() throws Exception {
            //given
            String authentication = getAuthentication(memberId);

            mockMvc.perform(get("/api/v1/boards/folders/" + wishListFolderId)
                            .header(HttpHeaders.AUTHORIZATION, authentication))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("SUCCESS"))
                    .andExpect(jsonPath("$.result.content[0].boardId").value(createdBoard.getId()))
                    .andExpect(jsonPath("$.result.content[0].storeId").value(createdBoard.getStore().getId()))
                    .andExpect(jsonPath("$.result.content[0].storeName").value(createdBoard.getStore().getName()))
                    .andExpect(jsonPath("$.result.content[0].thumbnail").value(createdBoard.getThumbnail()))
                    .andExpect(jsonPath("$.result.content[0].title").value(createdBoard.getTitle()))
                    .andExpect(jsonPath("$.result.content[0].price").value(createdBoard.getPrice()))
                    .andExpect(jsonPath("$.result.content[0].isWished").value(true))
                    .andExpect(jsonPath("$.result.content[0].isBundled").value(createdBoard.isBundled()))
                    .andExpect(jsonPath("$.result.content[0].reviewRate").value(
                            createdBoard.getBoardStatistic().getBoardReviewGrade()
                                    .round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue()))
                    .andExpect(jsonPath("$.result.content[0].reviewCount").value(
                            createdBoard.getBoardStatistic().getBoardReviewCount()))
                    .andExpect(jsonPath("$.result.content[0].isBbangcketing").value(createdBoard.isBbangketing()))
                    .andExpect(jsonPath("$.result.content[0].isSoldOut").value(createdBoard.isSoldOut()))
                    .andExpect(jsonPath("$.result.content[0].discountRate").value(createdBoard.getDiscountRate()))
                    .andExpect(jsonPath("$.result.nextCursor").value(-1))
                    .andExpect(jsonPath("$.result.hasNext").value(false));
        }

        private String getAuthentication(Long memberId) {
            String token = tokenProvider.generateToken(memberId, Duration.ofMinutes(1));
            return "Bearer " + token;
        }
    }
}
