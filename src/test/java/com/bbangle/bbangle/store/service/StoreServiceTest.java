package com.bbangle.bbangle.store.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponse;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StoreServiceTest extends AbstractIntegrationTest {

    private static final Long NULL_CURSOR = null;
    private static final Long NULL_MEMBER_ID = null;

    private final String TEST_TITLE = "TestTitle";

    private Member member;

    @BeforeEach
    void setup() {
        wishListStoreRepository.deleteAll();
        wishListFolderRepository.deleteAll();
        storeRepository.deleteAll();
        memberRepository.deleteAll();

        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
    }

    @Test
    @DisplayName("스토어 상세페이지 - 스토어 조회 기능 : 게시판 아이디로 스토어를 조회할 수 있다")
    void getBoardDetailResponseTest() {
        Store store = fixtureStore(Map.of("name", TEST_TITLE));
        Board board = fixtureBoard(Map.of("store", store));
        Long memberId = null;

        StoreDto storeDto = storeService.getStoreDtoByBoardId(memberId, board.getId());

        AssertionsForClassTypes.assertThat(storeDto.getId()).isEqualTo(store.getId());
        AssertionsForClassTypes.assertThat(storeDto.getTitle()).isEqualTo(TEST_TITLE);
    }

    @Test
    @DisplayName("상품 상세페이지 내 store 조회 정상 확인")
    void getStoreDetailResponse() {
        //given
        Store store = StoreFixture.storeGenerator();
        storeRepository.save(store);

        wishListStoreRepository.save(WishListStore.builder()
            .store(store)
            .member(member)
            .build()
        );

        StoreResponse result = storeService
            .getStoreResponse(member.getId(), store.getId());

        //then
        assertThat(result.storeId()).isEqualTo(store.getId());
        assertThat(result.storeProfile()).isEqualTo(store.getProfile());
        assertThat(result.storeTitle()).isEqualTo(store.getName());
        assertThat(result.isWished()).isTrue();
    }

    @Nested
    @DisplayName("store 조회 서비스 로직 테스트")
    class GetStoreList {

        @BeforeEach
        void saveStoreList() {
            for (int i = 0; i < 30; i++) {
                Store store = StoreFixture.storeGenerator();
                storeRepository.save(store);
            }
        }

        @Test
        @DisplayName("정상적으로 첫 페이지를 조회한다")
        void getFirstPage() {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> list = storeService.getList(
                NULL_CURSOR,
                NULL_MEMBER_ID
            );
            List<StoreResponseDto> content = list.getContent();
            Boolean hasNext = list.getHasNext();

            //then
            assertThat(content).hasSize(20);
            assertThat(hasNext).isTrue();
        }

        @Test
        @DisplayName("정상적으로 마지막 페이지를 조회한다")
        void getLastPage() {
            //given
            StoreCustomPage<List<StoreResponseDto>> firstPage = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            Long nextCursor = firstPage.getNextCursor();

            //when
            StoreCustomPage<List<StoreResponseDto>> lastPage = storeService.getList(nextCursor,
                NULL_MEMBER_ID);

            List<StoreResponseDto> lastPageContent = lastPage.getContent();
            Boolean lastPageHasNext = lastPage.getHasNext();
            Long lastPageNextCursor = lastPage.getNextCursor();

            //then
            assertThat(lastPageContent).hasSize(10);
            assertThat(lastPageHasNext).isFalse();
        }

        @Test
        @DisplayName("마지막 자료를 조회하는 경우 nextCursor는 -1을 가리킨다")
        void getLastContent() {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> firstPage = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            Long nextCursor = firstPage.getNextCursor();
            StoreCustomPage<List<StoreResponseDto>> lastPage = storeService.getList(nextCursor,
                NULL_MEMBER_ID);
            Long lastContentCursor = lastPage.getNextCursor();

            StoreCustomPage<List<StoreResponseDto>> noContent = storeService.getList(
                lastContentCursor,
                NULL_MEMBER_ID);

            //then
            assertThat(noContent.getContent()).hasSize(0);
            assertThat(noContent.getNextCursor()).isEqualTo(-1L);
        }

        @Test
        @DisplayName("좋아요를 누른 store는 isWished가 true로 반환된다")
        void getWishedContent() throws Exception {
            //given, when
            StoreCustomPage<List<StoreResponseDto>> before = storeService.getList(NULL_CURSOR,
                NULL_MEMBER_ID);
            StoreResponseDto first = before.getContent()
                .stream()
                .findFirst()
                .orElseThrow(Exception::new);
            wishListStoreService.save(member.getId(), first.getStoreId());

            //then
            StoreResponseDto wishedContent = storeService.getList(NULL_CURSOR, member.getId())
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(Exception::new);
            assertThat(wishedContent.getIsWished()).isTrue();
        }
    }

}
