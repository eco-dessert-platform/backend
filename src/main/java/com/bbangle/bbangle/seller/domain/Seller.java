package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.domain.model.EmailVO;
import com.bbangle.bbangle.seller.domain.model.PhoneNumberVO;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    @Embedded
    private PhoneNumberVO phoneNumberVO; // phone + subPhone을 포함

    @Embedded
    @Column(name = "email", columnDefinition = "VARCHAR(100)")
    private EmailVO emailVO;

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
        PhoneNumberVO phoneNumberVO,
        EmailVO emailVO,
        String originAddressLine,
        String originAddressDetail,
        String profile,
        CertificationStatus certificationStatus,
        Store store
    ) {

        validateField(originAddressLine, originAddressDetail, profile, certificationStatus, store);
        this.phoneNumberVO = phoneNumberVO;
        this.emailVO = emailVO;
        this.originAddressLine = originAddressLine;
        this.originAddressDetail = originAddressDetail;
        this.profile = profile;
        this.certificationStatus = certificationStatus;
        this.store = store;
    }

    public static Seller create(
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail,
        String profile,
        CertificationStatus certificationStatus,
        Store store
    ) {
        if(store == null)  throw new BbangleException(BbangleErrorCode.INVALID_STORE);

        store.changeStatus(StoreStatus.RESERVED);

        return Seller.builder()
            .phoneNumberVO(PhoneNumberVO.of(phone,subPhone))
            .emailVO(EmailVO.of(email))
            .originAddressLine(originAddressLine)
            .originAddressDetail(originAddressDetail)
            .profile(profile)
            .certificationStatus(certificationStatus)
            .store(store)
            .build();
    }

    private void validateField(
        String originAddressLine,
        String originAddressDetail,
        String profile,
        CertificationStatus certificationStatus,
        Store store
    ) {


        if (originAddressLine == null || originAddressLine.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_ADDRESS);
        }

        if(originAddressDetail == null || originAddressDetail.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_DETAIL_ADDRESS);
        }

        if(profile == null || profile.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);
        }

        if (certificationStatus == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_CERTIFICATION_STATUS);
        }

        if (store == null) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE);
        }
    }


}

