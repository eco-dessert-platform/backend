# POST `/api/v1/admin/sellers/documents/download` — 판매자 서류 ZIP 다운로드

- 선택한 판매자(들)의 서류를 스토어명 디렉토리 구조로 묶은 ZIP 파일로 다운로드합니다.
- 조회된 서류가 하나도 없으면 예외(`SELLER_DOCUMENT_NOT_FOUND`)를 반환합니다.
- 응답은 JSON이 아닌 ZIP 바이너리 스트림(`application/zip`)입니다.

| # | Method | Endpoint                                    | 설명              |
|---|--------|----------------------------------------------|-----------------|
| 1 | POST   | `/api/v1/admin/sellers/documents/download`    | 판매자 서류 ZIP 다운로드 |

# **Request 헤더**

| key           | 설명       | value 타입 | 옵션               | Nullable | 예시                             |
|---------------|----------|----------|------------------|----------|--------------------------------|
| Authorization | 인증 토큰    | String   | Bearer Token     | N        | Bearer eyJhbGciOiJIUzI1NiJ9... |
| Content-Type  | 요청 본문 타입 | String   | application/json | N        | application/json               |

# Path Variable

- 해당 없음

# **Query parameter**

- 해당 없음

# Request Body

```json
{
  "sellerIds": [1, 2, 3]
}
```

| key       | 설명            | value 타입    | 옵션        | Nullable | 예시        |
|-----------|---------------|-------------|-----------|----------|-----------|
| sellerIds | 서류를 다운로드할 판매자 ID 목록 | List<Long> | 최소 1개, 최대 50개 | N        | [1, 2, 3] |

# Response

- 성공 시 JSON이 아닌 **ZIP 파일 바이너리**가 스트리밍으로 반환됩니다.

| 응답 헤더             | 값                                     |
|-------------------|---------------------------------------|
| Content-Type      | application/zip                       |
| Content-Disposition | attachment; filename="documents.zip" |

## ZIP 내부 구조

```
documents.zip
├── 빵그리베이커리/
│   ├── 사업자등록증.pdf
│   ├── 통신판매업신고증.jpg
│   ├── 즉석식품제조가공업등록증.png
│   └── 통장사본.pdf
└── 달콤한과자점/
    ├── 사업자등록증.pdf
    └── 통장사본.jpg
```

- 판매자별로 스토어명 디렉토리 하위에 서류가 묶입니다.
- 파일명은 원본 파일명이 아닌 `DocumentType`의 한글명으로 변환됩니다 (예: `사업자등록증.pdf`).
- 서류가 없는 판매자는 ZIP에 포함되지 않습니다.
- 같은 판매자가 동일한 서류 종류를 중복 등록한 경우, 파일명 뒤에 `(2)`, `(3)`... 접미사가 붙어 구분됩니다 (예: `사업자등록증(2).pdf`).

## 실패 시 Response Body

```json
{
  "success": false,
  "code": -719,
  "message": "판매자의 서류가 존재하지 않습니다."
}
```

| **key** | **설명**     | **value 타입** | **옵션**                                     | **Nullable** | **예시**                |
|---------|------------|--------------|--------------------------------------------|--------------|------------------------|
| success | 요청 성공 여부   | Boolean      |                                              | N            | false                  |
| code    | 응답 코드      | Integer      | -719: 서류 없음, -605: 스트림 처리 실패                | N            | -719                   |
| message | 응답 메시지     | String       |                                              | N            | 판매자의 서류가 존재하지 않습니다. |

### 주요 에러 코드

| code | HTTP Status | 설명                                    |
|------|-------------|---------------------------------------|
| -719 | 404         | 요청한 판매자(들) 서류가 하나도 존재하지 않음            |
| -605 | 500         | ZIP 생성/스트리밍 중 IO 오류 (문서 URL 접근 실패 등)  |
| -    | 400         | `sellerIds`가 비어있거나 50개를 초과 (Validation) |
