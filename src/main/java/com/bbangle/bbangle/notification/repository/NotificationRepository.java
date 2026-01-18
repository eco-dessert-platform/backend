package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.notification.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notice, Long>, NotificationQueryDSLRepository {

    @Query("SELECT n FROM Notice n")
    Page<Notice> searchNoticeAll(Pageable pageable);

}
