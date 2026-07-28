# 멀티 모듈 전환 제안

---

## AS-IS

단일 모듈 안에 모든 코드가 공존. 역할 경계가 패키지 컨벤션에만 의존.

```
src/main/java/com/bbangle/bbangle/
├── board/
├── order/
├── claim/
├── seller/      # seller 코드가 customer 코드를 참조해도 컴파일 오류 없음
├── member/
├── auth/
├── config/
└── ...
```

**문제:**
- admin / seller / customer 간 경계를 코드로 강제할 수 없음
- 코드 변경 시 전체 재빌드
- 나중에 서버를 분리하려면 대규모 리팩토링 필요

---

## TO-BE

역할 단위로 모듈 분리. 모듈 의존성이 곧 아키텍처 규칙.

```
bbangle-global-utils   # 순수 유틸 (암호화, 쿠키, CSV 등)
bbangle-domain         # 엔티티, Repository, QueryDSL, 외부 클라이언트
bbangle-admin          # 관리자 전용 (Controller + Service + DTO)
bbangle-seller         # 판매자 전용 (Controller + Service + DTO)
bbangle-customer       # 고객 전용   (Controller + Service + DTO)
bbangle-api            # Spring Boot 진입점 (현재는 3개를 하나로 합침)
```

### 의존성 방향

```
global-utils
     ↑
  domain
     ↑
admin / seller / customer
     ↑
    api
```

상위 모듈이 하위 모듈을 참조하는 것은 컴파일 오류 → 역할 간 침범을 코드로 차단.

---

## 모듈별 패키지 구조

### bbangle-global-utils
```
com.bbangle.bbangle.common.util
├── AesEncryptionUtil.java
├── CookieUtils.java
├── CsvUtil.java
└── ...
```

### bbangle-domain
```
com.bbangle.bbangle
├── common.domain/
│   ├── BaseEntity.java
│   └── DomainException.java
├── board/
│   ├── domain/Board.java
│   └── repository/BoardRepository.java
│   └── repository/BoardQueryDSLRepositoryImpl.java
├── order/
│   ├── domain/Order.java, OrderStatus.java
│   └── repository/OrderRepository.java
├── claim/
│   ├── domain/ExchangeRequest.java, ExchangeRequestStatus.java
│   └── repository/ClaimRepository.java
├── ... (member, seller, store, review 등 전체 도메인)
├── common/redis/RedisRepositoryImpl.java
├── image/S3ImageService.java
├── push/FirebasePushService.java
└── auth/oauth/KakaoApiClient.java, GoogleApiClient.java
```

### bbangle-admin
```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   └── exception/AdminException.java
│   └── exception/GlobalControllerAdvice.java
├── board/
│   ├── controller/AdminBoardController.java
│   ├── service/AdminBoardService.java
│   └── dto/AdminBoardResponse.java
├── seller/
│   ├── controller/AdminSellerController.java
│   └── service/AdminSellerService.java
└── settlement/
    ├── controller/SettlementController.java
    └── service/SettlementService.java
```

### bbangle-seller
```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   └── exception/SellerException.java
│   └── exception/GlobalControllerAdvice.java
├── seller/
│   ├── controller/SellerController.java
│   └── service/SellerService.java
├── order/
│   ├── controller/SellerOrderController.java
│   └── service/SellerOrderService.java
└── claim/
    ├── controller/SellerExchangeController.java
    └── service/ExchangeService.java
```

### bbangle-customer
```
com.bbangle.bbangle
├── common/
│   ├── dto/CommonResult.java
│   └── exception/CustomerException.java
│   └── exception/GlobalControllerAdvice.java
├── member/
│   ├── controller/MemberController.java
│   └── service/MemberService.java
├── board/
│   ├── controller/BoardController.java
│   └── service/BoardService.java
└── auth/
    ├── controller/CustomerTokenApiController.java
    └── service/CustomerTokenService.java
```

### bbangle-api
```
com.bbangle.bbangle
├── BbangleApplication.java
└── config/
    ├── SecurityConfig.java
    ├── SwaggerConfig.java
    └── ...
```
