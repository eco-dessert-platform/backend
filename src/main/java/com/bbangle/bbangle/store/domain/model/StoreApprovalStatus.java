package com.bbangle.bbangle.store.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreApprovalStatus {
    PENDING, REJECT, APPROVE
}
