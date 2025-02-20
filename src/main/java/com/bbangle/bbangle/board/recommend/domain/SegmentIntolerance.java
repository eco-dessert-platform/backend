package com.bbangle.bbangle.board.recommend.domain;

import com.bbangle.bbangle.board.recommend.enums.Segment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "product_board_id")
    private Long boardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_segment")
    private Segment segment;

    @Id
    @Column(name = "product_id")
    private Long productId;

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
