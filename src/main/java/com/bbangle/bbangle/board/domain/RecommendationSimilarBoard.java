package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "recommendation_similar_board")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(RecommendationSimilarBoardComposityKey.class)
@ToString
public class RecommendationSimilarBoard extends CreatedAtBaseEntity {

    @Id
    @Column(name = "query_item")
    private Long queryItem;

    @Id
    @Column(name = "rank")
    private Integer rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_item", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Board board;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "recommendation_theme", length = 30, columnDefinition = "varchar")
    @Enumerated(value = EnumType.STRING)
    private SimilarityTypeEnum recommendationTheme;

    @Column(name = "model_version")
    private String modelVersion;

    private RecommendationSimilarBoard(Long queryItem, Integer rank, Long boardId, BigDecimal score, SimilarityTypeEnum recommendationTheme, String modelVersion) {
        this.queryItem = queryItem;
        this.rank = rank;
        this.board = Board.builder().id(boardId).build();
        this.score = score;
        this.recommendationTheme = recommendationTheme;
        this.modelVersion = modelVersion;
    }

    public static RecommendationSimilarBoard create(Long queryItem, Integer rank, Long boardId, BigDecimal score, SimilarityTypeEnum recommendationTheme, String modelVersion) {
        return new RecommendationSimilarBoard(
            queryItem,
            rank,
            boardId,
            score,
            recommendationTheme,
            modelVersion
        );
    }

}