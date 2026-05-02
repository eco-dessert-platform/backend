package com.bbangle.bbangle.auth.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] OAuthSellerService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuthSellerServiceIntegrationTest {

    @Autowired
    private OAuthSellerService service;

    @Autowired
    private RedisRepository redisRepository;

    @Test
    @DisplayName("Redis에 저장된 SellerInfo를 조회하고 즉시 삭제한다.")
    void success_getSellerInfoFromRedis() {

        // given
        OAuth2DTO.InfoDTO sellerInfo = OAuth2DTO.InfoDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .status(CertificationStatus.NEW)
            .build();

        redisRepository.setFromDTO(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code", sellerInfo, Duration.ofMinutes(5));

        // when
        OAuth2DTO.InfoDTO result = service.getSellerInfoFromRedis("code");

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(result.status()).isEqualTo(CertificationStatus.NEW);

        assertThat(redisRepository.getDTOAndDelete(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code", OAuth2DTO.InfoDTO.class))
            .isNull();
    }

    @Test
    @DisplayName("Redis에 SellerInfo가 없으면 UNAUTHORIZED 예외")
    void failure_getSellerInfoFromRedis() {

        // when & then
        assertThatThrownBy(() -> service.getSellerInfoFromRedis("code"))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode._UNAUTHORIZED.getMessage());
    }
}