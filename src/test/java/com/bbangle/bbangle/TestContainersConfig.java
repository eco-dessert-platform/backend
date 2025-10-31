package com.bbangle.bbangle;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Profile("test")
public class TestContainersConfig {

  private static final MariaDBContainer<?> MARIADB_CONTAINER; // 전역 정적 컨테이너 참조(프로세스 전체에서 하나만 사용)

    static { // 클래스 로드 시 한 번 실행되는 정적 초기화 블록
        MARIADB_CONTAINER = new MariaDBContainer<>( // MariaDB Testcontainer 인스턴스 생성 시작
            DockerImageName.parse("mariadb:10.9.3") // 사용할 도커 이미지와 태그 지정
        )
            .withDatabaseName("bakery_test") // 변경: 테스트 전용 DB 이름으로 분리
            .withUsername("test") // 데이터베이스 사용자 이름 설정
            .withPassword("test") // 데이터베이스 비밀번호 설정
            .withStartupTimeout(Duration.ofMinutes(2)) // 컨테이너 시작 대기 시간 설정
            .withEnv("LANG", "en_US.UTF-8") // 컨테이너 내부 로케일을 UTF-8로 설정하여 인코딩 문제 완화
            .withCommand( // 컨테이너 실행 시 전달할 MariaDB 서버 옵션들 설정
                "--character-set-server=utf8mb4", // 기본 문자셋 utf8mb4 설정
                "--collation-server=utf8mb4_unicode_ci", // 정렬 규칙: UCA 기반 collation으로 변경
                "--max_connections=200", // 최대 연결 수 조정
                "--innodb_buffer_pool_size=256M", // InnoDB 버퍼 풀 크기 설정
                "--skip-log-bin",                      // ✅ 바이너리 로그 비활성화
                "--skip-name-resolve",                 // ✅ DNS 역조회 비활성화
                "--performance-schema=OFF",            // ✅ Performance Schema 비활성화
                "--innodb_flush_log_at_trx_commit=2",  // ✅ fsync 완화
                "--skip-ssl"
            )
            .withReuse(false); // 포트 고정 시 재사용 비활성화(테스트 환경에서 재사용하지 않음)

        MARIADB_CONTAINER.start(); // 컨테이너 시작 호출

        // 컨테이너 시작 확인 로그 출력(디버그/정보용)
        System.out.println("================================================================================");
        System.out.println("MariaDB TestContainer started");
        System.out.println("Container ID: " + MARIADB_CONTAINER.getContainerId()); // 컨테이너 ID 출력
        System.out.println("JDBC URL: " + MARIADB_CONTAINER.getJdbcUrl()); // JDBC 접속 URL 출력
        System.out.println("Host: " + MARIADB_CONTAINER.getHost()); // 매핑된 호스트 출력
        System.out.println("Port: " + MARIADB_CONTAINER.getMappedPort(3306)); // 매핑된 포트 출력
        System.out.println("Database: " + MARIADB_CONTAINER.getDatabaseName()); // 데이터베이스 이름 출력
        System.out.println("Username: " + MARIADB_CONTAINER.getUsername()); // 사용자 이름 출력
        System.out.println("================================================================================");
    }

    @Bean
    public MariaDBContainer<?> mariadbContainer() { // 스프링 빈으로 컨테이너 인스턴스 노출
        return MARIADB_CONTAINER; // 정적 컨테이너 객체 반환
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) { // 테스트 실행 시 동적으로 프로퍼티 등록
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl); // 스프링 데이터소스 URL에 컨테이너 JDBC URL 등록
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername); // 사용자명 등록
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword); // 비밀번호 등록
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver"); // 드라이버 클래스명 등록

        // Spring Boot 3.2.1 권장 설정: Hibernate용 MariaDB 방언 등록
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MariaDBDialect");
    }
}
