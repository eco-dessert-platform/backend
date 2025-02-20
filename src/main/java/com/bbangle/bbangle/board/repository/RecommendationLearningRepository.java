package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendLearningConfig;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import org.springframework.data.repository.CrudRepository;

public interface RecommendationLearningRepository extends CrudRepository<RecommendLearningConfig, RedisKeyEnum> {

}
