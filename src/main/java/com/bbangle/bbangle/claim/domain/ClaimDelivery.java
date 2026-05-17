package com.bbangle.bbangle.claim.domain;

import com.bbangle.bbangle.claim.domain.constant.ClaimDeliveryType;
import com.bbangle.bbangle.claim.domain.constant.ClaimShippingStatus;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "claim_delivery")
@Entity
public class ClaimDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id")
    private Claim claim;

    @Column(name = "delivery_type", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ClaimDeliveryType deliveryType;

    @Embedded
    private Sender sender;

    @Embedded
    private Receiver receiver;

    @Embedded
    private Shipping shipping;

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ClaimShippingStatus status;

    private LocalDateTime collectedAt;

    private ClaimDelivery(Claim claim, ClaimDeliveryType deliveryType, Shipping shipping, ClaimShippingStatus status) {
        this.claim = claim;
        this.deliveryType = deliveryType;
        this.shipping = shipping;
        this.status = status;
    }

    public static ClaimDelivery createReturnPickup(Claim claim, CourierCompany courierCode, String trackingNumber) {
        return new ClaimDelivery(
            claim,
            ClaimDeliveryType.RETURN_PICKUP,
            Shipping.scheduled(courierCode.getDisplayName(), trackingNumber),
            ClaimShippingStatus.IN_TRANSIT
        );
    }

    public static ClaimDelivery createExchangeRedelivery(Claim claim, CourierCompany courierCode, String trackingNumber) {
        return new ClaimDelivery(
            claim,
            ClaimDeliveryType.EXCHANGE_REDELIVERY,
            Shipping.scheduled(courierCode.getDisplayName(), trackingNumber),
            ClaimShippingStatus.IN_TRANSIT
        );
    }
    
    public void updateInvoice(CourierCompany courierCode, String trackingNumber) {
        this.shipping.modifyShippingInfo(courierCode.getDisplayName(), trackingNumber);
    }

}
