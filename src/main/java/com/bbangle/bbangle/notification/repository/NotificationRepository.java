package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.notification.domain.Notice;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notice, Long>, NotificationQueryDSLRepository {

    Page<Notice> findByIsDeletedFalse(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Notice n
        SET n.isDeleted = true
        WHERE n.admin.id = :adminId AND n.id IN :noticeIds
        """)
    void deleteAllByAdminIdAndIdIn(@Param("adminId") Long adminId, @Param("noticeIds") List<Long> noticeIds);

    @Query("SELECT n FROM Notice n WHERE n.id IN :distinctIds AND n.admin.id = :adminId AND n.isDeleted = false")
    List<Notice> findAllByIdInAndAdminId(@Param("distinctIds") List<Long> distinctIds, @Param("adminId") Long adminId);
}
