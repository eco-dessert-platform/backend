package com.bbangle.bbangle.member.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.customer.controller.dto.request.DeliveryAddressSaveRequest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import com.bbangle.bbangle.member.repository.MemberDeliveryAddressRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] DeliveryAddressService")
@SpringBootTest
@ActiveProfiles("test")
class DeliveryAddressServiceIntegrationTest {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberDeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // -----------------------------------------------------------------------
    // Issue 1 (High): 기본 배송지 동시 생성 방지 — Member 행 잠금 동시성 검증
    //
    // @Transactional 사용 불가: 비관적 잠금은 커밋 시점에 해제되므로
    // 클래스 레벨 @Transactional이 있으면 경합 자체가 발생하지 않는다.
    // @AfterEach JDBC 직접 정리로 격리 보장.
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("기본 배송지 동시 설정 — Member 행 잠금 동시성 검증")
    class ConcurrentDefaultAddressTest {

        private Member savedMember;

        @BeforeEach
        void setUp() {
            savedMember = memberRepository.save(Member.builder()
                .name("동시성_테스트_회원")
                .email("concurrent@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("concurrent-test-id")
                .build());
        }

        @AfterEach
        void tearDown() {
            jdbcTemplate.update(
                "DELETE FROM member_delivery_address WHERE member_id = ?",
                savedMember.getId()
            );
            jdbcTemplate.update(
                "DELETE FROM member WHERE id = ?",
                savedMember.getId()
            );
        }

        @Test
        @DisplayName("N개 스레드가 동시에 isDefault=true로 배송지 추가해도 기본 배송지는 1개만 존재한다")
        void concurrent_addDefaultAddress_onlyOneDefaultExists() throws InterruptedException {
            int threadCount = 5;
            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Exception> errors = new CopyOnWriteArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            Long memberId = savedMember.getId();

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                executor.submit(() -> {
                    try {
                        startGate.await();
                        deliveryAddressService.addDeliveryAddress(memberId,
                            new DeliveryAddressSaveRequest(
                                "집" + idx, "홍길동", "010-0000-0000",
                                "서울시 강남구", "101호", "12345", true
                            ));
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startGate.countDown();
            boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(finished).as("모든 스레드가 10초 내에 완료되어야 함").isTrue();
            assertThat(errors).as("동시 요청 중 예외 발생: %s", errors).isEmpty();

            List<MemberDeliveryAddress> addresses =
                deliveryAddressRepository.findAllByMemberIdAndIsDeletedFalse(memberId);
            long defaultCount = addresses.stream()
                .filter(MemberDeliveryAddress::isDefault)
                .count();

            assertThat(addresses).as("배송지는 스레드 수만큼 생성되어야 함").hasSize(threadCount);
            assertThat(defaultCount).as("기본 배송지는 정확히 1개여야 함").isEqualTo(1);
        }

        @Test
        @DisplayName("N개 스레드가 동시에 setDefaultDeliveryAddress를 호출해도 기본 배송지는 1개만 존재한다")
        void concurrent_setDefaultDeliveryAddress_onlyOneDefaultExists() throws InterruptedException {
            int threadCount = 5;
            Long memberId = savedMember.getId();

            // 기본 배송지 후보 N개 미리 생성 (isDefault=false)
            List<Long> addressIds = new CopyOnWriteArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                MemberDeliveryAddress addr = deliveryAddressRepository.save(
                    MemberDeliveryAddress.builder()
                        .member(savedMember)
                        .addressName("집" + i)
                        .recipientName("홍길동")
                        .phone("010-0000-0000")
                        .address("서울시 강남구")
                        .addressDetail("101호")
                        .zipCode("12345")
                        .isDefault(false)
                        .build()
                );
                addressIds.add(addr.getId());
            }

            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<Exception> errors = new CopyOnWriteArrayList<>();
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final Long addressId = addressIds.get(i);
                executor.submit(() -> {
                    try {
                        startGate.await();
                        deliveryAddressService.setDefaultDeliveryAddress(memberId, addressId);
                    } catch (Exception e) {
                        errors.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startGate.countDown();
            boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(finished).as("모든 스레드가 10초 내에 완료되어야 함").isTrue();
            assertThat(errors).as("동시 요청 중 예외 발생: %s", errors).isEmpty();

            List<MemberDeliveryAddress> addresses =
                deliveryAddressRepository.findAllByMemberIdAndIsDeletedFalse(memberId);
            long defaultCount = addresses.stream()
                .filter(MemberDeliveryAddress::isDefault)
                .count();

            assertThat(defaultCount).as("기본 배송지는 정확히 1개여야 함").isEqualTo(1);
        }
    }

    // -----------------------------------------------------------------------
    // Issue 2 (Medium): isDefault null — DB 실제 상태 확인
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isDefault null 처리 — DB 상태 검증")
    class IsDefaultNullHandlingTest {

        @Test
        @Transactional
        @DisplayName("updateDeliveryAddress: isDefault=null이면 기존 기본 배송지 상태가 DB에 유지된다")
        void update_withNullIsDefault_preservesExistingDefaultState() {
            Member member = memberRepository.save(Member.builder()
                .name("테스트_회원_null")
                .email("null-default@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("null-default-id")
                .build());

            MemberDeliveryAddress defaultAddress = deliveryAddressRepository.save(
                MemberDeliveryAddress.builder()
                    .member(member)
                    .addressName("집")
                    .recipientName("홍길동")
                    .phone("010-0000-0000")
                    .address("서울시 강남구")
                    .addressDetail("101호")
                    .zipCode("12345")
                    .isDefault(true)
                    .build()
            );

            // isDefault 필드 없이(null) 다른 필드만 수정
            deliveryAddressService.updateDeliveryAddress(
                member.getId(),
                defaultAddress.getId(),
                new DeliveryAddressSaveRequest(
                    "집(수정)", "홍길순", "010-1111-2222",
                    "서울시 마포구", "202호", "00000", null
                )
            );

            MemberDeliveryAddress updated =
                deliveryAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            // 다른 필드는 변경되고 isDefault 상태는 유지
            assertThat(updated.isDefault()).isTrue();
            assertThat(updated.getAddressName()).isEqualTo("집(수정)");
            assertThat(updated.getRecipientName()).isEqualTo("홍길순");
        }

        @Test
        @Transactional
        @DisplayName("addDeliveryAddress: isDefault=null이면 기본 배송지가 설정되지 않는다")
        void add_withNullIsDefault_doesNotSetAsDefault() {
            Member member = memberRepository.save(Member.builder()
                .name("테스트_회원_null2")
                .email("null-add@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("null-add-id")
                .build());

            deliveryAddressService.addDeliveryAddress(
                member.getId(),
                new DeliveryAddressSaveRequest(
                    "집", "홍길동", "010-0000-0000",
                    "서울시", "101호", "12345", null
                )
            );

            List<MemberDeliveryAddress> addresses =
                deliveryAddressRepository.findAllByMemberIdAndIsDeletedFalse(member.getId());

            assertThat(addresses).hasSize(1);
            assertThat(addresses.get(0).isDefault()).isFalse();
        }

        @Test
        @Transactional
        @DisplayName("updateDeliveryAddress: isDefault=false이면 기본 배송지가 DB에서 해제된다")
        void update_withFalseIsDefault_unsetsDefaultInDb() {
            Member member = memberRepository.save(Member.builder()
                .name("테스트_회원_false")
                .email("false-default@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("false-default-id")
                .build());

            MemberDeliveryAddress defaultAddress = deliveryAddressRepository.save(
                MemberDeliveryAddress.builder()
                    .member(member)
                    .addressName("집")
                    .recipientName("홍길동")
                    .phone("010-0000-0000")
                    .address("서울시")
                    .addressDetail("101호")
                    .zipCode("12345")
                    .isDefault(true)
                    .build()
            );

            deliveryAddressService.updateDeliveryAddress(
                member.getId(),
                defaultAddress.getId(),
                new DeliveryAddressSaveRequest(
                    "집", "홍길동", "010-0000-0000",
                    "서울시", "101호", "12345", false
                )
            );

            MemberDeliveryAddress updated =
                deliveryAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            assertThat(updated.isDefault()).isFalse();
        }
    }

    // -----------------------------------------------------------------------
    // Issue 3 (Low): ID 열거 방지 — 소유권 포함 단일 쿼리
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("배송지 ID 열거 방지 — 소유권 포함 조회 검증")
    class IdEnumerationPreventionTest {

        @Test
        @Transactional
        @DisplayName("타인 소유 배송지 ID 접근 시 403이 아닌 404가 반환된다 (ID 존재 여부 비노출)")
        void accessOtherMembersAddress_returns404NotForbidden() {
            Member owner = memberRepository.save(Member.builder()
                .name("소유자")
                .email("owner@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("owner-id")
                .build());

            Member attacker = memberRepository.save(Member.builder()
                .name("공격자")
                .email("attacker@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("attacker-id")
                .build());

            MemberDeliveryAddress ownerAddress = deliveryAddressRepository.save(
                MemberDeliveryAddress.builder()
                    .member(owner)
                    .addressName("집")
                    .recipientName("소유자")
                    .phone("010-0000-0000")
                    .address("서울시")
                    .addressDetail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build()
            );

            // 공격자가 소유자의 배송지 ID로 삭제 시도 → 403이 아닌 404
            assertThatThrownBy(() ->
                deliveryAddressService.deleteDeliveryAddress(attacker.getId(), ownerAddress.getId()))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }

        @Test
        @Transactional
        @DisplayName("타인 소유 배송지를 기본 배송지로 설정 시도해도 404가 반환된다")
        void setDefaultOtherMembersAddress_returns404() {
            Member owner = memberRepository.save(Member.builder()
                .name("소유자2")
                .email("owner2@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("owner2-id")
                .build());

            Member attacker = memberRepository.save(Member.builder()
                .name("공격자2")
                .email("attacker2@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("attacker2-id")
                .build());

            MemberDeliveryAddress ownerAddress = deliveryAddressRepository.save(
                MemberDeliveryAddress.builder()
                    .member(owner)
                    .addressName("집")
                    .recipientName("소유자2")
                    .phone("010-0000-0000")
                    .address("서울시")
                    .addressDetail("101호")
                    .zipCode("12345")
                    .isDefault(false)
                    .build()
            );

            assertThatThrownBy(() ->
                deliveryAddressService.setDefaultDeliveryAddress(attacker.getId(), ownerAddress.getId()))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }

        @Test
        @Transactional
        @DisplayName("존재하지 않는 배송지 ID 접근 시 404가 반환된다")
        void accessNonExistentAddress_returns404() {
            Member member = memberRepository.save(Member.builder()
                .name("테스트_회원_notfound")
                .email("notfound@test.com")
                .provider(OauthServerType.KAKAO)
                .providerId("notfound-id")
                .build());

            Long nonExistentId = Long.MAX_VALUE;

            assertThatThrownBy(() ->
                deliveryAddressService.deleteDeliveryAddress(member.getId(), nonExistentId))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }
    }
}
