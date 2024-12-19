package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.RecommendLearningConfig;
import com.bbangle.bbangle.board.domain.RedisKeyEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationLearningRepository extends JpaRepository<RecommendLearningConfig, RedisKeyEnum> {

}
