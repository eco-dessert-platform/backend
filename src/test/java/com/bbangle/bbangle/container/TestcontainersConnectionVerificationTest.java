package com.bbangle.bbangle.container;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;


@ActiveProfiles("test")
@SpringBootTest
@DisplayName("테스트 컨테이너 연결 검증")
@Slf4j
public class TestcontainersConnectionVerificationTest {

    @Autowired // 스프링이 DataSource 빈을 주입하도록 표시
    private DataSource dataSource; // 테스트에서 사용할 DataSource 필드 선언

    @Autowired // 스프링이 MariaDBContainer 빈을 주입하도록 표시
    private MariaDBContainer<?> mariadbContainer; // Testcontainers의 MariaDB 컨테이너 필드 선언

    @Test // 해당 메서드가 테스트 메서드임을 표시
    @DisplayName("컨테이너 실행 및 연결 확인")
        // 테스트 메서드의 표시 이름 설정
    void testContainerConnection() throws SQLException { // 테스트 메서드 선언, SQLException을 던질 수 있음
        // 컨테이너 실행 확인
        assertThat(mariadbContainer.isRunning()).isTrue(); // 컨테이너가 실행 중인지 AssertJ로 검증

        // DataSource 연결 확인
        try (Connection connection = dataSource.getConnection()) { // DataSource로부터 커넥션을 얻어 try-with-resources로 자동 종료
            assertThat(connection.isValid(5)).isTrue(); // 커넥션이 유효한지(타임아웃 5초) 검증
        }

        log.info("✅ 컨테이너 실행 및 연결: 정상"); // 성공 로그 출력
    }

    @Test // 해당 메서드가 테스트 메서드임을 표시
    @DisplayName("Character Set 및 Collation 설정 확인")
        // 테스트 메서드의 표시 이름 설정
    void testCharacterSetAndCollation() throws SQLException { // 테스트 메서드 선언, SQLException을 던질 수 있음
        try (Connection connection = dataSource.getConnection(); // DataSource에서 커넥션 획득
            Statement statement = connection.createStatement()) { // Statement 생성, try-with-resources로 자동 종료

            ResultSet charsetResult = statement.executeQuery( // 서버 문자셋 변수를 조회하는 쿼리 실행
                "SHOW VARIABLES LIKE 'character_set_server'"
            );
            charsetResult.next();
            String charset = charsetResult.getString("Value");

            ResultSet collationResult = statement.executeQuery(
                "SHOW VARIABLES LIKE 'collation_server'"
            );
            collationResult.next();
            String collation = collationResult.getString("Value");

            assertThat(charset).isEqualTo("utf8mb4"); // 문자셋이 utf8mb4인지 검증
            assertThat(collation).isEqualTo("utf8mb4_unicode_ci"); // 컬레이션이 utf8mb4_unicode_ci인지 검증

            log.info(" Character Set: " + charset + ", Collation: " + collation); // 조회 결과 로그 출력
        }
    }

    @Test // 해당 메서드가 테스트 메서드임을 표시
    @DisplayName("기본 CRUD 작업 확인")
        // 테스트 메서드의 표시 이름 설정
    void testBasicCRUD() throws SQLException { // 테스트 메서드 선언, SQLException을 던질 수 있음
        try (Connection connection = dataSource.getConnection(); // DataSource에서 커넥션 획득
            Statement statement = connection.createStatement()) { // Statement 생성, try-with-resources로 자동 종료

            // 테이블 생성
            // 멀티라인 텍스트 블록을 사용해 SQL DDL을 실행
            statement.execute(""" 
                CREATE TABLE test_table (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """); // 텍스트 블록 종료 및 실행

            // 데이터 삽입
            int insertCount = statement.executeUpdate(
                "INSERT INTO test_table (name) VALUES ('테스트'), ('Test')"
            );

            // 데이터 조회
            ResultSet resultSet = statement.executeQuery( // COUNT 쿼리 실행
                "SELECT COUNT(*) as cnt FROM test_table"
            );
            resultSet.next();
            int count = resultSet.getInt("cnt");

            assertThat(insertCount).isEqualTo(2);
            assertThat(count).isEqualTo(2);

            // 정리
            statement.execute("DROP TABLE test_table"); // 생성한 테스트 테이블을 삭제

            log.info("CRUD 작업: 정상"); // 성공 로그 출력
        }
    }
}
