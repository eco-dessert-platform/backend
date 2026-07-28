# Git 가이드라인

이 프로젝트의 실제 커밋/PR 히스토리를 분석하여 정리한 가이드라인이다.
Claude가 커밋 메시지 작성, PR 생성 등 git 작업을 도울 때 이 문서를 기준으로 삼는다.

---

## 브랜치 전략

```
main         ← 배포 브랜치 (직접 push 금지)
  └── dev    ← 개발 통합 브랜치 (PR 대상)
        └── feature/{설명}  ← 기능 개발 브랜치
```

### 브랜치 네이밍

```
feature/{도메인}-{기능-설명}
```

**실제 사례:**
```
feature/seller-charge-domain
feature/seller-return-invoice
feature/admin-get-upload-boards
feature/admin-upload-product-approve-reject
feature/seller-register-documents
feature/seller-statistics-daily-amount
feature/update-return-invoice
```

**규칙:**
- 소문자 + 하이픈(kebab-case) 사용
- 도메인을 앞에 명시: `seller-`, `admin-`
- 동사 없이 명사형으로 기술하는 경우가 많음

---

## 커밋 메시지 형식

### Subject (제목)

```
[Type] (스코프) 설명 (#PR번호)
```

- `#PR번호`는 PR merge 커밋에 자동 포함됨 → 개발 중 커밋에는 생략
- 스코프는 선택사항이지만 있으면 가독성이 좋아짐

### Type 목록

| Type | 용도 |
|------|------|
| `[Feat]` | 새 기능 추가 |
| `[Fix]` | 버그 수정 |
| `[Refactor]` | 기능 변경 없는 코드 개선 |
| `[Test]` | 테스트 코드 추가/수정 |
| `[Chore]` | Flyway 마이그레이션, 설정 등 부수 작업 |
| `[Document]` | 문서 작업 |

> **주의:** 히스토리에 `[Feature]`, `[Refactore]` 같은 변형이 존재하지만, `[Feat]`로 통일한다.

### 스코프 예시

```
(판매자)   (관리자)   (어드민)   (셀러)   (공통)
```

### 커밋 메시지 예시

```
[Feat] (판매자) 완료 주문 목록 조회 API 구현
[Refactor] (공통) Order 도메인 역할 분리
[Fix] SecurityFilterChain에 CORS 설정 추가하여 preflight 요청 403 해결
[Test] SellerOrderService 단위 테스트 실패 오류 수정 및 검증 로직 최신화
[Chore] Flyway V44 추가
```

### Body (본문) — 선택사항

작업 내용이 복잡하거나 여러 단계가 있을 때 사용:

```
* [feat] 핵심 기능 구현
  - 세부 변경 사항 설명
* [test] 통합 테스트 작성
* [refactor] QueryDSL로 쿼리 리팩토링
* [fix] CodeRabbit 리뷰 반영
```

또는 번호 목록 형태:
```
1. 판매자 완료 주문 목록 조회 API 구현 (구매확정·취소·반품·교환 페이징 + 상태별 카운트)
2. 슬라이스 테스트 및 통합 테스트 추가
3. 응답에 결제/배송 상세 필드 추가 및 상태 필터 단순화
```

---

## 주의사항

- `main` 브랜치에 직접 push하지 않는다
- Flyway 마이그레이션 파일은 한 번 추가하면 수정하지 않는다 (새 버전 파일 추가)

> push, PR 생성, PR 템플릿 작성 등 PR 관련 가이드는 `pr-create` 스킬(`.claude/skills/pr-create/SKILL.md`)을 참고한다.
