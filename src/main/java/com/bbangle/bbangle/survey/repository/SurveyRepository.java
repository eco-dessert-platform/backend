package com.bbangle.bbangle.survey.repository;

import com.bbangle.bbangle.survey.domain.FoodSurvey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<FoodSurvey, Long> {

    boolean existsByMemberId(Long memberId);
    Optional<FoodSurvey> findByMemberId(Long memberId);

}
