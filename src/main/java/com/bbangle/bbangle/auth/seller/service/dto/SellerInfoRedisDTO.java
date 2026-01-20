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
            Object id = map.get("id");
            Object role = map.get("role");
            Object status = map.get("status");

            if (id == null || role == null || status == null)
                throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);

            return new SellerInfoRedisDTO(
                Long.valueOf(id.toString()),
                Role.from(role.toString()),
                CertificationStatus.fromDescription(status.toString())
            );
        } catch (IllegalArgumentException e) {
            throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);
        }
    }
}
