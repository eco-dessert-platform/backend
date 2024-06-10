package com.bbangle.bbangle.boardstatistic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private int boardWishCount;

    @Column(name = "board_review_count")
    private int boardReviewCount;

    @Column(name = "board_view_count")
    private int boardViewCount;

    @Column(name = "board_review_grade")
    private Double boardReviewGrade;

    public void updateBasicScore(Double score, boolean createAction) {
        if (createAction) {
            this.basicScore += score;
            return;
        }
        basicScore += score;
    }

    public void updateViewCount() {
        this.boardViewCount++;
    }

    public void updateWishCount(boolean isWish) {
        if (isWish) {
            this.boardWishCount++;
            return;
        }
        boardWishCount--;
    }

    public void updateReviewCount(boolean isCreate) {
        if (isCreate) {
            this.boardReviewCount++;
            return;
        }
        boardReviewCount--;
    }

}
