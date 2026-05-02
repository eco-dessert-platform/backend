package com.bbangle.bbangle.auth.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] OAuthSellerService")
@ExtendWith(MockitoExtension.class)
class OAuthSellerServiceUnitTest {

    @InjectMocks
    private OAuthSellerService service;

    @Mock
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

        given(
            redisRepository.getDTOAndDelete(
                OAuthSellerService.OAUTH_CODE_NAMESPACE,
                "code",
                OAuth2DTO.InfoDTO.class
            )
        ).willReturn(sellerInfo);

        // when
        OAuth2DTO.InfoDTO result = service.getSellerInfoFromRedis("code");

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(result.status()).isEqualTo(CertificationStatus.NEW);
    }

    @Test
    @DisplayName("Redis로부터 Seller의 정보 조회를 실패한다.")
    void failure_getSellerInfoFromRedis() {

        // given
        given(
            redisRepository.getDTOAndDelete(
                OAuthSellerService.OAUTH_CODE_NAMESPACE,
                "code",
                OAuth2DTO.InfoDTO.class
            )
        ).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getSellerInfoFromRedis("code"))
            .isInstanceOf(BbangleException.class);
    }
}