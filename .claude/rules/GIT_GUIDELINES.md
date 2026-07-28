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

## Push & PR 전체 흐름

PR을 올리기 전 push까지의 전체 순서:

```bash
# 1. 현재 브랜치 확인
git status
git branch

# 2. 변경사항 스테이징
git add {파일명}           # 특정 파일만
# git add -p              # 변경사항을 chunk 단위로 선택하며 추가 (권장)

# 3. 커밋
git commit -m "[Feat] (판매자) 스토어명 변경 신청 API 구현"

# 4. 원격에 push
git push origin {브랜치명}
# 최초 push인 경우 upstream 설정 포함
git push -u origin {브랜치명}

# 5. PR 생성
gh pr create --base dev --title "..." --body "..."
```

### 자주 쓰는 push 관련 명령어

```bash
# 현재 브랜치를 원격에 push (upstream 설정 포함)
git push -u origin feature/seller-charge-domain

# upstream 이미 설정된 경우 (이후 push부터)
git push

# push 전 원격과 차이 확인
git log origin/{브랜치명}..HEAD --oneline

# 원격 브랜치 목록 확인
git branch -r
```

### push 전 체크

```bash
# 빌드 확인 (테스트 제외)
./gradlew build -x test

# checkstyle 통과 여부
./gradlew checkstyleMain

# 원격 dev 기준으로 충돌 없는지 확인
git fetch origin
git log HEAD..origin/dev --oneline
```

---

## PR 작성 (gh 사용)

### PR 템플릿 구조

이 프로젝트는 GitHub PR 템플릿을 사용한다. PR body는 아래 구조를 반드시 따른다:

```markdown
## History

<!--연관된 내용, 이슈 링크를 달아주세요-->
<!--이슈 태스크를 모두 완료하고 닫는다면 * Resolves #번호-->
<!--이슈 태스크를 모두 완료하지는 못 했지만 닫는다면 * Closes #번호-->
<!--열어둔다면 * #번호-->

## 🚀 Major Changes & Explanations

<!--빠른 리뷰를 위해 이해를 도울 만한 설명을 자세히 적어주세요-->

## 📷 Test Image

<!-- postman, swagger 등을 활용한 api 결과, 각종 Edge case 테스트 결과 이미지를 붙여주세요-->
<!-- 이미지가 많거나 클 경우 오른쪽 패턴을 이용해주세요<img src = "CREATED_IMG_URL" width = "400px">-->

## 💡 ETC

<!-- ex) 질문. 작업 관련 사항, 고민한 내용 등등을 적어주세요-->
```

**섹션별 작성 가이드:**

- **History**: 연관 이슈 번호를 링크. 이슈를 닫으면 `Resolves #번호`, 닫지 않으면 `#번호`
- **Major Changes & Explanations**: 실제 변경 내용을 `* [type] 설명` 형태로 상세히 기술 (리뷰어가 빠르게 이해할 수 있도록)
- **Test Image**: Postman/Swagger 결과 스크린샷, Edge case 테스트 결과 이미지 첨부
- **ETC**: 고민한 내용, 질문, 특이사항 등

### gh로 PR 생성하는 명령어

```bash
gh pr create \
  --base dev \
  --title "[Feat] (판매자) 스토어명 변경 신청 API" \
  --body "$(cat <<'EOF'
## History

* Resolves #123

## 🚀 Major Changes & Explanations

* [feat] (판매자) 스토어명 변경 신청 API 구현
  - StoreNameRequest 엔티티 및 Repository 추가
  - 변경 신청 상태(PENDING/APPROVE/REJECT) 관리

* [chore] Flyway V43 추가
  - store_name_request 테이블 DDL

* [test] 서비스 단위 테스트 작성

* [test] Facade 통합 테스트 작성

* [fix] CodeRabbit 리뷰 반영

## 📷 Test Image

<!-- 스크린샷 첨부 -->

## 💡 ETC

<!-- 특이사항 없음 -->
EOF
)"
```

### PR 제목 규칙

커밋 subject와 동일한 형식을 따른다:

```
[Feat] (판매자) 스토어명 변경 신청 API
[Refactor] (판매자) 등록한 스토어 조회 API 리팩토링
[Fix] SecurityFilterChain CORS 설정 추가
```

### PR 대상 브랜치

항상 `dev` 브랜치로 PR을 생성한다.

### 유용한 gh 명령어

```bash
# PR 생성
gh pr create --base dev --title "..." --body "..."

# PR 목록 조회
gh pr list

# PR 상세 조회
gh pr view {PR번호}

# PR 상태 / CI 확인
gh pr checks {PR번호}

# PR 머지
gh pr merge {PR번호} --squash
```

---

## 자주 보이는 작업 패턴

새 API 기능을 개발할 때 PR body에 보통 이 순서로 커밋들이 담긴다:

```
* [feat]    핵심 구현 (Controller, Service, Facade, Repository)
* [chore]   Flyway 마이그레이션 추가 (스키마 변경 있을 때)
* [test]    단위 테스트 (UnitTest, ServiceUnitTest)
* [test]    통합 테스트 (IntegrationTest, FacadeIntegrationTest)
* [test]    컨트롤러 테스트
* [fix]     CodeRabbit 리뷰 반영
* [refactor] 추가 개선 사항
```

---

## 주의사항

- `main` 브랜치에 직접 push하지 않는다
- PR 없이 `dev`에 직접 merge하지 않는다
- Flyway 마이그레이션 파일은 한 번 추가하면 수정하지 않는다 (새 버전 파일 추가)
- CodeRabbit 리뷰가 달리면 반영 후 `[fix] CodeRabbit 내용 반영` 커밋을 남긴다
