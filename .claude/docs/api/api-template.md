# **Request 헤더**

| key           | 설명       | value 타입 | 옵션               | Nullable | 예시                             |
|---------------|----------|----------|------------------|----------|--------------------------------|
| Authorization | 인증 토큰    | String   | Bearer Token     | N        | Bearer eyJhbGciOiJIUzI1NiJ9... |
| Content-Type  | 요청 본문 타입 | String   | application/json | N        | application/json               |

# Path Variable

(예: 게시판 ID 기준 조회)

| key        | 설명      | value 타입 | 옵션 | Nullable | 예시 |
|------------|---------|----------|----|----------|----|
| boardId    | 게시판 ID  | Long     |    | N        | 10 |
| categoryId | 카테고리 ID | Long     |    | Y        | 3  |

# **Query parameter**

| key  | 설명     | value 타입 | 옵션             | Nullable | 예시             |
|------|--------|----------|----------------|----------|----------------|
| page | 페이지 번호 | Integer  | 0부터 시작         | N        | 0              |
| size | 페이지 크기 | Integer  | 최대 100         | N        | 20             |
| sort | 정렬 기준  | String   | createdAt,desc | Y        | createdAt,desc |

# Request Body

```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "writerId": 1001
}
```

| key       | 설명       | value 타입  | 옵션         | Nullable | 예시         |
|-----------|----------|-----------|------------|----------|------------|
| startDate | 조회 시작 일자 | LocalDate | yyyy-MM-dd | Y        | 2025-01-01 |
| startDate | 조회 시작 일자 | LocalDate | yyyy-MM-dd | Y        | 2025-01-01 |
| startDate | 조회 시작 일자 | LocalDate | yyyy-MM-dd | Y        | 2025-01-01 |

# Response Body

```json
{
  "success": true,
  "code": 0,
  "message": "SUCCESS",
  "result": {
    "contents": [
      {
        "id": 1,
        "title": "Spring Boot API 설계",
        "writerName": "kmindev",
        "viewCount": 120,
        "createdAt": "2025-12-10T14:32:10"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 152
  }
}
```

| key                        | 설명        | value 타입      | 옵션       | Nullable | 예시                  |
|----------------------------|-----------|---------------|----------|----------|---------------------|
| success                    | 요청 성공 여부  | Boolean       |          | N        | true                |
| code                       | 응답 코드     | Integer       | 0: 성공    | N        | 0                   |
| message                    | 응답 메시지    | String        |          | N        | SUCCESS             |
| result                     | 실제 응답 데이터 | Object        |          | Y        |                     |
| result.contents            | 결과 목록     | Array         |          | Y        |                     |
| result.contents.id         | 게시글 ID    | Long          |          | N        | 1                   |
| result.contents.title      | 게시글 제목    | String        |          | N        | Spring Boot API 설계  |
| result.contents.writerName | 작성자 이름    | String        |          | N        | kmindev             |
| result.contents.viewCount  | 조회수       | Integer       |          | N        | 120                 |
| result.contents.createdAt  | 생성 일시     | LocalDateTime | ISO-8601 | N        | 2025-12-10T14:32:10 |
| result.page                | 현재 페이지    | Integer       |          | N        | 0                   |
| result.size                | 페이지 크기    | Integer       |          | N        | 20                  |
| result.totalElements       | 전체 건수     | Long          |          | N        | 152                 |