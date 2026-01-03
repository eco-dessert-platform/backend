package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO : Test
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "seller")
@Entity
public class OAuth2Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "profile", columnDefinition = "VARCHAR(255)")
    private String profile;

    @Column(name = "provider", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private OauthServerType provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private CertificationStatus certificationStatus;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 판매자 등록을 위한 생성자
    @Builder
    public OAuth2Seller(
            String name,
            String email,
            String profile,
            OauthServerType provider,
            String providerId,
            CertificationStatus certificationStatus,
            boolean isDeleted,
            Store store
    ) {
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.provider = provider;
        this.providerId = providerId;
        this.certificationStatus = certificationStatus;
        this.isDeleted = isDeleted;
        this.store = store;
    }

    public static OAuth2Seller create(
            String name,
            String email,
            String profile,
            OauthServerType provider,
            String providerId
    ) {
        return OAuth2Seller.builder()
                .name(name)
                .email(email)
                .profile(profile)
                .provider(provider)
                .providerId(providerId)
                .certificationStatus(CertificationStatus.PENDING)
                .isDeleted(false)
                .store(null)
                .build();
    }
}
