package com.bbangle.bbangle.store.seller.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SellerStoreServiceIntegrationTest {

    @Autowired
    private SellerStoreService sellerStoreService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("판매자가 스토어 등록에 성공한다.")
    void success_register_store() {

        // given
        Seller seller = SellerFixture.defaultSellerMergeConflict();
        Store store = StoreFixture.defaultStore();

        // when
        sellerStoreService.registerStore(seller, store);

        // then
        assertThat(seller.getStore()).isEqualTo(store);
        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(store.getStatus()).isEqualTo(StoreStatus.RESERVED);
    }

    @Nested
    @DisplayName("selectStoreNameForSeller() 테스트")
    class selectStoreNameForSellerTest {

        @BeforeEach
        void setUp() {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE store").executeUpdate();
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            em.clear();

            // 검색에 걸리는 스토어명 (LIKE '%빵%') - 21개
            List<String> matchingNames = List.of(
                "빵굽는하루",
                "오늘의빵집",
                "행복한빵연구소",
                "빵다방",
                "빵의정원",
                "달콤한빵나라",
                "서울빵공작소",
                "빵작업실",
                "엄마의빵집",
                "따끈한빵하우스",
                "시골빵집",
                "동네빵집",
                "프랑스빵집",
                "착한빵집",
                "장인빵집",
                "우리빵집",
                "맛있는빵집",
                "수제빵집",
                "행복한빵집",
                "아침빵집",
                "야간빵집"
            );

            // 검색에 걸리지 않는 스토어명
            List<String> nonMatchingNames = List.of(
                "커피가좋다",
                "구름카페",
                "행복서점",
                "미소문구점",
                "호두과자집",
                "고래서점",
                "작은꽃가게",
                "달빛카페",
                "두부마켓",
                "바닐라마을"
            );

            Stream.concat(matchingNames.stream(), nonMatchingNames.stream())
                .forEach(name -> {
                    storeRepository.save(Store.createForSeller(
                        name, "test.com", "introduce", "01012345678", "01012345678",
                        "test@temp.com", "서울", "서울"
                    ));

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

            em.flush();
            em.clear();
        }

        @Nested
        @DisplayName("가게 이름 목록 조회에 성공한다.")
        class Success_SelectStoreNameForSellerTest {

            @Test
            @DisplayName("가게 이름 일부를 입력하면 해당하는 가게 이름 목록을 20개 반환한다")
            void selectStoreNameForSeller() {

                // given
                String keyword = "빵";

                // when
                CursorPagination<StoreInfo> result =
                    sellerStoreService.selectStoreNameForSeller(keyword, null);

                // then
                assertThat(result.getContent().size()).isLessThanOrEqualTo(20);

                assertThat(result.getContent())
                    .allMatch(info ->
                        info.name().replace(" ", "")
                            .contains(keyword.replace(" ", ""))
                    );
            }

            @Test
            @DisplayName("일치하는 가게 이름이 없으면 빈 목록을 반환한다")
            void with_noMatch() {

                // given
                String keyword = "NonExistent";

                // when
                var result = sellerStoreService.selectStoreNameForSeller(keyword, null);

                // then
                assertThat(result.getContent()).isEmpty();
            }

            @Test
            @DisplayName("공백있는 문자열이 넘어오면 이를 제거한다")
            void with_whitespace() {

                // given
                String keyword = "  빵  ";

                // when
                var result = sellerStoreService.selectStoreNameForSeller(keyword, null);

                // then
                assertThat(result.getContent())
                    .allMatch(info -> info.name().replace(" ", "").contains("빵"));
            }
        }

        @Nested
        @DisplayName("커서 기반 페이지네이션 테스트")
        class CursorPaginationTest {

            @Test
            @DisplayName("다음 페이지가 존재하면 hasNext와 nextCursor를 반환한다.")
            void hasNext() {

                // given
                String keyword = " 빵 ";

                // when
                CursorPagination<StoreInfo> result = sellerStoreService.selectStoreNameForSeller(keyword, null);

                // then
                assertThat(result.getHasNext()).isTrue();
                assertThat(result.getNextCursor()).isNotNull();
            }

            @Test
            @DisplayName("cursorId 이후 데이터만 조회된다.")
            void with_cursorId() {

                // given
                String keyword = " 빵 ";

                CursorPagination<StoreInfo> firstPage = sellerStoreService.selectStoreNameForSeller(keyword, null);
                Long cursorId = firstPage.getNextCursor();

                // when
                CursorPagination<StoreInfo> secondPage = sellerStoreService.selectStoreNameForSeller(keyword, cursorId);

                // then
                assertThat(secondPage.getContent())
                    .allMatch(info -> info.id() >= cursorId);
            }
        }
    }

    @Nested
    @DisplayName("createStore() 테스트")
    class CreateStoreTest {

        @Nested
        @DisplayName("등록하기 위한 스토어 생성 및 조회에 성공한다.")
        class Success_CreateStoreTest {

            @Test
            @DisplayName("기존의 스토어를 조회할 경우 이미지 파일이 없으면 기존 프로필 경로를 사용한다.")
            void with_storeId_and_without_multipartFile() {

                // given
                Store store = storeRepository.save(StoreFixture.defaultStore());
                StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
                    "store",
                    "newProfile.png",
                    "newIntroduce",
                    "01011112222",
                    "01099998888",
                    "temp@test.com",
                    "서울",
                    "123동",
                    store.getId()
                );

                // when
                Store result = sellerStoreService.createStore(request, null);

                // then
                assertThat(result.getId()).isEqualTo(store.getId());
                assertThat(result.getName()).isEqualTo(store.getName());
                assertThat(result.getProfile()).isEqualTo(request.profile());
                assertThat(result.getIntroduce()).isEqualTo(request.shortDescription());
                assertThat(result.getPhoneNumberVO().getPhoneNumber()).isEqualTo(request.phoneNumber());
                assertThat(result.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(request.subPhoneNumber());
                assertThat(result.getEmailVO().getEmail()).isEqualTo(request.email());
                assertThat(result.getOriginAddressLine()).isEqualTo(request.originAddress());
                assertThat(result.getOriginAddressDetail()).isEqualTo(request.originAddressDetail());
            }

            @Test
            @DisplayName("기존의 스토어를 조회할 경우 이미지 파일이 존재하면 업로드한 이미지 경로를 사용한다.")
            void with_storeId_and_multipartFile() {

                // given
                Store store = storeRepository.save(StoreFixture.defaultStore());
                StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
                    "store",
                    "newProfile.png",
                    "newIntroduce",
                    "01011112222",
                    "01099998888",
                    "temp@test.com",
                    "경기도",
                    "나동",
                    store.getId()
                );
                String uploadedImagePath = "cdn/new-profile.png";

                // when
                Store result = sellerStoreService.createStore(request, uploadedImagePath);

                // then
                assertThat(result.getId()).isEqualTo(store.getId());
                assertThat(result.getName()).isEqualTo(store.getName());
                assertThat(result.getProfile()).isEqualTo(uploadedImagePath);
                assertThat(result.getIntroduce()).isEqualTo(request.shortDescription());
                assertThat(result.getPhoneNumberVO().getPhoneNumber()).isEqualTo(request.phoneNumber());
                assertThat(result.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(request.subPhoneNumber());
                assertThat(result.getEmailVO().getEmail()).isEqualTo(request.email());
                assertThat(result.getOriginAddressLine()).isEqualTo(request.originAddress());
                assertThat(result.getOriginAddressDetail()).isEqualTo(request.originAddressDetail());
            }

            @Test
            @DisplayName("등록하기 위한 새로운 스토어를 생성한다.")
            void createStore() {

                // given
                StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
                    "store",
                    null,
                    "newIntroduce",
                    "01011112222",
                    "01099998888",
                    "temp@test.com",
                    "서울",
                    "123동",
                    null
                );
                String profileImage = "profile.jpg";

                // when
                Store result = sellerStoreService.createStore(request, profileImage);

                // then
                assertThat(result.getId()).isNotNull();
                assertThat(result.getName()).isEqualTo("store");
                assertThat(result.getProfile()).isEqualTo("profile.jpg");
                assertThat(result.getIntroduce()).isEqualTo("newIntroduce");
                assertThat(result.getPhoneNumberVO().getPhoneNumber()).isEqualTo("01011112222");
                assertThat(result.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo("01099998888");
                assertThat(result.getEmailVO().getEmail()).isEqualTo("temp@test.com");
                assertThat(result.getOriginAddressLine()).isEqualTo("서울");
                assertThat(result.getOriginAddressDetail()).isEqualTo("123동");
            }
        }

        @Nested
        @DisplayName("스토어 생성 및 조회에 성공한다.")
        class Fail_CreateStoreTest {

            @Test
            @DisplayName("존재하지 않는 storeId로 조회하면 예외를 던진다.")
            void storeId_notFound() {

                // given
                StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
                    "store",
                    "newProfile.png",
                    "newIntroduce",
                    "01011112222",
                    "01099998888",
                    "temp@test.com",
                    "서울",
                    "123동",
                    999L
                );

                // when & then
                Assertions.assertThatThrownBy(() -> sellerStoreService.createStore(request, null)
                    )
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
                    });
            }

            @Test
            @DisplayName("등록하기 위한 신규 스토어 생성 시 프로필 이미지가 없으면 실패한다.")
            void without_profile() {

                // given
                StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
                    "store",
                    null,
                    "newIntroduce",
                    "01011112222",
                    "01099998888",
                    "temp@test.com",
                    "서울",
                    "123동",
                    null
                );

                // when & then
                Assertions.assertThatThrownBy(() -> sellerStoreService.createStore(request, null)
                    )
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                    });
            }
        }
    }
}
