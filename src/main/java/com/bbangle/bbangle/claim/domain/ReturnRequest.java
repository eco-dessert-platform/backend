package com.bbangle.bbangle.claim.domain;

import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.APPROVED;
import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.REJECTED;
import static com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus.REQUESTED;

import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

    public void approve(String reason) {
        if (status != REQUESTED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = APPROVED;
        this.getOrderItem().returnApprove();
    }

    public void reject(String reason) {
        if (status != REQUESTED) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
        this.status = REJECTED;
        this.getOrderItem().returnReject();
    }
}
