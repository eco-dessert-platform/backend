package com.bbangle.bbangle.store.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;
import com.bbangle.bbangle.store.domain.model.StoreApplicationStatus;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "store_application")
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "profile")
    private String profile;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StoreApplicationStatus status;

    @Embedded
    private PhoneNumberVO phoneNumberVO;

    @Embedded
    private EmailVO emailVO;

    @Column(name = "origin_address_line", columnDefinition = "VARCHAR(255)")
    private String originAddressLine;

    @Column(name = "origin_address_detail", columnDefinition = "VARCHAR(255)")
    private String originAddressDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder
    private StoreApplication(
        String name,
        String introduce,
        String profile,
        StoreApplicationStatus status,
        PhoneNumberVO phoneNumberVO,
        EmailVO emailVO,
        String originAddressLine,
        String originAddressDetail,
        Seller seller,
        Store store
    ) {
        this.name = name;
        this.introduce = introduce;
        this.profile = profile;
        this.status = status;
        this.phoneNumberVO = phoneNumberVO;
        this.emailVO = emailVO;
        this.originAddressLine = originAddressLine;
        this.originAddressDetail = originAddressDetail;
        this.seller = seller;
        this.store = store;
    }

    public static StoreApplication createStoreApplication(
        String name,
        String profile,
        String introduce,
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail,
        Seller Seller,
        Store store
    ) {
        return StoreApplication.builder()
            .name(name)
            .introduce(introduce)
            .profile(profile)
            .status(StoreApplicationStatus.PENDING)
            .phoneNumberVO(PhoneNumberVO.of(phone, subPhone))
            .emailVO(EmailVO.of(email))
            .originAddressLine(originAddressLine)
            .originAddressDetail(originAddressDetail)
            .seller(Seller)
            .store(store)
            .build();
    }

    public void updateStatus(StoreApplicationStatus status) {
        this.status = status;
    }
}
