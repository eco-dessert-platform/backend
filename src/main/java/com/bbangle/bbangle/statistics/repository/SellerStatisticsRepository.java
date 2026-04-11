package com.bbangle.bbangle.statistics.repository;

import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerStatisticsRepository extends JpaRepository<SellerStatisticsDaily, Long> {

    List<SellerStatisticsDaily> findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
        Long sellerId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
    );
}
