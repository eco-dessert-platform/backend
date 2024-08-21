package com.bbangle.bbangle.boardstatistic.domain;

import com.bbangle.bbangle.boardstatistic.update.StatisticUpdate;
import com.bbangle.bbangle.boardstatistic.update.UpdateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "board_statistic")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "basic_score")
    private Double basicScore;

    @Column(name = "board_wish_count")
    private Long boardWishCount;

    @Column(name = "board_review_count")
    private Long boardReviewCount;

    @Column(name = "board_view_count")
    private Long boardViewCount;

    @Column(name = "board_review_grade")
    private BigDecimal boardReviewGrade;

    public void updateBasicScore(Double score, boolean createAction) {
        if (createAction) {
            this.basicScore += score;
            return;
        }
        basicScore -= score;
    }

    public void setBoardWishCountWhenInit(Long boardViewCount) {
        this.boardWishCount = boardViewCount;
    }

    public void updateViewCount(int count) {
        this.boardViewCount += count;
    }

    public void updateWishCount(Long wishCount) {
        this.boardWishCount = wishCount;
    }

    public void updateReviewCount(Long reviewCount) {
       this.boardViewCount = reviewCount;
    }

    public void updateReviewGrade(BigDecimal reviewGrade) {
        this.boardReviewGrade = reviewGrade;
    }

    public void setBoardReviewCountWhenInit(Long count) {
        this.boardReviewCount = count;
    }

    public void setBoardReviewRateWhenInit(BigDecimal grade) {
        this.boardReviewGrade = grade;
    }

    public void updateBasicScoreWhenInit() {
        this.basicScore = (double) (boardReviewCount * 50 + boardViewCount + boardWishCount * 50);
    }

}
