package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeliveryAddressRepository extends JpaRepository<MemberDeliveryAddress, Long> {

    Optional<MemberDeliveryAddress> findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(Long memberId);

    List<MemberDeliveryAddress> findAllByMemberIdAndIsDeletedFalse(Long memberId);
}
