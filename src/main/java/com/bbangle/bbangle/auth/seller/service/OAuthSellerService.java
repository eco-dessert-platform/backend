package com.bbangle.bbangle.auth.seller.service;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthSellerService {

    public static final String OAUTH_CODE_NAMESPACE = "oauth2:code";

    private final RedisRepository redisRepository;

    public OAuth2DTO.InfoDTO getSellerInfoFromRedis(String code) {
        OAuth2DTO.InfoDTO sellerInfo;
        try {
            sellerInfo = redisRepository.getDTOAndDelete(OAUTH_CODE_NAMESPACE, code, OAuth2DTO.InfoDTO.class);
        } catch (Exception e) {
            log.warn("Redis 조회/삭제 실패 : ", e);
            throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);
        }

        if (sellerInfo == null) throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);

        return sellerInfo;
    }
}
