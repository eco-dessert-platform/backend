package com.bbangle.bbangle.board.domain;


import com.bbangle.bbangle.board.customer.domain.constant.Segment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "member_segment_and_intolerance")
@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSegment {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_segment", columnDefinition = "varchar")
    private Segment segment;

    @Column(name = "lactose_intolerance", columnDefinition = "tinyint")
    private Boolean lactoseIntolerance;

    @Column(name = "peanut_intolerance", columnDefinition = "tinyint")
    private Boolean peanutIntolerance;

    @Column(name = "peach_intolerance", columnDefinition = "tinyint")
    private Boolean peachIntolerance;

    @Column(name = "rice_intolerance", columnDefinition = "tinyint")
    private Boolean riceIntolerance;

    @Column(name = "tomato_intolerance", columnDefinition = "tinyint")
    private Boolean tomatoIntolerance;

    @Column(name = "pine_nuts_intolerance", columnDefinition = "tinyint")
    private Boolean pineNutsIntolerance;

    @Column(name = "soy_milk_intolerance", columnDefinition = "tinyint")
    private Boolean soyMilkIntolerance;

    @Column(name = "bean_intolerance", columnDefinition = "tinyint")
    private Boolean beanIntolerance;

    @Column(name = "walnuts_intolerance", columnDefinition = "tinyint")
    private Boolean walnutsIntolerance;
}
