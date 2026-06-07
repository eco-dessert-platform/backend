package com.bbangle.bbangle.member.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.customer.controller.dto.request.DeliveryAddressSaveRequest;
import com.bbangle.bbangle.member.customer.controller.dto.response.DeliveryAddressResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import com.bbangle.bbangle.member.repository.MemberDeliveryAddressRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryAddressService {

    private final MemberDeliveryAddressRepository deliveryAddressRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getDeliveryAddresses(Long memberId) {
        return deliveryAddressRepository.findAllByMemberIdAndIsDeletedFalse(memberId)
            .stream()
            .map(DeliveryAddressResponse::from)
            .toList();
    }

    public DeliveryAddressResponse addDeliveryAddress(Long memberId, DeliveryAddressSaveRequest request) {
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault());

        // isDefault=true일 때 Member 행 잠금을 직렬화 포인트로 사용해 동시 insert 경합 방지
        Member member = makeDefault
            ? memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER))
            : memberRepository.findById(memberId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        if (makeDefault) {
            deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
                .ifPresent(MemberDeliveryAddress::unsetDefault);
        }

        MemberDeliveryAddress address = MemberDeliveryAddress.builder()
            .member(member)
            .addressName(request.addressName())
            .recipientName(request.recipientName())
            .phone(request.phone())
            .address(request.address())
            .addressDetail(request.addressDetail())
            .zipCode(request.zipCode())
            .isDefault(makeDefault)
            .build();

        return DeliveryAddressResponse.from(deliveryAddressRepository.save(address));
    }

    public DeliveryAddressResponse updateDeliveryAddress(Long memberId, Long addressId,
                                                         DeliveryAddressSaveRequest request) {
        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);

        address.update(
            request.addressName(),
            request.recipientName(),
            request.phone(),
            request.address(),
            request.addressDetail(),
            request.zipCode()
        );

        // isDefault가 null이면 기존 기본 배송지 상태 유지
        if (request.isDefault() != null) {
            boolean makeDefault = request.isDefault();
            if (makeDefault && !address.isDefault()) {
                // Member 행 잠금으로 직렬화 후 기존 기본 배송지 해제
                memberRepository.findByIdWithLock(memberId)
                    .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));
                deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
                    .ifPresent(MemberDeliveryAddress::unsetDefault);
                address.setDefault();
            } else if (!makeDefault) {
                address.unsetDefault();
            }
        }

        return DeliveryAddressResponse.from(address);
    }

    public void deleteDeliveryAddress(Long memberId, Long addressId) {
        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);
        address.delete();
    }

    public void setDefaultDeliveryAddress(Long memberId, Long addressId) {
        // Member 행 잠금을 먼저 획득해 동시 기본 배송지 변경 직렬화
        memberRepository.findByIdWithLock(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);
        deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
            .ifPresent(MemberDeliveryAddress::unsetDefault);
        address.setDefault();
    }

    // 소유권을 조회 조건에 포함해 타인 주소 접근 시에도 동일한 404 반환 (ID 열거 방지)
    private MemberDeliveryAddress findOwnedAddress(Long memberId, Long addressId) {
        return deliveryAddressRepository
            .findByIdAndMemberIdAndIsDeletedFalse(addressId, memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND));
    }
}
