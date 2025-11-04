package com.bbangle.bbangle.claim.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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

}
