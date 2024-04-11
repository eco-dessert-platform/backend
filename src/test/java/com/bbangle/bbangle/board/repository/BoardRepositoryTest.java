package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.testutil.TestFactoryManager;
import com.bbangle.bbangle.member.repository.MemberRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.testutil.model.*;
import com.bbangle.bbangle.wishListBoard.repository.WishListProductRepository;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@Rollback
public class BoardRepositoryTest {
    private final TestFactoryManager testFactoryManager;
    private final Long memberId = 2L;

    public BoardRepositoryTest(
            @Autowired MemberRepository memberRepository,
            @Autowired StoreRepository storeRepository,
            @Autowired BoardRepository boardRepository,
            @Autowired ProductRepository productRepository,
            @Autowired EntityManager entityManager,
            @Autowired BoardImgRepository boardImgRepository,
            @Autowired WishListFolderRepository wishListFolderRepository,
            @Autowired WishListProductRepository wishListProductRepository,
            @Autowired WishListStoreRepository wishListStoreRepository
    ){
        testFactoryManager = new TestFactoryManager(entityManager)
                .setTestStoreFactory(storeRepository)
                .setTestBoardFactory(boardRepository)
                .setTestProductFactory(productRepository)
                .setTestBoardImageFactory(boardImgRepository)
                .setTestMemberFactory(memberRepository)
                .setTestWishlistFolderFactory(wishListFolderRepository)
                .setTestWishlistBoardFactory(wishListProductRepository)
                .setTestWishlistStoreFactory(wishListStoreRepository);
    }

    @BeforeEach
    public void saveData() {
        createProductRelatedContent(10);
        createLikeData();
    }

    @AfterEach
    void afterEach() {
        testFactoryManager.resetAutoIncreasementAndRowData();
    }

    @Test
    @DisplayName("상품 게시물 상세보기 조회가 잘되고 있다")
    public void getProductDtosToDuplicatedTagsTest(){
        List<String> productOneTag = new ArrayList<>();
        productOneTag.add("glutenFree");
        productOneTag.add("sugarFree");

        List<String> productTwoTag = new ArrayList<>();
        productOneTag.add("highProtein");
        productOneTag.add("sugarFree");

        List<String> productThreeTag = new ArrayList<>();
        productOneTag.add("glutenFree");
        productOneTag.add("ketogenic");

        List<ProductDto> productDtos = new ArrayList<>();
        productDtos.add(ProductDto.builder().tags(productOneTag).build());
        productDtos.add(ProductDto.builder().tags(productTwoTag).build());
        productDtos.add(ProductDto.builder().tags(productThreeTag).build());

        var boardRepository = testFactoryManager.getTestBoardFactory().getRepository();
        List<String> actualDuplicatedTags = boardRepository.getProductDtosToDuplicatedTags(productDtos);

        assertThat(actualDuplicatedTags, containsInAnyOrder("glutenFree", "sugarFree", "highProtein", "ketogenic"));
        assertThat(actualDuplicatedTags, not(hasItem("vegan")));
    }

    @Test
    @DisplayName("상품 게시물 상세보기 조회가 잘되고 있다")
    public void getBoardResponseDtoTest(){
        var boardRepository = testFactoryManager.getTestBoardFactory().getRepository();
        var board = testFactoryManager.getTestBoardFactory().getTestEntity("board0");

        var boardDetailResponse = boardRepository.getBoardDetailResponse(memberId, board.getId());
        assertThat(boardDetailResponse.store().storeId(), is(1L));

        var firstBoard = boardDetailResponse.board();
        assertThat(firstBoard.boardId(), is(1L));
        assertThat(firstBoard.title(), is("비건 베이커리 로썸 비건빵"));
        assertThat(firstBoard.isBundled(), is(true));
        assertThat(firstBoard.tags(), containsInAnyOrder("glutenFree", "sugarFree", "vegan", "ketogenic"));

        var images  = firstBoard.images();
        assertThat(images.toArray(), arrayWithSize(2));

        var firstImage = images.get(0);
        assertThat(firstImage.id(), is(1L));
        assertThat(firstImage.url(), is("www.naver.com1"));

        var products = boardDetailResponse.board().products();
        assertThat(products.toArray(), arrayWithSize(3));

        var firstProduct = boardDetailResponse.board().products().get(0);
        assertThat(firstProduct.id(), is(1L));
        assertThat(firstProduct.title(), is("콩볼"));

        var firstProductTag = firstProduct.tags().stream().toArray();
        assertThat(firstProductTag, arrayContaining("glutenFree", "sugarFree", "vegan", "ketogenic"));
    }

