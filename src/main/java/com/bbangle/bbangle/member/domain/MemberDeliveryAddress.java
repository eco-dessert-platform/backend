package com.bbangle.bbangle.member.domain;

import com.bbangle.bbangle.common.domain.SoftDeleteBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "member_delivery_address")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDeliveryAddress extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    @Column(name = "is_default", nullable = false, columnDefinition = "tinyint")
    private boolean isDefault;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Builder
    private MemberDeliveryAddress(
        Member member,
        String addressName,
        boolean isDefault,
        String recipientName,
        String phone,
        String address,
        String addressDetail,
        String zipCode
    ) {
        this.member = member;
        this.addressName = addressName;
        this.isDefault = isDefault;
        this.recipientName = recipientName;
        this.phone = phone;
        this.address = address;
        this.addressDetail = addressDetail;
        this.zipCode = zipCode;
    }
}
