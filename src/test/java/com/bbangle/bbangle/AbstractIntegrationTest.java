package com.bbangle.bbangle;

import com.bbangle.bbangle.analytics.service.AnalyticsService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardImgRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.member.service.MemberService;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.bbangle.bbangle.notification.service.NotificationService;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.service.StoreService;
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
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static java.util.Collections.emptyMap;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

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
    protected WishListStoreService wishListStoreService;
    @Autowired
    protected AnalyticsService analyticsService;
    @Autowired
    protected WishListBoardService wishListBoardService;
    @Autowired
    protected BoardRepository boardRepository;
    @Autowired
    protected StoreRepository storeRepository;
    @Autowired
    protected RankingRepository rankingRepository;
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
    protected NotificationRepository notificationRepository;

    @BeforeEach
    void before() {
        // 이거 없으면 데이터가 꼬여서 테스트 너무 힘들어서 넣었습니다 살려주세요
        storeRepository.deleteAllInBatch();
        rankingRepository.deleteAllInBatch();
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
    }

    protected FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    /**
     * NOTE: param 에 변경하고자 하는 필드명 : 값 형식으로 주입하면 변경되어 insert 됨
     */
    protected Store fixtureStore(Map<String, Object> params) {
        ArbitraryBuilder<Store> builder = fixtureMonkey.giveMeBuilder(Store.class);
        setBuilderParams(params, builder);

        return storeRepository.save(builder.sample());
    }

    protected Product fixtureProduct(Map<String, Object> params) {
        ArbitraryBuilder<Product> builder = fixtureMonkey.giveMeBuilder(Product.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("board")) {
            builder = builder.set("board", null); // board 가 있으면 에러나서 추가
        }

        Product sample = builder.sample();
        return productRepository.save(sample);
    }

    protected Ranking fixtureRanking(Map<String, Object> params) {
        ArbitraryBuilder<Ranking> builder = fixtureMonkey.giveMeBuilder(Ranking.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("board")) {
            Board board = fixtureBoard(emptyMap());
            builder = builder.set("board", board);
        }

        return rankingRepository.save(builder.sample());
    }

    protected Board fixtureBoard(Map<String, Object> params) {
        ArbitraryBuilder<Board> builder = fixtureMonkey.giveMeBuilder(Board.class);
        setBuilderParams(params, builder);

        if (!params.containsKey("store")) {
            Store store = fixtureStore(emptyMap());
            builder = builder.set("store", store);
        }

        List<Product> products;
        if (!params.containsKey("productList")) {
            products = Collections.singletonList(fixtureProduct(emptyMap()));
            builder = builder.set("productList", products);
        } else {
            products = (List<Product>) params.get("productList");
        }

        Board board = boardRepository.save(builder.sample());

        // product 에 다시 board 를 세팅해줘야 조인이 됨
        productRepository.saveAll(
            products.stream().peek(it -> it.setBoard(board)).collect(Collectors.toList())
        );

        return board;
    }

    private void setBuilderParams(Map<String, Object> params, ArbitraryBuilder builder) {
        for (Entry<String, Object> entry : params.entrySet()) {
            builder = builder.set(entry.getKey(), entry.getValue());
        }
    }
}
