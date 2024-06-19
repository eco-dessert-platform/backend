package com.bbangle.bbangle.push.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "push")
@Getter
@Entity
@Builder
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

    @Column(name = "push_category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private PushCategory pushCategory;

    @Column(name = "push_status", columnDefinition = "tinyint")
    private boolean pushStatus;


    public void recreatePush() {
        this.pushStatus = true;
    }


    public void cancelPush() {
        this.pushStatus = false;
    }

}
