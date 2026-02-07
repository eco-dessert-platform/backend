package com.bbangle.bbangle.store.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.domain.SoftDeleteBaseEntity;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "store")
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends SoftDeleteBaseEntity {

    private static final String DEFAULT_IDENTIFIER =
        String.valueOf((Math.abs(UUID.randomUUID().getLeastSignificantBits()) % 90000) + 10000);

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    List<Board> boards = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "name")
    private String name;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "profile")
    private String profile;

    @Embedded
    private PhoneNumberVO phoneNumberVO; // phone + subPhone을 포함

    @Embedded
    private EmailVO emailVO;

    @Column(name = "origin_address_line", columnDefinition = "VARCHAR(255)")
    private String originAddressLine;

    @Column(name = "origin_address_detail", columnDefinition = "VARCHAR(255)")
    private String originAddressDetail;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @Builder
    private Store(
        String name,
        String identifier,
        String introduce,
        String profile,
        PhoneNumberVO phoneNumberVO,
        EmailVO emailVO,
        String originAddressLine,
        String originAddressDetail,
        StoreStatus status
    ) {
        this.name = name;
        this.identifier = identifier;
        this.introduce = introduce;
        this.profile = profile;
        this.phoneNumberVO = phoneNumberVO;
        this.emailVO = emailVO;
        this.originAddressLine = originAddressLine;
        this.originAddressDetail = originAddressDetail;
        this.status = status;
    }

    public static Store createForSeller(
        String name,
        String profile,
        String introduce,
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail
    ) {
        return Store.builder()
            .name(name)
            .identifier(DEFAULT_IDENTIFIER)
            .profile(profile)
            .introduce(introduce)
            .phoneNumberVO(PhoneNumberVO.of(phone, subPhone))
            .emailVO(EmailVO.of(email))
            .originAddressLine(originAddressLine)
            .originAddressDetail(originAddressDetail)
            .status(StoreStatus.NONE)
            .build();
    }

    public void updateDetail(
        String profile,
        String introduce,
        String phone,
        String subPhone,
        String email,
        String originAddressLine,
        String originAddressDetail
    ) {
        this.profile = profile;
        this.introduce = introduce;
        this.phoneNumberVO = PhoneNumberVO.of(phone, subPhone);
        this.emailVO = EmailVO.of(email);
        this.originAddressLine = originAddressLine;
        this.originAddressDetail = originAddressDetail;
    }

    public void changeStatus(StoreStatus status) {
        this.status = status;
    }
}
