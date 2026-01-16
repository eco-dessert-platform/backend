package com.bbangle.bbangle.auth.seller.service.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.Map;

public record SellerInfoRedisDTO(
    Long id,
    Role role,
    CertificationStatus status
) {

    public static SellerInfoRedisDTO fromMap(Map<Object, Object> map) {
        try {
            return new SellerInfoRedisDTO(
                Long.valueOf(map.get("id").toString()),
                Role.from(map.get("role").toString()),
                CertificationStatus.fromDescription(map.get("status").toString())
            );
        } catch (Exception e) {
            throw new BbangleException(BbangleErrorCode.UNAUTHORIZED);
        }
    }
}
