package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.board.domain.constant.Segment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Table(name = "segment_intolerance")
@Builder
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SegmentIntolerance {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_board_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_segment")
    private Segment segment;

    @Column(name = "lactose_tag", columnDefinition = "tinyint")
    private Boolean lactoseTag;

    @Column(name = "peanut_tag", columnDefinition = "tinyint")
    private Boolean peanutTag;

    @Column(name = "walnuts_tag", columnDefinition = "tinyint")
    private Boolean walnutsTag;

    @Column(name = "pine_nuts_tag", columnDefinition = "tinyint")
    private Boolean pineNutsTag;

    @Column(name = "soy_milk_tag", columnDefinition = "tinyint")
    private Boolean soyMilkTag;

    @Column(name = "rice_tag", columnDefinition = "tinyint")
    private Boolean riceTag;

    @Column(name = "peach_tag", columnDefinition = "tinyint")
    private Boolean peachTag;

    @Column(name = "bean_tag", columnDefinition = "tinyint")
    private Boolean beanTag;

    @Column(name = "tomato_tag", columnDefinition = "tinyint")
    private Boolean tomatoTag;

}
