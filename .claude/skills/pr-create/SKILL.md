---
name: pr-create
description: PR 생성 및 push 워크플로우 가이드. gh pr create 사용법과 PR 템플릿 작성 규칙을 다룬다. push하거나 PR을 생성할 때 사용한다.
---

# PR 생성 가이드

## PR 생성 흐름

1. 커밋 안 되어 있으면 사용자에게 확인 후 커밋
2. push 안 되어 있으면 push
3. PR 생성 (대상 브랜치는 항상 `dev`)
   ```bash
   gh pr create --base dev --title "..." --body "..."
   ```

---

## PR 템플릿 구조

PR body는 아래 구조를 반드시 따른다:

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

- **History**: 연관 이슈 번호를 링크. 이슈를 닫으면 `Resolves #번호`, 닫지 않으면 `#번호`
- **Major Changes & Explanations**: 실제 변경 내용을 `* [type] 설명` 형태로 상세히 기술
- **Test Image**: Postman/Swagger 결과 스크린샷, Edge case 테스트 결과 이미지 첨부
- **ETC**: 고민한 내용, 질문, 특이사항 등

### PR 제목 규칙

커밋 subject와 동일한 형식을 따른다:

```
[Feat] (판매자) 스토어명 변경 신청 API
[Refactor] (판매자) 등록한 스토어 조회 API 리팩토링
[Fix] SecurityFilterChain CORS 설정 추가
```

---

## 주의사항

- PR 없이 `dev`에 직접 merge하지 않는다
- CodeRabbit 리뷰가 달리면 반영 후 `[fix] CodeRabbit 내용 반영` 커밋을 남긴다
