<p style="color:red; font-weight: bold">※ 현재는 개발 단계이므로 코드는 dev에서 확인 가능 ※</p>

#  🍞 건강 베이커리 플랫폼 🍞
![image](https://github.com/eco-dessert-platform/backend/assets/125535111/0096fe1f-b54b-4b76-ae00-12ff8eb14fe3)
<center>건강 베이커리를 한눈에 보고 편리하게 구매할 수 있는</center>
<center><span style="font-weight: bold">건강 베이커리 플랫폼</span>입니다</center>

# 개요
- 프로젝트 이름 : Bbanggree's Oven
- 프로젝트 기간 : 2023.08 ~ (진행중)
- 개발 멤버

| 윤동현     | 윤동석     | 이중원 |정동욱| 윤예찬                 |
|-----|----|-----|-----|---------------------|
|-----|----|-----|-----| [dpcks9893@naver.com](dpcks9893@naver.com) |

# 전략
### 코드 컨벤션
- 구글 컨벤션을 설정하여 Check Style에 적용해서 사용하고 있습니다. 
- [설정 가이드](https://velog.io/@geun/Intellij-Formatter-Checkstyle-%EC%84%B8%ED%8C%85%ED%95%98%EA%B8%B0)
- 예외 사항 
    - Indent는 4로 설정해야 합니다.

### 브랜치 전략
- Git-flow 전략을 기반으로 main, develop 브랜치와 feature 보조 브랜치를 운용했습니다.
- main, develop, Feat 브랜치로 나누어 개발을 하였습니다.
    - **main** 브랜치는 배포 단계에서만 사용하는 브랜치입니다.
    - **develop** 브랜치는 개발 단계에서 git-flow의 master 역할을 하는 브랜치입니다.
    - **Feat** 브랜치는 기능 단위로 독립적인 개발 환경을 위하여 사용하고 merge 후 각 브랜치를 삭제해주었습니다.

# 역할
| 도메인        | 기능        | 담당  |
|------------|-----------|-----|
| 스토어        | 상세정보 조회   |  윤예찬   |
| 게시물        | 조회        | 이중원 |
| 게시물        | 정렬        | 이중원 |
| 게시물        | 상세정보 조회   | 윤예찬 |
| 위시리스트(스토어) |           | 윤동석  |
| 위시리스트(폴더)  |           | 이중원 |
| 위시리스트(게시물) |           | 이중원 |
| 회원         |           |     |
| 검색         | 키워드 검색    | 윤예찬 |
| 검색         | 인기 검색어 조회 | 윤예찬 |
| 검색         | 최신 검색어 조회 | 윤예찬 |
| 검색         | 자동완성      | 윤예찬 |
| 검색         | 결과조회      | 윤예찬 |
| 랭킹         |           | 이중원 |
| 토큰         |           | 윤동석 |
| 추천         |           | 이중원 |
| 알림         |           | 윤동석 |
| 리뷰         |           | 윤동석 |
| 관리자        |           |     |
| 환경설정       | Cors 설정   |  윤예찬   |
| AOP        |           |     |
| 테스트        |           |     |

# 아키텍쳐
![image](https://github.com/eco-dessert-platform/backend/assets/125535111/c9a1b3b8-2574-4e1a-b6f6-143170174e2b)

# ERD
![image](https://github.com/eco-dessert-platform/backend/assets/125535111/b456bb7f-1bb5-4da6-b95c-48011fb0f3a8)


