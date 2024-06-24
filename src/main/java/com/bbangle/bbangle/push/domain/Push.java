package com.bbangle.bbangle.push.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "push_category", columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private PushCategory pushCategory;

    @Column(name = "is_subscribed", columnDefinition = "tinyint")
    private boolean subscribed;


    public void updateSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

}
