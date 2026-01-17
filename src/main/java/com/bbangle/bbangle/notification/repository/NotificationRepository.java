package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.notification.domain.Notice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notice, Long>, NotificationQueryDSLRepository {

    Optional<Notice> findByIdAndAdminId(Long noticeId,Long adminId);
}
