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
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

        // isDefault 요청 시 기존 기본 배송지 해제
        if (request.isDefault()) {
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
            .isDefault(request.isDefault())
            .build();

        return DeliveryAddressResponse.from(deliveryAddressRepository.save(address));
    }

    public DeliveryAddressResponse updateDeliveryAddress(Long memberId, Long addressId,
                                                         DeliveryAddressSaveRequest request) {
        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);

        // isDefault 요청 시 기존 기본 배송지 해제 (본인 주소 제외)
        if (request.isDefault() && !address.isDefault()) {
            deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
                .ifPresent(MemberDeliveryAddress::unsetDefault);
        }

        address.update(
            request.addressName(),
            request.recipientName(),
            request.phone(),
            request.address(),
            request.addressDetail(),
            request.zipCode()
        );

        if (request.isDefault()) {
            address.setDefault();
        } else {
            address.unsetDefault();
        }

        return DeliveryAddressResponse.from(address);
    }

    public void deleteDeliveryAddress(Long memberId, Long addressId) {
        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);
        address.delete();
    }

    public void setDefaultDeliveryAddress(Long memberId, Long addressId) {
        MemberDeliveryAddress address = findOwnedAddress(memberId, addressId);

        // 기존 기본 배송지 해제
        deliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(memberId)
            .ifPresent(MemberDeliveryAddress::unsetDefault);

        address.setDefault();
    }

    private MemberDeliveryAddress findOwnedAddress(Long memberId, Long addressId) {
        MemberDeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .filter(a -> !a.isDeleted())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        if (!address.getMember().getId().equals(memberId)) {
            throw new BbangleException(BbangleErrorCode.DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        return address;
    }
}
