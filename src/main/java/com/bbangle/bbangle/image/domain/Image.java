package com.bbangle.bbangle.image.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "image")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_category", columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ImageCategory imageCategory;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "image_order")
    private int order;

    @Column(name = "path")
    private String path;

    public void update(Long domainId, String path){
        this.domainId = domainId;
        this.path = path;
    }
}
