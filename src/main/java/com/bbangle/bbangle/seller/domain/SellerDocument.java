package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
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
@Table(name = "seller_documents")
@Entity
public class SellerDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Column(name = "type", columnDefinition = "VARCHAR(30)")
    private String type;

    @Column(name = "url", columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name = "status", columnDefinition = "VARCHAR(30)")
    private String status;

}
