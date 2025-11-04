package com.bbangle.bbangle.claim.domain;

import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
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
@DiscriminatorValue("EXCHANGE")
@Table(name = "exchange_request")
@Entity
public class ExchangeRequest extends Claim {

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ExchangeRequestStatus status;

}
