package com.bbangle.bbangle.auth.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[통합테스트] OAuthSellerService 동시성 테스트")
@SpringBootTest
@ActiveProfiles("test")
class OAuthSellerServiceConcurrencyTest {

    @Autowired
    private OAuthSellerService service;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private TokenProvider tokenProvider;

    // 모든 스레드가 SellerId=1L, Role=Role.ROLE_SELLER인 row에 접근하도록 설정
    @BeforeEach
    void setUp() {
        given(tokenProvider.generateToken(any(), any(), any()))
            .willAnswer(inv -> UUID.randomUUID().toString());

        refreshTokenRepository.save(
            RefreshToken.create(1L, Role.ROLE_SELLER, "init")
        );
    }

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("Refresh Token 생성 및 업데이트 레이스 컨디션 테스트")
    void generateRefreshToken_concurrent() throws InterruptedException {

        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<String> tokens = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    tokens.add(service.generateRefreshToken(1L, Role.ROLE_SELLER));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        startLatch.countDown();
        latch.await();
        executor.shutdown();

        // then

        // DB에는 Token이 1개만 존재해야 함
        List<RefreshToken> dbTokens = refreshTokenRepository.findAll();
        assertThat(dbTokens).hasSize(1);

        // 모든 스레드가 같은 SellerId + Role을 통해 같은 row를 업데이트 함
        // 따라서 DB의 토큰은 스레드 작업 후 생성된 토큰 리스트에서 가장 마지막 토큰과 일치해야한다.
        assertThat(dbTokens.get(0).getRefreshToken()).isEqualTo(tokens.get(tokens.size() - 1));
    }
}