package com.bbangle.bbangle;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.localstack.LocalStackContainer;
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
    private static final LocalStackContainer localStackContainer;

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

        // 3. LocalStack 설정
        localStackContainer = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4.0"))
            .withServices(LocalStackContainer.Service.S3)
            .withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(false);

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

    @Bean
    public LocalStackContainer localStackContainer() {
        return localStackContainer;
    }

    @Bean
    @Primary
    public AmazonS3 amazonS3() {
        // [핵심 수정] 안전장치 추가: 컨테이너가 실행 중이 아니라면 여기서 확실히 시작시킵니다.
        if (!localStackContainer.isRunning()) {
            localStackContainer.start();
        }

        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                // getEndpointOverride는 컨테이너가 Running 상태일 때만 호출 가능
                localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
                localStackContainer.getRegion()
            ))
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(localStackContainer.getAccessKey(),
                    localStackContainer.getSecretKey())
            ))
            .withPathStyleAccessEnabled(true)
            .build();
    }

    @Bean
    public Boolean initS3Bucket(AmazonS3 amazonS3,
        @Value("${cloud.aws.s3.bucket}") String bucketName) {
        try {
            if (!amazonS3.doesBucketExistV2(bucketName)) {
                amazonS3.createBucket(bucketName);
            }
        } catch (Exception e) {
            System.out.println(" Bucket creation failed: " + e.getMessage());
        }
        return true;
    }

}
