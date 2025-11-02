package com.bbangle.bbangle;

import java.time.Duration;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("test")
public class TestContainersConfig {

    private static MariaDBContainer<?> mariadbContainer; // 전역 정적 컨테이너 참조(프로세스 전체에서 하나만 사용)

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
            .withReuse(false);
        mariadbContainer.start(); // ✅ 명시적으로 시작
    }

    @Bean
    @ServiceConnection
    public MariaDBContainer<?> mariadbContainer() {
        return mariadbContainer;
    }


    // 선택: 컨테이너가 실제로 뜬 뒤 정보 로그 출력 (디버깅용)
    @Bean
    public ApplicationRunner logMariaInfo(MariaDBContainer<?> mariadbContainer) {
        return args -> {
            System.out.println("================================================================================");
            System.out.println("📦 MariaDB TestContainer Application Context Loaded");
            System.out.println("Container ID: " + mariadbContainer.getContainerId());
            System.out.println("JDBC URL: " + mariadbContainer.getJdbcUrl());
            System.out.println("Host: " + mariadbContainer.getHost());
            System.out.println("Port: " + mariadbContainer.getMappedPort(3306));
            System.out.println("Database: " + mariadbContainer.getDatabaseName());
            System.out.println("Username: " + mariadbContainer.getUsername());
            System.out.println("Is Running: " + mariadbContainer.isRunning());
            System.out.println("================================================================================");
        };
    }
}
