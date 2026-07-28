# Bbangle 멀티 모듈 프로젝트 설계

> 현재 모놀리식 Spring Boot 프로젝트를 Gradle 멀티 모듈로 전환하기 위한 설계 문서.

---

## 1. 전환 목적

| 현재 문제 | 멀티 모듈로 해결 |
|-----------|----------------|
| 도메인 간 경계 없이 자유로운 참조 | 모듈 의존성으로 아키텍처 강제 |
| seller/admin/customer 경계가 컨벤션에만 의존 | 컴파일 타임에 역할 분리 강제 |
| 단일 빌드 → 전체 재빌드 | 변경된 모듈만 재빌드 |
| 서버 분리 불가 | 역할 모듈 단위로 독립 배포 가능 |

---

## 2. 모듈 구조

```
bbangle/ (루트)
├── settings.gradle
├── build.gradle
│
├── bbangle-global-utils/     # 순수 유틸리티
├── bbangle-domain/           # JPA 엔티티, Repository, QueryDSL, 외부 클라이언트
├── bbangle-admin/            # 관리자: Service + Facade + Controller + DTO
├── bbangle-seller/           # 판매자: Service + Facade + Controller + DTO
├── bbangle-customer/         # 고객:   Service + Facade + Controller + DTO
└── bbangle-api/              # Spring Boot 진입점 (서버 합칠 때만)
```

### 의존성 흐름 (단방향)

```
bbangle-global-utils
      ↑
bbangle-domain
      ↑
┌─────┼──────┐
admin seller customer
└─────┼──────┘
      ↑
bbangle-api  ← 합칠 때만. 분리 시 각 역할 모듈이 독립 서버
```

### 서버 합칠 때 vs 분리할 때

**합칠 때 (현재)**
```
global-utils → domain → admin
                      → seller    → bbangle-api (단일 서버)
                      → customer
```

**분리할 때 (미래)**
```
global-utils → domain → admin    (독립 서버)
                      → seller   (독립 서버)
                      → customer (독립 서버)
```
각 역할 모듈에 `@SpringBootApplication`을 추가하면 독립 배포 가능.

---

## 3. 각 모듈 상세

### 3.1 `bbangle-global-utils`

**책임**: 프레임워크/도메인과 무관한 순수 유틸리티.

```
com.bbangle.bbangle.common.util
├── AesEncryptionUtil.java
├── CookieUtils.java
├── CsvUtil.java
├── HtmlUtils.java
├── SecurityUtils.java
└── VisitorFingerprintUtils.java
```

```gradle
// bbangle-global-utils/build.gradle
dependencies {
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

### 3.2 `bbangle-domain`

**책임**: 도메인 모델 + 인프라 구현 전담.
엔티티, Repository 인터페이스, QueryDSL 구현체, Redis, 외부 서비스 클라이언트, Flyway.

```
com.bbangle.bbangle.common
└── domain/
    ├── BaseEntity.java
    ├── CreatedAtBaseEntity.java
    └── DomainException.java          # entity/repository 레벨 예외

com.bbangle.bbangle
├── board/
│   ├── domain/Board.java, BoardDetail.java, Product.java
│   ├── repository/BoardRepository.java
│   └── repository/BoardQueryDSLRepositoryImpl.java
├── order/
│   ├── domain/Order.java, OrderItem.java, OrderDelivery.java, OrderStatus.java
│   ├── repository/OrderRepository.java
│   └── repository/OrderDSLRepositoryImpl.java
├── claim/
│   ├── domain/Claim.java, ExchangeRequest.java, ReturnRequest.java, CancelRequest.java
│   ├── domain/ExchangeRequestStatus.java
│   └── repository/ClaimDSLRepositoryImpl.java
├── member/domain/, seller/domain/, store/domain/
├── review/domain/, wishlist/domain/, payment/domain/
├── notification/domain/, settlement/domain/
├── ... (기타 도메인)
│
├── common/redis/RedisRepositoryImpl.java
├── image/S3ImageService.java
├── push/FirebasePushService.java
├── auth/oauth/kakao/KakaoApiClient.java
├── auth/oauth/google/GoogleApiClient.java
└── seller/TossAccountVerificationClient.java
```

Flyway 마이그레이션:
```
bbangle-domain/src/main/resources/db/migration/
├── V1__init.sql
└── ...
```

```gradle
// bbangle-domain/build.gradle
dependencies {
    implementation project(':bbangle-global-utils')

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'

    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"

    implementation 'com.amazonaws:aws-java-sdk-s3:1.11.238'
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    implementation 'com.squareup.okhttp3:okhttp:4.2.2'
    implementation 'org.flywaydb:flyway-mysql'
    implementation 'org.flywaydb:flyway-core'
    implementation 'net.ttddyy:datasource-proxy:1.10.1'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}

