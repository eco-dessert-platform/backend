package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishListBoard.domain.WishlistProduct;
import com.bbangle.bbangle.wishListBoard.repository.WishListProductRepository;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
@Rollback
public class BoardRepositoryImplTest {


    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BoardImgRepository boardImgRepository;

    @Autowired
    private WishListFolderRepository wishListFolderRepository;

    @Autowired
    private WishListProductRepository wishListProductRepository;

    @Autowired
    private WishListStoreRepository wishListStoreRepository;

    @BeforeEach
    public void saveData() {
        createData(15);
    }

    @AfterEach
    void afterEach() {
        this.entityManager
            .createNativeQuery("ALTER TABLE store ALTER COLUMN `id` RESTART WITH 1")
            .executeUpdate();

        this.entityManager
            .createNativeQuery("ALTER TABLE product_board ALTER COLUMN `id` RESTART WITH 1")
            .executeUpdate();

        this.entityManager
                .createNativeQuery("ALTER TABLE member ALTER COLUMN `id` RESTART WITH 2")
                .executeUpdate();

        this.entityManager
                .createNativeQuery("ALTER TABLE wishlist_folder ALTER COLUMN `id` RESTART WITH 1")
                .executeUpdate();

        this.entityManager
                .createNativeQuery("ALTER TABLE wishlist_product ALTER COLUMN `id` RESTART WITH 1")
                .executeUpdate();

        this.entityManager
                .createNativeQuery("ALTER TABLE wishlist_store ALTER COLUMN `id` RESTART WITH 1")
                .executeUpdate();
    }

    @Test
    public void getBoardResponseDtoTest(){

        Long memberId = 2L;
        Long storeId = 1L;
        Long boardId = 1L;

        Member member = Member.builder().id(memberId).email("dd@ex.com").nickname("test").name("testName").birth("99999").phone("01023299893").build();
        Store store = Store.builder().id(storeId).build();
        Board board = Board.builder().id(boardId).build();
        WishlistFolder wishlistFolder = WishlistFolder.builder().folderName("Test").member(member).build();
        WishlistProduct wishlistProduct = WishlistProduct.builder().board(board)
                .memberId(memberId)
                .wishlistFolder(wishlistFolder)
                .build();
        WishlistStore wishlistStore = WishlistStore.builder().store(store).member(member).build();

        memberRepository.save(member);
        wishListFolderRepository.save(wishlistFolder);
        wishListProductRepository.save(wishlistProduct);
        wishListStoreRepository.save(wishlistStore);

        var result = boardRepository.getBoardDetailResponse(memberId, boardId);

        Assertions.assertEquals(1L,result.store().storeId());
        Assertions.assertEquals("RAWSOME",result.store().storeName());
        Assertions.assertEquals("비건 베이커리 로썸 비건빵",result.board().title());
        assertThat(List.of("glutenFree", "sugarFree", "vegan"),containsInAnyOrder(result.board().tags().toArray()));

        boolean isProduct1 = false;
        boolean isProduct2 = false;
        boolean isProduct3 = false;
        for (ProductDto productDto:
            result.board().products()) {
                switch (productDto.title()){
                    case "콩볼":
                        assertThat(List.of("glutenFree", "sugarFree", "vegan"),containsInAnyOrder(productDto.tags().toArray()));
                        isProduct1 = true;
                        break;
                    case "카카모카":
                        assertThat(List.of("glutenFree", "vegan"),containsInAnyOrder(productDto.tags().toArray()));
                        isProduct2 = true;
                        break;
                    case "로미넛쑥":
                        assertThat(List.of("glutenFree", "sugarFree", "vegan"),containsInAnyOrder(productDto.tags().toArray()));
                        isProduct3 = true;
                        break;
                }
        }

        Assertions.assertEquals(true, isProduct1);
        Assertions.assertEquals(true, isProduct2);
        Assertions.assertEquals(true, isProduct3);
    }

