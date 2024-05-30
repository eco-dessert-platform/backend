package com.bbangle.bbangle.board.repository.folder.query;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WishListRecentBoardQueryProviderTest extends AbstractIntegrationTest {

    private static final boolean INGREDIENT_TRUE = true;
    private static final boolean INGREDIENT_FALSE = false;

    Member member;
    WishListFolder wishListFolder;
    Long firstSavedId;
    Long lastSavedId;
    Long savedId;
    Product product;
    Product product2;
    Board createdBoard;
    Store store;

    @BeforeEach
    void generalSetUp(){
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
        store = StoreFixture.storeGenerator();
        store = storeRepository.save(store);

        wishListFolder = wishListFolderRepository.findByMemberId(member.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));
    }

    @Nested
    @DisplayName("정상적인 DAO를 만드는지 확인하는 테스트")
    class CreateSuccessfulDao {

        @BeforeEach
        void setup() {
            createdBoard = BoardFixture.randomBoard(store);
            createdBoard = boardRepository.save(createdBoard);
            savedId = createdBoard.getId();
            product = productWithVeganSugarFreeHighProteinBread(createdBoard);
            productRepository.save(product);
            product2 = productWIthKetogenicYogurt(createdBoard);
            productRepository.save(product2);
            Ranking ranking = RankingFixture.newRanking(createdBoard);
            rankingRepository.save(ranking);
            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));

        }

        @Test
        @DisplayName("정상적으로 board와 product 정보를 받아온다.")
        void getBoardResponseDaoList() {
            //given, when
            List<BoardResponseDao> boards = new WishListRecentBoardQueryProvider(queryFactory,
                new BooleanBuilder(),
                FolderBoardSortType.WISHLIST_RECENT.getOrderSpecifier(),
                wishListFolder).getBoards();

            BoardResponseDao boardResponseDao = getBoardResponseDao(createdBoard, product, store);
            BoardResponseDao boardResponseDao2 = getBoardResponseDao(createdBoard, product2, store);

            //then
            BoardResponseDao actual = boards.get(0);
            assertThat(actual.boardId()).isEqualTo(boardResponseDao.boardId());
            assertThat(actual.storeId()).isEqualTo(boardResponseDao.storeId());
            assertThat(actual.storeName()).isEqualTo(boardResponseDao.storeName());
            assertThat(actual.thumbnail()).isEqualTo(boardResponseDao.thumbnail());
            assertThat(actual.price()).isEqualTo(boardResponseDao.price());
            assertThat(actual.title()).isEqualTo(boardResponseDao.title());
            assertThat(actual.category()).isEqualTo(boardResponseDao.category());
            assertThat(actual.tagsDao().veganTag()).isEqualTo(boardResponseDao.tagsDao().veganTag());
            assertThat(actual.tagsDao().highProteinTag()).isEqualTo(boardResponseDao.tagsDao().highProteinTag());
            assertThat(actual.tagsDao().ketogenicTag()).isEqualTo(boardResponseDao.tagsDao().ketogenicTag());
            assertThat(actual.tagsDao().sugarFreeTag()).isEqualTo(boardResponseDao.tagsDao().sugarFreeTag());
            assertThat(actual.tagsDao().glutenFreeTag()).isEqualTo(boardResponseDao.tagsDao().glutenFreeTag());

            BoardResponseDao actual2 = boards.get(1);
            assertThat(actual2.boardId()).isEqualTo(boardResponseDao2.boardId());
            assertThat(actual2.storeId()).isEqualTo(boardResponseDao2.storeId());
            assertThat(actual2.storeName()).isEqualTo(boardResponseDao2.storeName());
            assertThat(actual2.thumbnail()).isEqualTo(boardResponseDao2.thumbnail());
            assertThat(actual2.price()).isEqualTo(boardResponseDao2.price());
            assertThat(actual2.title()).isEqualTo(boardResponseDao2.title());
            assertThat(actual2.category()).isEqualTo(boardResponseDao2.category());
            assertThat(actual2.tagsDao().veganTag()).isEqualTo(boardResponseDao2.tagsDao().veganTag());
            assertThat(actual2.tagsDao().highProteinTag()).isEqualTo(boardResponseDao2.tagsDao().highProteinTag());
            assertThat(actual2.tagsDao().ketogenicTag()).isEqualTo(boardResponseDao2.tagsDao().ketogenicTag());
            assertThat(actual2.tagsDao().sugarFreeTag()).isEqualTo(boardResponseDao2.tagsDao().sugarFreeTag());
            assertThat(actual2.tagsDao().glutenFreeTag()).isEqualTo(boardResponseDao2.tagsDao().glutenFreeTag());

        }

        private BoardResponseDao getBoardResponseDao(Board board, Product product, Store store) {
            return new BoardResponseDao(board.getId(),
                store.getId(),
                store.getName(),
                store.getProfile(),
                board.getTitle(),
                board.getPrice(),
                product.getCategory(),
                product.isGlutenFreeTag(),
                product.isHighProteinTag(),
                product.isSugarFreeTag(),
                product.isVeganTag(),
                product.isKetogenicTag());
        }

    }

    @Nested
    @DisplayName("위시리스트에 담은 순으로 BoardDao를 정상적으로 담는다")
    class BoardInFolderWithWishListRecentOrder{

        @BeforeEach
        void setup(){
            for (int i = 0; i < 12; i++) {
                Board createdBoard = BoardFixture.randomBoardWithPrice(store, i * 1000);
                createdBoard = boardRepository.save(createdBoard);
                if (i == 0) {
                    firstSavedId = createdBoard.getId();
                }
                if (i == 11) {
                    lastSavedId = createdBoard.getId();
                }
                Product product = ProductFixture.randomProduct(createdBoard);
                Product product2 = ProductFixture.randomProduct(createdBoard);
                productRepository.save(product);
                productRepository.save(product2);
                Ranking ranking = RankingFixture.newRanking(createdBoard);
                rankingRepository.save(ranking);
                wishListBoardService.wish(member.getId(), createdBoard.getId(),
                    new WishListBoardRequest(wishListFolder.getId()));
            }
        }

        @Test
        @DisplayName("정상적으로 위시리스트에 담은 순으로 DAO를 조회한다.")
        void getBoardResponseDaoWithWishListRecent(){
            //given, when
            List<BoardResponseDao> boardResponseDaoList = new WishListRecentBoardQueryProvider(
                queryFactory,
                new BooleanBuilder(),
                FolderBoardSortType.WISHLIST_RECENT.getOrderSpecifier(),
                wishListFolder)
                .getBoards();

            //then
            assertThat(boardResponseDaoList).hasSize(22);
            assertThat(boardResponseDaoList.stream().findFirst().get().boardId()).isEqualTo(lastSavedId);
            for(long i = lastSavedId; i <= firstSavedId; i++){
                final long finalizedId = i;
                assertThat(boardResponseDaoList
                    .stream()
                    .filter(dao -> dao.boardId().equals(finalizedId))
                    .count()).isEqualTo(2);
            }
        }
    }

    private Product productWithVeganSugarFreeHighProteinBread(Board board) {
        return Product.builder()
            .board(board)
            .title("test")
            .veganTag(INGREDIENT_TRUE)
            .ketogenicTag(INGREDIENT_FALSE)
            .sugarFreeTag(INGREDIENT_TRUE)
            .highProteinTag(INGREDIENT_TRUE)
            .glutenFreeTag(INGREDIENT_FALSE)
            .category(Category.BREAD)
            .build();
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
