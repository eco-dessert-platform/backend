package com.bbangle.bbangle;

import static java.util.Collections.emptyMap;

import com.bbangle.bbangle.analytics.service.AnalyticsService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardImgRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardRepository;
import com.bbangle.bbangle.board.service.BoardDetailService;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.board.service.ProductService;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.ranking.UpdateBoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.image.repository.ImageRepository;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.member.service.MemberService;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.bbangle.bbangle.push.service.FcmService;
import com.bbangle.bbangle.push.service.PushService;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.service.StoreService;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import com.bbangle.bbangle.wishlist.service.WishListStoreService;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final String STATISTIC_UPDATE_LIST = "STATISTIC_UPDATE_LIST";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext context;
    @Autowired
    protected JPAQueryFactory queryFactory;
    @Autowired
    protected MemberService memberService;
    @Autowired
    protected StoreService storeService;
    @Autowired
    protected BoardService boardService;
    @Autowired
    protected ProductService productService;
    @Autowired
    protected WishListStoreService wishListStoreService;
    @Autowired
    protected AnalyticsService analyticsService;
    @Autowired
    protected WishListBoardService wishListBoardService;
    @Autowired
    protected PushService pushService;
    @Autowired
    protected FcmService fcmService;
    @Autowired
    protected BoardDetailService boardDetailService;
    @Autowired
    protected BoardRepository boardRepository;
    @Autowired
    protected StoreRepository storeRepository;
    @Autowired
    protected BoardStatisticRepository boardStatisticRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected ReviewRepository reviewRepository;
    @Autowired
    protected BoardImgRepository boardImgRepository;
    @Autowired
    protected WishListFolderRepository wishListFolderRepository;
    @Autowired
    protected WishListBoardRepository wishListBoardRepository;
    @Autowired
    protected WishListStoreRepository wishListStoreRepository;
    @Autowired
    protected BoardDetailRepository boardDetailRepository;
    @Autowired
    protected RecommendationSimilarBoardRepository recommendationSimilarBoardRepository;
    @Autowired
    protected NotificationRepository notificationRepository;
    @Autowired
    protected PushRepository pushRepository;
    @Autowired
    protected ImageRepository imageRepository;
    @Autowired
    protected BoardPreferenceStatisticRepository preferenceStatisticRepository;
    @Autowired
    protected UpdateBoardStatistic updateBoardStatistic;
    @Autowired
    @Qualifier("updateRedisTemplate")
    protected RedisTemplate<String, Object> updateTemplate;

    @BeforeEach
    void before() {
        // 이거 없으면 데이터가 꼬여서 테스트 너무 힘들어서 넣었습니다 살려주세요
        storeRepository.deleteAllInBatch();
        boardStatisticRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        boardImgRepository.deleteAllInBatch();
        wishListFolderRepository.deleteAllInBatch();
        wishListBoardRepository.deleteAllInBatch();
        wishListStoreRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        pushRepository.deleteAllInBatch();
        imageRepository.deleteAllInBatch();
        preferenceStatisticRepository.deleteAllInBatch();
        updateTemplate.delete(STATISTIC_UPDATE_LIST);
    }

    protected FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();

    /**
     * NOTE: param 에 변경하고자 하는 필드명 : 값 형식으로 주입하면 변경되어 insert 됨
     */

    protected WishListFolder fixtureWishListFolder(Map<String, Object> params) {
        ArbitraryBuilder<WishListFolder> builder = fixtureMonkey.giveMeBuilder(
                WishListFolder.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("member")) {
            Member member = fixtureMember(emptyMap());
            builder = builder.set("member", member); // store wishlist가 없으면 에러나서 추가
        }

        return wishListFolderRepository.save(builder.sample());
    }

    protected Member fixtureMember(Map<String, Object> params) {
        ArbitraryBuilder<Member> builder = fixtureMonkey.giveMeBuilder(Member.class);
        setBuilderParams(params, builder);

        return memberRepository.save(builder.sample());
    }

    protected Store fixtureStore(Map<String, Object> params) {
        ArbitraryBuilder<Store> builder = fixtureMonkey.giveMeBuilder(Store.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("wishListStoreList")) {
            builder = builder.set("wishListStores", null); // store wishlist가 없으면 에러나서 추가
        }

        return storeRepository.save(builder.sample());
    }

    protected Product fixtureProduct(Map<String, Object> params) {
        ArbitraryBuilder<Product> builder = fixtureMonkey.giveMeBuilder(Product.class);
        setBuilderParams(params, builder);
        builder = builder.set("id", null);

        if (!params.containsKey("board")) {
            builder = builder.set("board", null); // 양방향 참조 관계이므로 board 가 있으면 에러나서 추가
        }

        return builder.sample();
    }

    protected BoardDetail fixtureBoardDetail(Map<String, Object> params) {
        ArbitraryBuilder<BoardDetail> builder = fixtureMonkey.giveMeBuilder(BoardDetail.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("board")) {
            builder = builder.set("board", null); // board 가 있으면 에러나서 추가
        }

        BoardDetail sample = builder.sample();
        return boardDetailRepository.save(sample);
    }

    protected ProductImg fixtureBoardImage(Map<String, Object> params) {
        ArbitraryBuilder<ProductImg> builder = fixtureMonkey.giveMeBuilder(ProductImg.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("board")) {
            builder = builder.set("board", null); // board 가 있으면 에러나서 추가
        }

        ProductImg sample = builder.sample();
        return boardImgRepository.save(sample);
    }

    protected BoardStatistic fixtureRanking(Map<String, Object> params) {
        ArbitraryBuilder<BoardStatistic> builder = fixtureMonkey.giveMeBuilder(
                BoardStatistic.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("board")) {
            Board board = fixtureBoard(emptyMap());
            builder = builder.set("board", board);
        }

        return boardStatisticRepository.save(builder.sample());
    }

    protected Board fixtureBoard(Map<String, Object> params) {
        ArbitraryBuilder<Board> builder = fixtureMonkey.giveMeBuilder(Board.class);
        setBuilderParams(params, builder);
        builder = builder.set("id", null);

        if (!params.containsKey("store")) {
            Store store = fixtureStore(emptyMap());
            builder = builder.set("store", store);
        }

        if (!params.containsKey("productList")) {
            List<Product> products = Collections.singletonList(fixtureProduct(emptyMap()));
            builder = builder.set("products", products);
        }

        Board sample = builder.sample();
        sample.getProducts().forEach(product -> product.setBoard(sample));
        Board board = boardRepository.save(sample);

        return board;
    }


    protected Review fixtureReview(Map<String, Object> params) {
        ArbitraryBuilder<Review> builder = fixtureMonkey.giveMeBuilder(Review.class);
        setBuilderParams(params, builder);

        return reviewRepository.save(builder.sample());
    }

    private void setBuilderParams(Map<String, Object> params, ArbitraryBuilder builder) {
        for (Entry<String, Object> entry : params.entrySet()) {
            builder = builder.set(entry.getKey(), entry.getValue());
        }
    }

}
