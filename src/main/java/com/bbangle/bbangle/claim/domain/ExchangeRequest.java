package com.bbangle.bbangle.claim.domain;

import static com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus.APPROVED;
import static com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus.REJECTED;
import static com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus.REQUESTED;
import static com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus.RESHIPPED;

import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
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
@DiscriminatorValue("EXCHANGE")
@Table(name = "exchange_request")
@Entity
public class ExchangeRequest extends Claim {

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ExchangeRequestStatus status;

    private String sellerComment;

    @Builder
    public ExchangeRequest(
        OrderItem orderItem,
        String detailReason,
        LocalDateTime decidedAt,
        ExchangeRequestStatus status,
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

    public void startRedelivery() {
        if (status != APPROVED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = RESHIPPED;
    }

    public void validateReshipped() {
        if (this.status != RESHIPPED) {
            throw new BbangleException(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);
        }
    }

    public void completeExchange() {
        if (status != RESHIPPED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = ExchangeRequestStatus.COMPLETED;
    }
}
