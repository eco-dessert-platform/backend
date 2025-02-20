package com.bbangle.bbangle.review.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Table(
        name = "review_like",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "review_id"})
        }
)
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id")
    @NotNull
    private Long reviewId;

    @Column(name = "member_id")
    @NotNull
    private Long memberId;
}
