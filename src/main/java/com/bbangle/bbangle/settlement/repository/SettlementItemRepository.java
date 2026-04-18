package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 건별 정산(SettlementItem) JPA 레포지토리.
 * SettlementItemDSLRepository를 상속하여 QueryDSL 커스텀 메서드를 제공한다.
 */
public interface SettlementItemRepository
    extends JpaRepository<SettlementItem, Long>, SettlementItemDSLRepository {

}