// QueryDSL Q클래스 생성
def generated = 'src/main/generated'
tasks.withType(JavaCompile) {
    options.getGeneratedSourceOutputDirectory().set(file(generated))
}
sourceSets { main.java.srcDirs += [generated] }
clean { delete file(generated) }
```

---

### 3.3 `bbangle-admin`

**책임**: 관리자 기능 전담. Service + Facade + Controller + DTO + 예외 처리.

```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   ├── exception/AdminException.java
│   └── exception/GlobalControllerAdvice.java
├── board/
│   ├── controller/AdminBoardController.java
│   ├── service/AdminBoardService.java
│   └── facade/AdminBoardFacade.java
├── seller/
│   ├── controller/AdminSellerController.java
│   ├── service/AdminSellerService.java
│   └── facade/AdminSellerFacade.java
├── member/controller/AdminMemberController.java
├── settlement/
│   ├── controller/SellerSettlementController.java
│   ├── service/SellerSettlementService.java
│   └── service/SellerSettlementExcelService.java
└── statistics/service/AdminStatisticsService.java
```

```gradle
// bbangle-admin/build.gradle
dependencies {
    implementation project(':bbangle-domain')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    implementation 'org.apache.poi:poi:5.2.5'
    implementation 'org.apache.poi:poi-ooxml:5.2.5'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

### 3.4 `bbangle-seller`

**책임**: 판매자 기능 전담. Service + Facade + Controller + DTO + 예외 처리.

```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   ├── exception/SellerException.java
│   └── exception/GlobalControllerAdvice.java
├── seller/
│   ├── controller/SellerController.java
│   ├── service/SellerService.java
│   └── facade/SellerFacade.java
├── order/
│   ├── controller/SellerOrderController.java
│   └── service/SellerOrderService.java
├── claim/
│   ├── controller/SellerExchangeController.java, SellerReturnController.java, SellerCancelController.java
│   ├── service/ExchangeService.java, ReturnService.java, CancelService.java
│   └── facade/ClaimFacade.java
├── store/controller/SellerStoreController.java
└── statistics/service/SellerStatisticsService.java
```

```gradle
// bbangle-seller/build.gradle
dependencies {
    implementation project(':bbangle-domain')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    implementation 'org.mapstruct:mapstruct:1.5.3.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.3.Final'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

### 3.5 `bbangle-customer`

**책임**: 고객 기능 전담. Service + Facade + Controller + DTO + 예외 처리.

```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   ├── exception/CustomerException.java
│   └── exception/GlobalControllerAdvice.java
├── member/
│   ├── controller/MemberController.java, ProfileController.java
│   └── service/MemberService.java
├── board/
│   ├── controller/BoardDetailController.java
│   └── service/BoardService.java
├── review/controller/ReviewController.java, service/ReviewService.java
├── wishlist/service/WishlistService.java
├── search/service/SearchService.java
├── notification/service/NotificationService.java
└── auth/
    ├── controller/CustomerTokenApiController.java
    └── service/CustomerTokenService.java
```

```gradle
// bbangle-customer/build.gradle
dependencies {
    implementation project(':bbangle-domain')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

### 3.6 `bbangle-api` (서버 합칠 때만)

**책임**: Spring Boot 진입점, 보안 설정 통합.

```
com.bbangle.bbangle
├── BbangleApplication.java
└── config/
    ├── SecurityConfig.java
    ├── OAuth2SecurityConfig.java
    ├── SwaggerConfig.java
    ├── RedisConfig.java
    ├── S3Config.java
    ├── FirebaseConfig.java
    ├── QueryDslConfig.java
    └── WebConfig.java
```

```gradle
// bbangle-api/build.gradle
plugins {
    id 'org.springframework.boot'
}

dependencies {
    implementation project(':bbangle-admin')
    implementation project(':bbangle-seller')
    implementation project(':bbangle-customer')

    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    implementation 'io.jsonwebtoken:jjwt:0.9.1'
    implementation 'javax.xml.bind:jaxb-api:2.3.1'
    implementation 'net.logstash.logback:logstash-logback-encoder:7.4'

    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client:3.2.0'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

---

## 4. Gradle 설정

### `settings.gradle`

```gradle
rootProject.name = 'Bbangle'

include 'bbangle-global-utils'
include 'bbangle-domain'
include 'bbangle-admin'
include 'bbangle-seller'
include 'bbangle-customer'
include 'bbangle-api'
```

### 루트 `build.gradle`

```gradle
buildscript {
    ext {
        springBootVersion = '3.2.1'
        queryDslVersion = '5.0.0'
    }
}

plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1' apply false
    id 'io.spring.dependency-management' version '1.1.4' apply false
    id 'jacoco'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.bbangle'
    version = '0.0.1-SNAPSHOT'

    java { sourceCompatibility = '17' }

    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }

    dependencyManagement {
        imports {
            mavenBom "org.springframework.boot:spring-boot-dependencies:${springBootVersion}"
        }
    }

    configurations {
        compileOnly { extendsFrom annotationProcessor }
    }

    test {
        useJUnitPlatform()
        finalizedBy 'jacocoTestReport'
    }

    dependencies {
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testCompileOnly 'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'
        testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.9'
        testImplementation 'net.datafaker:datafaker:2.1.0'
    }
}
```

---

## 5. 주요 결정 사항

| 결정 | 이유 |
|------|------|
| 역할 기반 모듈 분리 (admin/seller/customer) | 서버 분리 옵션 열어둠. 컴파일 타임에 역할 경계 강제 |
| domain 단일 모듈 (core/infra 미분리) | 엔티티와 QueryDSL 구현체가 항상 같이 변경됨. 분리 실익 없음 |
| global-utils는 순수 유틸만 | 도메인/프레임워크 의존 없이 어디서든 재사용 가능 |
| 역할 모듈에 Controller + DTO + 예외까지 포함 | HTTP 관심사는 역할 모듈에 집중 |
| domain에 DomainException 하나만 정의 | entity/repository 레벨 예외만 도메인이 알면 됨 |
| Flyway를 domain에 위치 | DB 스키마 소유권을 domain이 가짐 |
| 도메인별 모듈 분리 안 함 | 도메인 간 결합이 높아 현재 규모에서 과함 |

---

## 6. 현재 도메인 → 역할 모듈 매핑

| 도메인 | admin | seller | customer |
|--------|:-----:|:------:|:--------:|
| board | ✓ (상품 관리) | | ✓ (조회) |
| order | | ✓ | ✓ |
| claim (교환/반품/취소) | | ✓ | ✓ |
| member | ✓ (관리) | | ✓ (본인) |
| seller | ✓ (관리) | ✓ (본인) | |
| store | | ✓ | ✓ (조회) |
| review | | | ✓ |
| wishlist | | | ✓ |
| search | | | ✓ |
| notification | | ✓ | ✓ |
| settlement | | ✓ | |
| statistics | ✓ | ✓ | |
| charge | ✓ | | |
| auth | | ✓ | ✓ |
| survey | | | ✓ |
| preference | | | ✓ |
| image | | ✓ | ✓ |
