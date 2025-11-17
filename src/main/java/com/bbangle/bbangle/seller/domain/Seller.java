package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.domain.model.EmailVO;
import com.bbangle.bbangle.seller.domain.model.PhoneNumberVO;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "sellers")
@Entity
public class Seller extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", columnDefinition = "VARCHAR(20)")
    private String phone;

    @Column(name = "sub_phone", columnDefinition = "VARCHAR(20)")
    private String subPhone;

    @Column(name = "email", columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(name = "origin_address_line", columnDefinition = "VARCHAR(255)")
    private String originAddressLine;

    @Column(name = "origin_address_detail", columnDefinition = "VARCHAR(255)")
    private String originAddressDetail;

    @Column(name = "profile", columnDefinition = "VARCHAR(255)")
    private String profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private CertificationStatus certificationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 판매자 등록을 위한 생성자
    @Builder(access = AccessLevel.PRIVATE)
    private Seller(
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail,
        String profile,
        CertificationStatus certificationStatus
    ) {
        this.phone = phone;
        this.subPhone = subPhone;
        this.email = email;
        this.originAddressLine = originAddressLine;
        this.originAddressDetail = originAddressDetail;
        this.profile = profile;
        this.certificationStatus = certificationStatus;
        validateField();
    }

    public static Seller create(
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail,
        String profile,
        CertificationStatus certificationStatus
    ) {
        return Seller.builder()
            .phone(phone)
            .subPhone(subPhone)
            .email(email)
            .originAddressLine(originAddressLine)
            .originAddressDetail(originAddressDetail)
            .profile(profile)
            .certificationStatus(certificationStatus)
            .build();
    }


    private void validateField() {

        PhoneNumberVO.of(this.phone, this.subPhone);
        EmailVO.of(this.email);
        if (this.originAddressLine == null || this.originAddressLine.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_ADDRESS);
        }

        if(this.originAddressDetail == null || this.originAddressDetail.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_DETAIL_ADDRESS);
        }

        if(this.profile == null || this.profile.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        if (this.certificationStatus == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_CERTIFICATION_STATUS);
        }
    }


}