    @Test
    @DisplayName("Wished Product 테이블에 값들이 존재해도, 내 데이터가 아니면 isWished는 false가 된다")
    public void getBoardResponseDtoLikeTest(){
        Long memberId = 3L;
        Long storeId = 1L;
        Long boardId = 1L;

        createLikeData(2L, storeId + 1L, boardId + 1L);
        for (int i = 0; i < 10; i++) {
            memberId ++;
            storeId ++;
            boardId ++;
            createLikeData(memberId, storeId, boardId);
        }

        Long testMemberId = 2L;
        var result = boardRepository.getBoardDetailResponse(testMemberId, boardId);
    
        Assertions.assertEquals(false, result.store().isWished(), "스토어 Like가 true 입니다");
        Assertions.assertEquals(false, result.board().isWished(), "보드 Like가 true 입니다");
    }

    @Test
    @DisplayName("Wished Productm isWished는 true가 된다")
    public void getBoardLikeTrueTest(){
        Long memberId = 2L;
        Long storeId = 1L;
        Long boardId = 1L;

        createLikeData(memberId, storeId, boardId);

        Long testMemberId = 2L;
        var result = boardRepository.getBoardDetailResponse(testMemberId, boardId);

        Assertions.assertEquals(true, result.store().isWished(), "스토어 Like가 true 입니다");
        Assertions.assertEquals(true, result.board().isWished(), "보드 Like가 true 입니다");
    }

    private void createData(int count) {
        for (int i = 0; i < count; i++) {
            var store = Store.builder()
                .identifier("7962401222")
                .name("RAWSOME")
                .profile(
                    "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                .introduce("건강을 먹다-로썸")
                .build();

            var board = Board.builder()
                .store(store)
                .title("비건 베이커리 로썸 비건빵")
                .price(5400)
                .status(true)
                .profile(
                    "https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                .purchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                .view(100)
                .sunday(false)
                .monday(false)
                .tuesday(false)
                .wednesday(false)
                .thursday(true)
                .sunday(false)
                .build();

            var product1 = Product.builder()
                .board(board)
                .title("콩볼")
                .price(3600)
                .category(Category.COOKIE)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(true)
                .veganTag(true)
                .ketogenicTag(false)
                .build();

            var product2 = Product.builder()
                .board(board)
                .title("카카모카")
                .price(5000)
                .category(Category.BREAD)
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(true)
                .ketogenicTag(false)
                .build();

            var product3 = Product.builder()

                    .board(board)
                    .title("로미넛쑥")
                    .price(5000)
                    .category(Category.BREAD)
                    .glutenFreeTag(true)
                    .highProteinTag(false)
                    .sugarFreeTag(true)
                    .veganTag(true)
                    .ketogenicTag(false)
                    .build();


            var boardImg = ProductImg.builder()
                    .board(board)
                            .url("www.naver.com")
                            .build();

            var boardImg2 = ProductImg.builder()
                    .board(board)
                    .url("www.naver.com")
                    .build();



            storeRepository.save(store);
            boardRepository.save(board);
            boardImgRepository.save(boardImg);
            boardImgRepository.save(boardImg2);
            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
        }
    }


    private void createLikeData(Long memberId, Long storeId, Long boardId){
        Member member = Member.builder().id(memberId).email("dd@ex.com").nickname("test").name("testName").birth("99999").phone("01023299893").build();
        Store store = Store.builder().id(storeId).build();
        Board board = Board.builder().id(boardId).build();
        WishlistFolder wishlistFolder = WishlistFolder.builder().folderName("Test").member(member).build();
        WishlistProduct wishlistProduct = WishlistProduct.builder().board(board)
                .memberId(memberId)
                .wishlistFolder(wishlistFolder)
                .build();
        WishlistStore wishlistStore = WishlistStore.builder().store(store).member(member).build();

        memberRepository.save(member);
        wishListFolderRepository.save(wishlistFolder);
        wishListProductRepository.save(wishlistProduct);
        wishListStoreRepository.save(wishlistStore);
    }

}
