package com.bbangle.bbangle;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("test")
public class TestContainersConfig {

    // 전역 정적 컨테이너 참조(프로세스 전체에서 하나만 사용)
    private static final MariaDBContainer<?> mariadbContainer;
    private static final GenericContainer<?> redisContainer;

    static {
        mariadbContainer = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.9.3"))
            .withDatabaseName("bakery")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withEnv("LANG", "en_US.UTF-8")
            .withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--max_connections=200",
                "--innodb_buffer_pool_size=256M",
                "--skip-log-bin",
                "--skip-name-resolve",
                "--performance-schema=OFF",
                "--innodb_flush_log_at_trx_commit=2",
                "--skip-ssl"
            )
            .withReuse(false); // 재사용 비활성화 , 단 차후 테스트 코드의 양이 많아진다면 true로 변경 고려

        // ✅ Redis 컨테이너 설정 (GenericContainer 사용)
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(Duration.ofSeconds(60))
            .withReuse(false) // 재사용 비활성화
            .waitingFor(Wait.forListeningPort()); // redis 실행될 때 까지 대기

    }

    @Bean
    @ServiceConnection
    public MariaDBContainer<?> mariadbContainer() {
        return mariadbContainer;
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return redisContainer;
    }

}
