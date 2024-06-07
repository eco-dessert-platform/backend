package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import java.time.Duration;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
            Store store = storeGenerator();
            storeRepository.save(store);

            board = boardGenerator(store,
                true,
                true,
                true,
                true,
                true,
                true,
                true);

            board2 = boardGenerator(store,
                true,
                false,
                true,
                true,
                false,
                true,
                true);
            Board save1 = boardRepository.save(board);
            Board save2 = boardRepository.save(board2);
            rankingRepository.save(BoardStatistic.builder()
                .board(save1)
                .popularScore(0.0)
                .recommendScore(0.0)
                .build());
            rankingRepository.save(BoardStatistic.builder()
                .board(save2)
                .popularScore(0.0)
                .recommendScore(0.0)
                .build());

            Product product1 = productGenerator(board,
                false,
                false,
                true,
                false,
                true);

            Product product2 = productGenerator(board,
                false,
                true,
                true,
                false,
                true);

            Product product3 = productGenerator(board2,
                true,
                false,
                true,
                false,
                true);

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
        }

        @Test
        @DisplayName("순서나 필터링 조건이 없어도 정상적으로 조회한다.")
        void getBoardListSuccessWithoutAnyCondition() throws Exception {
            //given, when, then
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
            //given, when, then
            mockMvc.perform(get("/api/v1/boards")
                    .param(ingredient, "true"))
                .andExpect(status().isOk())
                .andDo(print());
        }

        @ParameterizedTest
        @EnumSource(value = Category.class)
        @DisplayName("순서가 없고 카테고리 필터링 조건이 있어도 정상적으로 조회한다.")
        void getBoardListSuccessWithCategoryCondition(Category category) throws Exception {
            //given, when, then
            mockMvc.perform(get("/api/v1/boards")
                    .param("category", category.name()))
                .andExpect(status().isOk())
                .andDo(print());
        }

        @ParameterizedTest
        @ValueSource(strings = {"BREAD", "COOKIE", "TART", "JAM", "YOGURT", "ETC"})
        @DisplayName("순서가 없고 필터링 조건 둘 이상 있어도 정상적으로 조회한다.")
        void getBoardListSuccessWithCategoryAndIngredientCondition(String ingredient)
            throws Exception {
            // given
            MultiValueMap<String, String> info = new LinkedMultiValueMap<>();
            info.add("ketogenicTag", "true");
            info.add("category", ingredient);
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
            // given, when, then
            mockMvc.perform(get("/api/v1/boards")
                    .param("category", category))
                .andExpect(status().isBadRequest())
                .andDo(print());
        }


        private Board boardGenerator(
            Store store,
            boolean sunday,
            boolean monday,
            boolean tuesday,
            boolean wednesday,
            boolean thursday,
            boolean friday,
            boolean saturday
        ) {
            return Board.builder()
                .store(store)
                .title("title")
                .price(1000)
                .status(true)
                .profile("profile")
                .purchaseUrl("purchaseUrl")
                .view(1)
                .sunday(sunday)
                .monday(monday)
                .tuesday(tuesday)
                .wednesday(wednesday)
                .thursday(thursday)
                .friday(friday)
                .saturday(saturday)
                .isDeleted(sunday)
                .build();
        }

        private Store storeGenerator() {
            return Store.builder()
                .identifier("identifier")
                .name("name")
                .introduce("introduce")
                .profile("profile")
                .isDeleted(false)
                .build();
        }

        private Product productGenerator(
            Board board,
            boolean glutenFreeTag,
            boolean highProteinTag,
            boolean sugarFreeTag,
            boolean veganTag,
            boolean ketogenicTag
        ) {
            return Product.builder()
                .board(board)
                .title("title")
                .price(1000)
                .category(Category.BREAD)
                .glutenFreeTag(glutenFreeTag)
                .highProteinTag(highProteinTag)
                .sugarFreeTag(sugarFreeTag)
                .veganTag(veganTag)
                .ketogenicTag(ketogenicTag)
                .build();
        }

    }

    @Nested
    @DisplayName("폴더 내 게시글을 조회하는 API test")
    class BoardInFolderApiTest {

        private static final boolean INGREDIENT_TRUE = true;
        private static final boolean INGREDIENT_FALSE = false;

        Member member;
        Board createdBoard;
        Product product;
        Store store;
        WishListFolder wishListFolder;

        @BeforeEach
        void setup() {
            member = MemberFixture.createKakaoMember();
            member = memberService.getFirstJoinedMember(member);
            store = StoreFixture.storeGenerator();
            store = storeRepository.save(store);

            wishListFolder = wishListFolderRepository.findByMemberId(member.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));

            createdBoard = BoardFixture.randomBoardWithPrice(store, 1000);
            createdBoard = boardRepository.save(createdBoard);

            product = productWIthKetogenicYogurt(createdBoard);
            productRepository.save(product);

            BoardStatistic boardStatistic = RankingFixture.newRanking(createdBoard);
            rankingRepository.save(boardStatistic);

            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));
        }

        @Test
        @DisplayName("정상적으로 폴더 안의 게시글을 조회할 수 있다.")
        void getBoardInFolder() throws Exception {
            //given
            String authentication = getAuthentication(member);

            mockMvc.perform(get("/api/v1/boards/folders/" + wishListFolder.getId())
                    .header(HttpHeaders.AUTHORIZATION, authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.result.content[0].boardId").value(createdBoard.getId()))
                .andExpect(jsonPath("$.result.content[0].storeId").value(store.getId()))
                .andExpect(jsonPath("$.result.content[0].storeName").value(store.getName()))
                .andExpect(jsonPath("$.result.content[0].thumbnail").value(store.getProfile()))
                .andExpect(jsonPath("$.result.content[0].title").value(createdBoard.getTitle()))
                .andExpect(jsonPath("$.result.content[0].price").value(createdBoard.getPrice()))
                .andExpect(jsonPath("$.result.content[0].isWished").value(true))
                .andExpect(jsonPath("$.result.content[0].isBundled").value(false))
                .andExpect(jsonPath("$.result.content[0].tags[0]").value("ketogenic"))
                .andExpect(jsonPath("$.result.nextCursor").value(-1))
                .andExpect(jsonPath("$.result.hasNext").value(false))
                .andExpect(jsonPath("$.result.cursorScore").value(nullValue()));
        }

        private String getAuthentication(Member member) {
            String token = tokenProvider.generateToken(member, Duration.ofMinutes(1));
            return "Bearer " + token;
        }

        private Product productWIthKetogenicYogurt(Board board) {
            return Product.builder()
                .board(board)
                .title("test")
                .veganTag(INGREDIENT_FALSE)
                .ketogenicTag(INGREDIENT_TRUE)
                .sugarFreeTag(INGREDIENT_FALSE)
                .highProteinTag(INGREDIENT_FALSE)
                .glutenFreeTag(INGREDIENT_FALSE)
                .category(Category.YOGURT)
                .build();
        }

    }

}


