package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.common.domain.SoftDeleteCreatedAtBaseEntity;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
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

    // 판매자 스토어 등록
    public void registerStore(Store store) {
        this.store = store;
        this.store.changeStatus(StoreStatus.RESERVED);
        this.certificationStatus = CertificationStatus.PENDING;
    }

    /**
     * 해당 Seller 계정이 Store 등록 가능한지 체크하는 메서드
     * @return true = Store 등록 가능 | false = Store 등록 불가능
     */
    public boolean isRegisterAvailable() {
        return this.certificationStatus != CertificationStatus.PENDING
            && this.certificationStatus != CertificationStatus.APPROVED;
    }
}

