package com.bbangle.bbangle.claim.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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

}
