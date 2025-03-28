package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import org.springframework.data.repository.CrudRepository;

public interface RecommendationSimilarBoardMemoryRepository extends CrudRepository<RecommendBoardConfig, RedisKeyEnum> {

}
