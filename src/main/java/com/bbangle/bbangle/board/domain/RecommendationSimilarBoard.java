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
    @Column(name = "query_item")
    Long queryItem;

    @Column(name = "recommendation_item")
    Long recommendationItem;

    BigDecimal score;

    @Id
    Integer rank;

    @Column(name = "recommendation_theme")
    @Enumerated(EnumType.STRING)
    SimilarityTypeEnum recommendationTheme;

    @Column(name = "model_version")
    @Enumerated(EnumType.STRING)
    SimilarityModelVerEnum modelVersion;
}