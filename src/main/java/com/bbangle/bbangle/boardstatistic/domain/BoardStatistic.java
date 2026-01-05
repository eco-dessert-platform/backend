package com.bbangle.bbangle.boardstatistic.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.domain.SoftDeleteBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
public class BoardStatistic extends SoftDeleteBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY) // 연관관계 주인을 board로 변경하는 게 좋아보임
    @JoinColumn(name = "board_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

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
