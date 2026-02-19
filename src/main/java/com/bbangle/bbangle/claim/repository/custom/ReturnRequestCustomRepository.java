package com.bbangle.bbangle.claim.repository.custom;

import java.util.List;

public interface ReturnRequestCustomRepository {

    Long countReturnsBySeller(List<Long> returnIds, Long sellerId);

}
