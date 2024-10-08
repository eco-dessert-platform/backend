package com.bbangle.bbangle.push.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.google.firebase.database.annotations.Nullable;
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
import jakarta.validation.constraints.NotNull;

import lombok.ToString;

@Table(name = "push")
@Getter
@Entity
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Push extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "product_id")
    private Long productId;

    @Nullable
    @Column(name = "push_type", columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private PushType pushType;

    @Column(name = "days")
    private String days;

    @Column(name = "push_category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private PushCategory pushCategory;

    @NotNull
    @Column(name = "is_active", columnDefinition = "tinyint default 0")
    private boolean isActive = false;

    public void updateDays(String days) {
        this.days = days;
    }

    public void updateActive(boolean isActive) {
        this.isActive = isActive;
    }

}