    @Test
    @DisplayName("Wished Product 테이블에 값들이 존재해도, 내 데이터가 아니면 isWished는 false가 된다")
    public void getBoardResponseDtoLikeTest(){
        var boardRepository = testFactoryManager.getTestBoardFactory().getRepository();
        var board = testFactoryManager.getTestBoardFactory().getTestEntity("board1");
        var boardDetailResponse = boardRepository.getBoardDetailResponse(memberId, board.getId());

        assertThat(boardDetailResponse.store().isWished(), is(false));
        assertThat(boardDetailResponse.board().isWished(), is(true));
    }

    @Test
    @DisplayName("Wished Productm isWished는 true가 된다")
    public void getBoardLikeTrueTest(){
        var boardRepository = testFactoryManager.getTestBoardFactory().getRepository();
        var board = testFactoryManager.getTestBoardFactory().getTestEntity("board1");
        var boardDetailResponse = boardRepository.getBoardDetailResponse(memberId, board.getId());

        assertThat(boardDetailResponse.store().isWished(), is(false));
        assertThat(boardDetailResponse.board().isWished(), is(true));
    }

    private void createProductRelatedContent(int count) {
        for (int i = 0; i < count; i++) {
            var store = testFactoryManager.getTestStoreFactory().pushTestEntity(
                    "store" + i,
                    new TestStore().setIdentifier("7962401222")
                        .setName("RAWSOME")
                        .setProfile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fprofile.jpg?alt=media&token=26bd1435-2c28-4b85-a5aa-b325e9aac05e")
                        .getModel()
            );

            var board = testFactoryManager.getTestBoardFactory().pushTestEntity(
                    "board" + i,
                    new TestBoard(store)
                            .setBoardName("비건 베이커리 로썸 비건빵")
                            .setPrice(5400)
                            .setStatus(true)
                            .setProfile("https://firebasestorage.googleapis.com/v0/b/test-1949b.appspot.com/o/stores%2Frawsome%2Fboards%2F00000000%2F0.jpg?alt=media&token=f3d1925a-1e93-4e47-a487-63c7fc61e203")
                            .setPurchaseUrl("https://smartstore.naver.com/rawsome/products/5727069436")
                            .setView(100)
                            .setThursday(true)
                            .setSaturday(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3 - 2),
                    new TestProduct(board)
                            .setProductName("콩볼")
                            .setPrice(3600)
                            .setCategory(Category.COOKIE)
                            .setGlutenFreeTag(true)
                            .setSugarFreeTag(true)
                            .setVeganTag(true)
                            .setKetogenicTag(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3 - 1),
                    new TestProduct(board)
                            .setProductName("카카모카")
                            .setPrice(5000)
                            .setCategory(Category.BREAD)
                            .setGlutenFreeTag(true)
                            .setVeganTag(true)
                            .getModel()
            );

            testFactoryManager.getTestProductFactory().pushTestEntity(
                    "product" + (i * 3),
                    new TestProduct(board)
                            .setProductName("로미넛쑥")
                            .setPrice(5000)
                            .setCategory(Category.BREAD)
                            .setGlutenFreeTag(true)
                            .setSugarFreeTag(true)
                            .setVeganTag(true)
                            .getModel()
            );

            testFactoryManager.getTestBoardImageFactory().pushTestEntity(
                    "boardImg" + (i * 2 - 1),
                    new TestBoardImg(board)
                            .setImageUrl("www.naver.com1")
                            .getModel());

            testFactoryManager.getTestBoardImageFactory().pushTestEntity(
                    "boardImg" + (i * 2),
                    new TestBoardImg(board)
                            .setImageUrl("www.naver.com2")
                            .getModel());
        }
    }

    private void createLikeData(){
        var member = testFactoryManager.getTestMemberFactory().pushTestEntity(
                "member1",
                new TestMember()
                        .setId(memberId)
                        .getModel()
        );

        var wishlistFolder = testFactoryManager.getTestWishlistFolderFactory().pushTestEntity(
                "wishlistFolder1",
                new TestWishlistFolder(member).getModel()
        );

        var board = testFactoryManager.getTestBoardFactory().getTestEntity("board1");
        testFactoryManager.getTestWishlistBoardFactory().pushTestEntity(
                "wishlistBoard1",
                new TestWishlistBoard(wishlistFolder)
                        .setMemberId(memberId)
                        .setBoard(board)
                        .getModel()
        );

        var store = testFactoryManager.getTestStoreFactory().getTestEntity("store1");
        testFactoryManager.getTestWishlistStoreFactory().pushTestEntity(
                "wishlistStore1",
                new TestWishlistStore(store, member).getModel()
        );
    }
}
