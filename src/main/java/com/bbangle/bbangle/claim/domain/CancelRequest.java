package com.bbangle.bbangle.claim.domain;

import static com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus.APPROVED;
import static com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus.REJECTED;
import static com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus.REQUESTED;

import com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus;
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
@DiscriminatorValue("CANCEL")
@Table(name = "cancel_request")
@Entity
public class CancelRequest extends Claim {

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private CancelRequestStatus status;

    private String sellerComment;

    @Builder
    public CancelRequest(
        OrderItem orderItem,
        String detailReason,
        LocalDateTime decidedAt,
        CancelRequestStatus status
    ) {
        super(orderItem, detailReason, decidedAt);
        this.status = status;
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
}
