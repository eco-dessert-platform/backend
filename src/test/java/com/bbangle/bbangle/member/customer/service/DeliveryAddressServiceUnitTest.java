package com.bbangle.bbangle.member.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.customer.controller.dto.request.DeliveryAddressSaveRequest;
import com.bbangle.bbangle.member.customer.controller.dto.response.DeliveryAddressResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import com.bbangle.bbangle.member.repository.MemberDeliveryAddressRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] DeliveryAddressService")
@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceUnitTest {

    @Mock
    private MemberDeliveryAddressRepository deliveryAddressRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private DeliveryAddressService deliveryAddressService;

    private static final Long MEMBER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
            .id(MEMBER_ID)
            .name("홍길동")
            .email("test@test.com")
            .build();
    }

    private MemberDeliveryAddress buildAddress(boolean isDefault) {
        MemberDeliveryAddress address = MemberDeliveryAddress.builder()
            .member(member)
            .addressName("집")
            .isDefault(isDefault)
            .recipientName("홍길동")
            .phone("010-1234-5678")
            .address("서울시 강남구")
            .addressDetail("101호")
            .zipCode("12345")
            .build();
        ReflectionTestUtils.setField(address, "id", ADDRESS_ID);
        return address;
    }

    private DeliveryAddressSaveRequest buildRequest(Boolean isDefault) {
        return new DeliveryAddressSaveRequest(
            "집", "홍길동", "010-1234-5678",
            "서울시 강남구", "101호", "12345", isDefault
        );
    }

    // -----------------------------------------------------------------------
    // Issue 1 (High): 기본 배송지 동시 생성 방지 — Member 행 잠금 직렬화 검증
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("기본 배송지 동시 생성 방지 (Member 행 잠금)")
    class MemberRowLockForDefaultAddress {

        @Test
        @DisplayName("addDeliveryAddress: isDefault=true 요청 시 Member 행 잠금 획득 후 기존 기본 배송지 해제")
        void addDeliveryAddress_isDefaultTrue_acquiresMemberRowLock() {
            // Given
            MemberDeliveryAddress existingDefault = buildAddress(true);
            MemberDeliveryAddress newAddress = buildAddress(false);

            given(memberRepository.findByIdWithLock(MEMBER_ID)).willReturn(Optional.of(member));
            given(deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(MEMBER_ID))
                .willReturn(Optional.of(existingDefault));
            given(deliveryAddressRepository.save(any())).willReturn(newAddress);

            // When
            deliveryAddressService.addDeliveryAddress(MEMBER_ID, buildRequest(true));

            // Then — Member 행 잠금 메서드가 반드시 호출되어야 함
            then(memberRepository).should().findByIdWithLock(MEMBER_ID);
            assertThat(existingDefault.isDefault()).isFalse(); // 기존 기본 배송지 해제 확인
        }

        @Test
        @DisplayName("addDeliveryAddress: isDefault=false 요청 시 Member 행 잠금 미호출")
        void addDeliveryAddress_isDefaultFalse_doesNotAcquireMemberLock() {
            // Given
            MemberDeliveryAddress newAddress = buildAddress(false);
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(deliveryAddressRepository.save(any())).willReturn(newAddress);

            // When
            deliveryAddressService.addDeliveryAddress(MEMBER_ID, buildRequest(false));

            // Then — Member 행 잠금 메서드가 호출되지 않아야 함
            then(memberRepository).should(never()).findByIdWithLock(anyLong());
        }

        @Test
        @DisplayName("setDefaultDeliveryAddress: 항상 Member 행 잠금 후 기존 기본 배송지 해제")
        void setDefaultDeliveryAddress_alwaysAcquiresMemberRowLock() {
            // Given
            MemberDeliveryAddress target = buildAddress(false);
            MemberDeliveryAddress existingDefault = buildAddress(true);
            ReflectionTestUtils.setField(existingDefault, "id", 20L);

            given(memberRepository.findByIdWithLock(MEMBER_ID)).willReturn(Optional.of(member));
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(target));
            given(deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(MEMBER_ID))
                .willReturn(Optional.of(existingDefault));

            // When
            deliveryAddressService.setDefaultDeliveryAddress(MEMBER_ID, ADDRESS_ID);

            // Then
            then(memberRepository).should().findByIdWithLock(MEMBER_ID);
            assertThat(existingDefault.isDefault()).isFalse();
            assertThat(target.isDefault()).isTrue();
        }

        @Test
        @DisplayName("updateDeliveryAddress: isDefault=true 변경 시 Member 행 잠금 후 기존 기본 배송지 해제")
        void updateDeliveryAddress_changeToDefault_acquiresMemberRowLock() {
            // Given
            MemberDeliveryAddress target = buildAddress(false); // 현재 비기본
            MemberDeliveryAddress existingDefault = buildAddress(true);
            ReflectionTestUtils.setField(existingDefault, "id", 20L);

            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(target));
            given(memberRepository.findByIdWithLock(MEMBER_ID)).willReturn(Optional.of(member));
            given(deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(MEMBER_ID))
                .willReturn(Optional.of(existingDefault));

            // When
            deliveryAddressService.updateDeliveryAddress(MEMBER_ID, ADDRESS_ID, buildRequest(true));

            // Then
            then(memberRepository).should().findByIdWithLock(MEMBER_ID);
            assertThat(existingDefault.isDefault()).isFalse();
            assertThat(target.isDefault()).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Issue 2 (Medium): isDefault null 필드 누락 시 기존 상태 보존
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("isDefault null 처리 — 기존 기본 배송지 상태 보존")
    class IsDefaultNullHandling {

        @Test
        @DisplayName("addDeliveryAddress: isDefault=null이면 기본 배송지 아닌 주소 추가")
        void addDeliveryAddress_isDefaultNull_treatedAsFalse() {
            // Given
            MemberDeliveryAddress newAddress = buildAddress(false);
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(deliveryAddressRepository.save(any())).willReturn(newAddress);

            // When
            DeliveryAddressResponse response = deliveryAddressService.addDeliveryAddress(
                MEMBER_ID, buildRequest(null));

            // Then — 비관적 잠금 미호출, 기본 배송지 아님
            then(memberRepository).should(never()).findByIdWithLock(anyLong());
            assertThat(response.isDefault()).isFalse();
        }

        @Test
        @DisplayName("updateDeliveryAddress: isDefault=null이면 기존 기본 배송지 상태 유지 (잠금 미호출)")
        void updateDeliveryAddress_isDefaultNull_preservesExistingDefaultState() {
            // Given — 현재 기본 배송지인 주소
            MemberDeliveryAddress currentDefault = buildAddress(true);
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(currentDefault));

            // When — isDefault 필드 없이 다른 필드만 수정
            deliveryAddressService.updateDeliveryAddress(MEMBER_ID, ADDRESS_ID, buildRequest(null));

            // Then — 비관적 잠금 미호출, 기본 배송지 상태 유지
            then(memberRepository).should(never()).findByIdWithLock(anyLong());
            assertThat(currentDefault.isDefault()).isTrue();
        }

        @Test
        @DisplayName("updateDeliveryAddress: isDefault=false이면 기본 배송지 해제")
        void updateDeliveryAddress_isDefaultFalse_unsetsDefault() {
            // Given — 현재 기본 배송지인 주소
            MemberDeliveryAddress currentDefault = buildAddress(true);
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(currentDefault));

            // When
            deliveryAddressService.updateDeliveryAddress(MEMBER_ID, ADDRESS_ID, buildRequest(false));

            // Then
            assertThat(currentDefault.isDefault()).isFalse();
        }

        @Test
        @DisplayName("updateDeliveryAddress: 이미 기본 배송지인 주소에 isDefault=true 재요청 시 잠금 미호출")
        void updateDeliveryAddress_alreadyDefault_noLockAcquired() {
            // Given — 이미 기본 배송지
            MemberDeliveryAddress currentDefault = buildAddress(true);
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(currentDefault));

            // When
            deliveryAddressService.updateDeliveryAddress(MEMBER_ID, ADDRESS_ID, buildRequest(true));

            // Then — 이미 기본이므로 잠금 불필요
            then(memberRepository).should(never()).findByIdWithLock(anyLong());
            assertThat(currentDefault.isDefault()).isTrue();
        }
    }

    // -----------------------------------------------------------------------
    // Issue 3 (Low): 배송지 ID 열거 방지 — 소유권 포함 단일 쿼리
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("배송지 ID 열거 방지 — 소유권 포함 단일 조회")
    class IdEnumerationPrevention {

        @Test
        @DisplayName("존재하지 않는 배송지 접근 시 404 반환")
        void deleteDeliveryAddress_addressNotFound_throws404() {
            // Given
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.empty());

            // When & Then — 404 예외 발생
            assertThatThrownBy(() ->
                deliveryAddressService.deleteDeliveryAddress(MEMBER_ID, ADDRESS_ID))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }

        @Test
        @DisplayName("타인 소유 배송지 접근 시 403이 아닌 404 반환 (ID 열거 방지)")
        void deleteDeliveryAddress_otherMembersAddress_throws404NotForbidden() {
            // Given — 다른 memberId로 조회하면 empty (소유권 포함 쿼리)
            Long anotherMemberId = 999L;
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, anotherMemberId))
                .willReturn(Optional.empty());

            // When & Then — 403이 아닌 404 발생으로 ID 존재 여부 비노출
            assertThatThrownBy(() ->
                deliveryAddressService.deleteDeliveryAddress(anotherMemberId, ADDRESS_ID))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }

        @Test
        @DisplayName("소유한 배송지 삭제 성공")
        void deleteDeliveryAddress_ownedAddress_succeeds() {
            // Given
            MemberDeliveryAddress address = buildAddress(false);
            given(deliveryAddressRepository.findByIdAndMemberIdAndIsDeletedFalse(ADDRESS_ID, MEMBER_ID))
                .willReturn(Optional.of(address));

            // When
            deliveryAddressService.deleteDeliveryAddress(MEMBER_ID, ADDRESS_ID);

            // Then — soft delete 처리
            assertThat(address.isDeleted()).isTrue();
        }
    }
}
