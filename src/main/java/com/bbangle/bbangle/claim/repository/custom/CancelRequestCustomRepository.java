package com.bbangle.bbangle.claim.repository.custom;

import java.util.List;

public interface CancelRequestCustomRepository {

    Long countCancelsBySeller(List<Long> cancelIds, Long sellerId);
}
