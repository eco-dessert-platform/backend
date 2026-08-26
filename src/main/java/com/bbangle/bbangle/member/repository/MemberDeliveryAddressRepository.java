package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeliveryAddressRepository extends JpaRepository<MemberDeliveryAddress, Long> {

    // 기본 배송지 단순 조회 (읽기 전용)
    Optional<MemberDeliveryAddress> findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(Long memberId);

    // 소유권을 조회 조건에 포함해 ID 열거 방지 (없으면 무조건 404)
    Optional<MemberDeliveryAddress> findByIdAndMemberIdAndIsDeletedFalse(Long id, Long memberId);

    List<MemberDeliveryAddress> findAllByMemberIdAndIsDeletedFalse(Long memberId);
}
