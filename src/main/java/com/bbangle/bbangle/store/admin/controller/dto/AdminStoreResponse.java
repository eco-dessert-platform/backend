package com.bbangle.bbangle.store.admin.controller.dto;

import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import java.util.List;
import lombok.Builder;

public class AdminStoreResponse {

    @Builder
    public record UpdateStoreNameRequest(
        List<UpdateStoreNames> updateStoreNames,
        long totalElements,
        int totalPages,
        boolean hasPrevious,
        boolean hasNext
    ) {}
}
