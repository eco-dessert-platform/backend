package com.bbangle.bbangle.order.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderDeliveryStatus {
    NONE("미지정"),
    PREPARING("상품준비"),
    PICKING_UP("수거중"),
    PICKED_UP("수거완료"),
    DELIVERING("배송중"),
    DELIVERED("배송완료");

    private final String description;

    // description 으로 enum 찾기, 한글 설명으로 상태를 가져오는 메서드
    public static OrderDeliveryStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }
}
