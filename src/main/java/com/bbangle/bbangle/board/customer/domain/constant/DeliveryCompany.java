package com.bbangle.bbangle.board.customer.domain.constant;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "배송사 Enum 목록", enumAsRef = true)
public enum DeliveryCompany {
    CJ_LOGISTICS("CJ대한통운"),
    LOTTE("롯데택배"),
    HANJIN("한진택배"),
    LOGEN("로젠택배"),
    KOREA_POST("우체국택배"),
    CU_CONVENIENCE("CU편의점택배"),
    GS_CONVENIENCE("GS편의점택배"),
    GDP("GDP당일택배"),
    GOS("GOS당일택배"),
    GPSLOGIX("GPSLOGIX"),
    GS_FRESH("GSFresh"),
    GS_EXPRESS("GS익스프레스"),
    GSMNTON("GSMNTON"),
    KGB("KGB택배"),
    HYUNDAI_LOGISTICS("현대택배"),
    KDEXP("경동택배"),
    DAESIN("대신택배"),
    ILYANG("일양로지스"),
    HPL("합동택배"),
    HANIPS("한의사랑택배"),
    CHUNIL("천일택배"),
    HONAM("호남택배"),
    KG_LOGIS("KG로지스"),
    HPLUS("한플러스택배"),
    SHINHAN("신한택배"),
    KUNYOUNG("건영택배"),
    CVSNET("CVSnet 편의점택배");

    private final String companyName;

    public static DeliveryCompany findByCompanyName(String companyName) {
        for (DeliveryCompany company : values()) {
            if (company.getCompanyName().equals(companyName)) {
                return company;
            }
        }
        throw new BbangleException(BbangleErrorCode.NOTFOUND_DELIVERYCOMPANY);
    }
}
