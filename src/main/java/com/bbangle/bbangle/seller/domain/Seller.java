package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
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

    @Column(name = "phone",  columnDefinition = "VARCHAR(20)")
    private String phone;

    @Column(name = "sub_phone", columnDefinition = "VARCHAR(20)")
    private String subPhone;

    @Column(name= "email", columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(name = "origin_address_line", columnDefinition = "VARCHAR(255)")
    private String originAddressLine;

    @Column(name = "origin_address_detail", columnDefinition = "VARCHAR(255)")
    private String originAddressDetail;

    @Column(name = "profile", columnDefinition = "VARCHAR(255)")
    private String profile;

    @Column(name = "status", columnDefinition = "VARCHAR(20)")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

}

