package com.bbangle.bbangle.store.repository;


import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.querydsl.core.NonUniqueResultException;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] StoreRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("스토어 이름으로 검색을 성공합니다.")
    void findByStoreNameLike_success() {
        //arrange
        Store store = StoreFixture.defaultStore();
        storeRepository.save(store);
        // act
        Optional<Store> result = storeRepository.findByStoreName(DEFAULT_STORE_NAME);
        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(DEFAULT_STORE_NAME);
    }

    @Test
    @DisplayName("검색된 스토어가 없으면 빈 값을 반환합니다.")
    void findByStoreNameLike_ReturnEmpty() {
        // arrange
        Store store = StoreFixture.defaultStore();
        storeRepository.save(store);
        // act
        String keyword = "noExist";
        Optional<Store> result = storeRepository.findByStoreName(keyword);
        // assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("검색 결과가 여러 개일 경우 NonUniqueResultException 예외가 발생합니다.")
    void findByStoreNameLike_ThrowException_WhenMultipleResult() {
        // given
        Store store1 = StoreFixture.defaultStore("Bbanggle");
        storeRepository.save(store1);

        Store store2 = StoreFixture.defaultStore("Bbanggle");
        storeRepository.save(store2);

        String searchKeyword = "Bbanggle";

        // when & then
        assertThatThrownBy(() -> storeRepository.findByStoreName(searchKeyword))
            .isInstanceOf(NonUniqueResultException.class);
    }

    @Nested
    @DisplayName("existsByNormalizedStoreName() 테스트")
    class ExistsByNormalizedStoreNameTest {

        @Test
        @DisplayName("공백을 제거한 스토어명이 존재하면 true를 반환한다.")
        void existsByNormalizedStoreName() {

            // given
            Store store = StoreFixture.defaultStore("빵 그리");
            storeRepository.save(store);

            em.flush();
            em.clear();

            // when
            boolean result = storeRepository.existsByNormalizedStoreName("빵그리");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("삭제된 스토어는 중복 검사 대상에서 제외한다.")
        void existsByNormalizedStoreName_deleted() {

            // given
            Store store = StoreFixture.defaultStore("빵그리");
            storeRepository.save(store);
            store.delete();

            em.flush();
            em.clear();

            // when
            boolean result = storeRepository.existsByNormalizedStoreName("빵그리");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("공백을 제거한 스토어명이 존재하지 않으면 false를 반환한다.")
        void existsByNormalizedStoreName_notExists() {

            // given
            Store store = StoreFixture.defaultStore("빵그리");
            storeRepository.save(store);

            em.flush();
            em.clear();

            // when
            boolean result = storeRepository.existsByNormalizedStoreName("케이크");

            // then
            assertThat(result).isFalse();
        }
    }
}
