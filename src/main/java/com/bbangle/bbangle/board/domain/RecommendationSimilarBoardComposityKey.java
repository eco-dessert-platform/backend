package com.bbangle.bbangle.board.domain;

import java.io.Serializable;
import java.util.Objects;

public class RecommendationSimilarBoardComposityKey implements Serializable {

    private Long queryItem;
    private Integer rank;

    public RecommendationSimilarBoardComposityKey() {
    }

    public RecommendationSimilarBoardComposityKey(Long queryItem, Integer rank) {
        this.queryItem = queryItem;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecommendationSimilarBoardComposityKey that = (RecommendationSimilarBoardComposityKey) o;
        return Objects.equals(queryItem, that.queryItem) &&
            Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queryItem, rank);
    }
}
