package com.bbangle.bbangle.push.domain;

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
import lombok.ToString;

/**
 *
 * 현재 push 테이블이 이전 버전이라 아예 테이블을 따로 만들면 좋을 꺼 같아서
 * 이름만 변경했습니다
 * 추후 완료되면 이 Entity가 push 테이블로 들어갈 예정입니다
 */
@Table(name = "push_temp")
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

    @Column(name = "push_type", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private PushType pushType;

    @Column(name = "days")
    private String days;

    @Column(name = "push_category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private PushCategory pushCategory;

    @Column(name = "is_active", columnDefinition = "tinyint")
    private boolean active;


    public void updateDays(String days) {
        this.days = days;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

}
