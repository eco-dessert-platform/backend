package com.bbangle.bbangle.store.domain;

import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "store_name_request")
@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreNameRequest extends CreatedAtBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_name")
    private String currentName;

    @Column(name = "new_name")
    private String newName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StoreApprovalStatus status;

    @Column(name = "reject_category")
    @Enumerated(EnumType.STRING)
    private StoreNameRejectCategory rejectCategory;

    @Column(name = "reject_detail")
    private String rejectDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder
    private StoreNameRequest(
        String currentName,
        String newName,
        StoreApprovalStatus status,
        StoreNameRejectCategory rejectCategory,
        String rejectDetail,
        Seller seller,
        Store store
    ) {
        this.currentName = currentName;
        this.newName = newName;
        this.status = status;
        this.rejectCategory = rejectCategory;
        this.rejectDetail = rejectDetail;
        this.seller = seller;
        this.store = store;
    }

    public static StoreNameRequest createStoreNameRequest(Store store, Seller seller, String newName) {
        return StoreNameRequest.builder()
            .currentName(store.getName())
            .newName(newName)
            .status(StoreApprovalStatus.PENDING)
            .seller(seller)
            .store(store)
            .build();
    }

    public void approve() {
        validateStatus();
        this.store.updateName(this.currentName, this.newName);
        this.status = StoreApprovalStatus.APPROVE;
    }

    public void reject(StoreNameRejectCategory category, String rejectDetail) {
        validateStatus();
        this.rejectCategory = category;
        this.rejectDetail = rejectDetail;
        this.status = StoreApprovalStatus.REJECT;
    }

    private void validateStatus() {
        if (StoreApprovalStatus.REJECT.equals(this.status)) {
            throw new BbangleException(BbangleErrorCode.REQUEST_IS_REJECTED);
        }

        if (StoreApprovalStatus.APPROVE.equals(this.status)) {
            throw new BbangleException(BbangleErrorCode.REQUEST_IS_APPROVED);
        }
    }
}
