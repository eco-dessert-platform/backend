package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.common.domain.SoftDeleteCreatedAtBaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "sellers")
@Entity
public class Seller extends SoftDeleteCreatedAtBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(15)")
    private String name;

    @Column(name = "provider", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private OauthServerType provider;

    @Column(name = "provider_id", columnDefinition = "VARCHAR(50)", unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private CertificationStatus certificationStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", unique = true)
    private Store store;

    // 판매자 등록을 위한 생성자
    @Builder
    private Seller(
        String name,
        OauthServerType provider,
        String providerId,
        CertificationStatus certificationStatus,
        Store store
    ) {
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.certificationStatus = certificationStatus;
        this.store = store;
    }

    public static Seller create(
        String name,
        OauthServerType provider,
        String providerId
    ) {
        return Seller.builder()
            .name(name)
            .provider(provider)
            .providerId(providerId)
            .certificationStatus(CertificationStatus.NEW)
            .store(null)
            .build();
    }

    // TODO : v3 Admin이 승인하면 등록되도록 변경
    // 판매자 스토어 등록
    public void registerStore(Store store) {
        this.store = store;
        this.certificationStatus = CertificationStatus.PENDING;
    }

    public void updateStatus(CertificationStatus status) {
        this.certificationStatus = status;
    }

    // TODO : 테스트
    /**
     * 해당 Seller 계정이 Store 등록 가능한지 체크하는 메서드
     */
    public void isRegisterAvailable() {
        if (this.certificationStatus != CertificationStatus.NEW && this.certificationStatus != CertificationStatus.REJECTED)
            throw new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE);
    }
}

