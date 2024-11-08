package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.board.domain.composityKey.RecommendationSimilarBoardComposityKey;
import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
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
    private Long queryItem;

    private Long recommendationItem;

    private BigDecimal score;

    @Id
    private Integer rank;

    @Column(length = 30, columnDefinition = "varchar")
    @Enumerated(value = EnumType.STRING)
    private SimilarityTypeEnum recommendationTheme;

    @Column(length = 30, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private SimilarityModelVerEnum modelVersion;
}