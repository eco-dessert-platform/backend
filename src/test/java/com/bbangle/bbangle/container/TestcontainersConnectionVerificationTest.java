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

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MariaDBContainer<?> mariadbContainer;

    @Test
    @DisplayName("컨테이너 실행 및 연결 확인")
    void testContainerConnection() throws SQLException {
        // 컨테이너 실행 확인
        assertThat(mariadbContainer.isRunning()).isTrue();

        // DataSource 연결 확인
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(5)).isTrue();
        }

        log.info("✅ 컨테이너 실행 및 연결: 정상");
    }

    @Test
    @DisplayName("Character Set 및 Collation 설정 확인")
    void testCharacterSetAndCollation() throws SQLException {
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {

            ResultSet charsetResult = statement.executeQuery(
                "SHOW VARIABLES LIKE 'character_set_server'"
            );
            charsetResult.next();
            String charset = charsetResult.getString("Value");

            ResultSet collationResult = statement.executeQuery(
                "SHOW VARIABLES LIKE 'collation_server'"
            );
            collationResult.next();
            String collation = collationResult.getString("Value");

            assertThat(charset).isEqualTo("utf8mb4");
            assertThat(collation).isEqualTo("utf8mb4_unicode_ci");

            log.info(" Character Set: " + charset + ", Collation: " + collation);
        }
    }

    @Test
    @DisplayName("기본 CRUD 작업 확인")
    void testBasicCRUD() throws SQLException {
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {

            // 테이블 생성
            statement.execute("""
                CREATE TABLE test_table (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);

            // 데이터 삽입
            int insertCount = statement.executeUpdate(
                "INSERT INTO test_table (name) VALUES ('테스트'), ('Test')"
            );

            // 데이터 조회
            ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) as cnt FROM test_table"
            );
            resultSet.next();
            int count = resultSet.getInt("cnt");

            assertThat(insertCount).isEqualTo(2);
            assertThat(count).isEqualTo(2);

            // 정리
            statement.execute("DROP TABLE test_table");

            log.info("CRUD 작업: 정상");
        }
    }
}

