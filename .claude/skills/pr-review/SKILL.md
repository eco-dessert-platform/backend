---
name: pr-review
description: gh CLI로 GitHub PR을 리뷰할 때 사용한다. pending review로 코멘트를 모아서 제출하고, 게시 전 사용자 승인을 받으며, 상황에 맞는 이벤트 타입(APPROVE/REQUEST_CHANGES/COMMENT)을 고른다.
---

# GitHub PR 리뷰

## 핵심 원칙

- **Pending review로 배치 처리**: 코멘트가 하나뿐이어도 항상 pending review를 먼저 만든다. PR 작성자에게 알림이 한 번에 가고, 제출 전에 코멘트를 검토/추가할 수 있다.
- **게시 전 사용자 승인 필수**: 리뷰 코멘트는 공개적이고 되돌리기 어렵다. AskUserQuestion으로 게시할 내용(파일/라인/코멘트 전문/이벤트 타입)을 그대로 보여주고 yes/no 승인을 받은 뒤에만 게시한다.
- **gh CLI 설치 확인**: 시작 전 `gh --version`으로 확인한다. 없으면 설치 안내(`brew install gh` 등) 후 중단한다.

## 워크플로우

1. `gh --version`으로 gh CLI 확인
2. PR 분석 후 리뷰 내용 초안 작성
3. AskUserQuestion으로 게시될 내용 그대로 보여주고 승인 요청
4. 승인 후에만 게시

```bash
# 1. commit SHA 조회
gh pr view <PR번호> --json commits --jq '.commits[-1].oid'

# 2. PENDING 리뷰 생성 (event 필드 생략)
gh api repos/:owner/:repo/pulls/<PR번호>/reviews \
  -X POST \
  -f commit_id="<COMMIT_SHA>" \
  -f 'comments[][path]=path/to/file.ts' \
  -F 'comments[][line]=<라인번호>' \
  -f 'comments[][side]=RIGHT' \
  -f 'comments[][body]=코멘트 내용

```suggestion
// 제안 코드
```' \
  --jq '{id, state}'

# 3. 리뷰 제출
gh api repos/:owner/:repo/pulls/<PR번호>/reviews/<REVIEW_ID>/events \
  -X POST \
  -f event="COMMENT" \
  -f body="전체 리뷰 메시지"
```

## 이벤트 타입

| 타입 | 사용 시점 |
|------|-----------|
| `APPROVE` | 머지해도 되는 상태, 사소하거나 선택적인 제안만 있을 때 |
| `REQUEST_CHANGES` | 보안 취약점, 버그, 실패하는 테스트 등 반드시 고쳐야 할 이슈 |
| `COMMENT` | 질문, 중립적인 피드백 |

## 문법 규칙

- `comments[][path]`처럼 `[]`가 들어간 파라미터는 항상 작은따옴표(`'`)로 감싼다
- 문자열은 `-f`, 숫자(라인 번호)는 `-F` 사용
- 코드 제안은 ` ```suggestion ` 블록으로 감싼다 (해당 라인/범위를 통째로 대체하므로 완전하고 정확한 코드여야 함)
- 대상 파일 내용에 이미 백틱 3개(```)가 포함된 경우(마크다운 등), 백틱 4개나 물결(`~~~`)로 감싸 충돌을 피한다

## 자주 하는 실수

| 실수 | 해결 |
|------|------|
| 코멘트가 하나뿐이라 pending 없이 바로 게시 | 하나여도 pending review 사용 (일관된 워크플로우) |
| `comments[][]` 파라미터에 따옴표 안 감쌈 | 항상 작은따옴표로 감싸기 |
| commit SHA 조회 안 하고 진행 | `gh pr view`로 먼저 조회 |
| 이벤트 타입 잘못 선택 | 보안/버그 → REQUEST_CHANGES, 스타일 → APPROVE, 질문 → COMMENT |
| "사용자가 이미 리뷰하라고 했으니 승인 단계 생략" | 리뷰 아이디어에 대한 승인과 게시 내용에 대한 승인은 다르다 — 항상 게시 전 확인 |

---

참고: https://github.com/aidankinzett/claude-git-pr-skill
