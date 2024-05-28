package com.bbangle.bbangle.board.repository.folder.query;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
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

    @Nested
    @DisplayName("정상적인 DAO를 만드는지 확인하는 테스트")
    class CreateSuccessfulDao {

        private static final Long DEFAULT_FOLDER_ID = 0L;

        Member member;
        WishListFolder wishListFolder;
        Long savedId;
        Product product;
        Product product2;
        Board createdBoard;
        Store store;

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
            Assertions.assertThat(actual.boardId()).isEqualTo(boardResponseDao.boardId());
            Assertions.assertThat(actual.storeId()).isEqualTo(boardResponseDao.storeId());
            Assertions.assertThat(actual.storeName()).isEqualTo(boardResponseDao.storeName());
            Assertions.assertThat(actual.thumbnail()).isEqualTo(boardResponseDao.thumbnail());
            Assertions.assertThat(actual.price()).isEqualTo(boardResponseDao.price());
            Assertions.assertThat(actual.title()).isEqualTo(boardResponseDao.title());
            Assertions.assertThat(actual.category()).isEqualTo(boardResponseDao.category());
            Assertions.assertThat(actual.tagsDao().veganTag()).isEqualTo(boardResponseDao.tagsDao().veganTag());
            Assertions.assertThat(actual.tagsDao().highProteinTag()).isEqualTo(boardResponseDao.tagsDao().highProteinTag());
            Assertions.assertThat(actual.tagsDao().ketogenicTag()).isEqualTo(boardResponseDao.tagsDao().ketogenicTag());
            Assertions.assertThat(actual.tagsDao().sugarFreeTag()).isEqualTo(boardResponseDao.tagsDao().sugarFreeTag());
            Assertions.assertThat(actual.tagsDao().glutenFreeTag()).isEqualTo(boardResponseDao.tagsDao().glutenFreeTag());

            BoardResponseDao actual2 = boards.get(1);
            Assertions.assertThat(actual2.boardId()).isEqualTo(boardResponseDao2.boardId());
            Assertions.assertThat(actual2.storeId()).isEqualTo(boardResponseDao2.storeId());
            Assertions.assertThat(actual2.storeName()).isEqualTo(boardResponseDao2.storeName());
            Assertions.assertThat(actual2.thumbnail()).isEqualTo(boardResponseDao2.thumbnail());
            Assertions.assertThat(actual2.price()).isEqualTo(boardResponseDao2.price());
            Assertions.assertThat(actual2.title()).isEqualTo(boardResponseDao2.title());
            Assertions.assertThat(actual2.category()).isEqualTo(boardResponseDao2.category());
            Assertions.assertThat(actual2.tagsDao().veganTag()).isEqualTo(boardResponseDao2.tagsDao().veganTag());
            Assertions.assertThat(actual2.tagsDao().highProteinTag()).isEqualTo(boardResponseDao2.tagsDao().highProteinTag());
            Assertions.assertThat(actual2.tagsDao().ketogenicTag()).isEqualTo(boardResponseDao2.tagsDao().ketogenicTag());
            Assertions.assertThat(actual2.tagsDao().sugarFreeTag()).isEqualTo(boardResponseDao2.tagsDao().sugarFreeTag());
            Assertions.assertThat(actual2.tagsDao().glutenFreeTag()).isEqualTo(boardResponseDao2.tagsDao().glutenFreeTag());

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

    private static Product productWithVeganSugarFreeHighProteinBread(Board board) {
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

    private static Product productWIthKetogenicYogurt(Board board) {
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
