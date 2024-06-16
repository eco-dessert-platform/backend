package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.notification.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notice, Long>, NotificationQueryDSLRepository {

}
