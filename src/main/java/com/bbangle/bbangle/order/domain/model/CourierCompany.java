package com.bbangle.bbangle.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourierCompany {

    CJ_LOGISTICS("CJ대한통운"),
    LOTTE("롯데택배"),
    HANJIN("한진택배"),
    LOGEN("로젠택배"),
    POST("우체국택배"),
    CJ_INTERNATIONAL("CJ대한통운(국제택배)"),
    CU_POST("CU편의점택배"),
    GOP_SAME_DAY("GOP당일택배"),
    GOS_SAME_DAY("GOS당일택배"),
    GPS_LOGIX("GPSLOGIX"),
    GS_FRESH("GSFresh"),
    GSI_EXPRESS("GSI익스프레스"),
    GSMNTON("GSMNTON"),
    GS_POSTBOX_QUICK("GSPostbox퀵"),
    GS_POSTBOX("GSPostbox택배"),
    ETC("기타"),
    NONE("-"); // 정보가 없는 경우

    private final String displayName;

    /**
     * 한글 명칭으로 Enum 상수를 찾는 유틸리티 메서드
     */
    public static CourierCompany fromDisplayName(String text) {
        for (CourierCompany company : CourierCompany.values()) {
            if (company.displayName.equals(text)) {
                return company;
            }
        }
        return ETC; // 매칭되는 게 없을 경우 기타 반환
    }
}
