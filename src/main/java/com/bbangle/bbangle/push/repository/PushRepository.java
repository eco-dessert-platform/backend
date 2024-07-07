package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushRepository extends JpaRepository<Push, Long>, PushQueryDSLRepository {
}
