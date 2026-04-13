package com.bbangle.bbangle.claim.domain;

import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.APPROVED;
import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.PICKUP_SCHEDULED;
import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.REJECTED;
import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.REQUESTED;

import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.OrderItem;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DiscriminatorValue("RETURN")
@Table(name = "return_request")
@Entity
public class ReturnRequest extends Claim {

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ReturnRequestRequestStatus status;

    private String sellerComment;

    @Builder
    public ReturnRequest(
        OrderItem orderItem,
        String detailReason,
        LocalDateTime decidedAt,
        ReturnRequestRequestStatus status,
        String sellerComment
    ) {
        super(orderItem, detailReason, decidedAt);
        this.status = status;
        this.sellerComment = sellerComment;
    }

    public void approve(String reason) {
        if (status != REQUESTED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = APPROVED;
        this.sellerComment = reason;
        super.decide();
    }

    public void reject(String reason) {
        if (status != REQUESTED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = REJECTED;
        this.sellerComment = reason;
        super.decide();
    }

    public void startReturnPickup() {
        this.status.validateTransition(ReturnRequestRequestStatus.PICKUP_SCHEDULED);
        this.status = ReturnRequestRequestStatus.PICKUP_SCHEDULED;
    }
    
    public void validatePickupScheduled() {
        if (this.status != PICKUP_SCHEDULED) {
            throw new BbangleException(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);
        }
    }
}
