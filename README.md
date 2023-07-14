![](https://velog.velcdn.com/images/jiseung/post/eb6f06c5-13bc-4346-af1d-4f9962b6139f/image.png)

## 🖼 Overview

> ❓ 같은 취미를 가지고 있는 사람들끼리 만나면 얘기할 수 있는게 많아지고, 더 깊은 이야기를 나눌 수 있지 않을까?

>💡 특정 테마를 주제로 **SNS**를 만들어보자! → **그림 SNS**

<img width="100px" src="https://s3.us-west-2.amazonaws.com/secure.notion-static.com/d3156e99-4f78-4dd4-8b7b-f368f4c3050a/qwerqwer.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20230309%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20230309T022027Z&X-Amz-Expires=86400&X-Amz-Signature=758495cff74222b9155d45d648acaea14bef6302c1cc9f18a31947e2ef6f5f0e&X-Amz-SignedHeaders=host&response-content-disposition=filename%3D%22qwerqwer.png%22&x-id=GetObject" />

### [ArtBubble]

예술을 의미하는 단어  **Art**와 SNS 알고리즘에 의해 묶인 사람들을 뜻하는 은어  **Bubble**의 합성어

### [로고]

<img width="100px" src="https://velog.velcdn.com/images/jiseung/post/567372c3-6dcf-4f9c-8413-373b97ae719b/image.png" />

**공유**의 의미를 가지는 아이콘 형태에
새로움을 상징하는  **초록,**  자유로움을 상징하는  **파랑,**  창의성을 상징하는  **보라**를 통해 우리 서비스의 성격을 나타냅니다.
또한 보라색에서 새롭게 나오고 있는 방울은 또 다른 가능성을 의미합니다.

## 📖 Introduction

### 🛠️ Backend Tech Stack

![](https://github.com/DevTraces/BackEnd/assets/81020108/52d76c7f-50dc-4a97-9f3b-c85413d62372)

- Framework: `SpringBoot`, `SpringSecurity`, `SpringDataJPA`
- Language: `JAVA 8`
- Database: `AWS RDS(MySQL)`, `AWS Elasticache(Redis)`, `AWS S3`
- Login: `JWT`, `OAuth 2.0`
- HTTPS: `AWS Load Balancer`, `AWS Certificate Manager`
- Stress Test: `nGrinder`
- Code Test: `Junit5 & Mockito`
- Deploy: `AWS EC2`
- CI/CD: `Github Actions`, `AWS CodeDeploy`
- API doc: `Swagger`
- Communication Tools : `Notion`, `Discord`, `Slack`


### 💻 주요 기능


🔑  **계정**
<table>
  <tr>
    <td max-width="500px">
      <img src="https://velog.velcdn.com/images/jiseung/post/3208d91f-0521-406b-9ba4-8555c674cf58/image.png" />
    </td>
    <td>
      <b>회원가입</b> - 이메일 인증/카카오를 통해 회원가입을 할 수 있습니다.<br>
      <b>로그인</b>- 이메일 및 소셜 로그인(카카오)을 할 수 있습니다.<br>
      <b>로그아웃</b> - 로그아웃을 할 수 있습니다.<br>
      <b>비밀번호 재설정</b> - 비밀번호를 변경하거나 잊어버린 경우 재설정할 수 있습니다.<br>
      <b>회원탈퇴</b> - 탈퇴가 가능하고, 탈퇴한 유저는 재가입이 가능합니다.<br>
      <b>프로필</b> - 유저의 정보(프로필 이미지, 이름, 닉네임, 자기소개)를 등록/수정할 수 있습니다.
    </td>
  </tr>
</table>

👤  **프로필**

<table>
  <tr>
    <td max-width="500px" >
      <img src="https://velog.velcdn.com/images/jiseung/post/1f7bfea2-5403-410e-9964-da4752bffc70/image.png" />
    </td>
    <td>
      <b>유저 정보</b> - 프로필 이미지, 이름, 닉네임, 자기소개, 게시글 개수, 팔로잉/팔로워 수를 볼 수 있습니다.<br>
      <b>게시글</b> - 유저가 작성한 게시글 목록을 볼 수 있습니다.<br>
      <b>팔로잉/팔로워 리스트</b> - 유저가 팔로우하거나 나를 팔로우하는 사람의 목록을 볼 수 있습니다.<br>
      <b>저장된 목록</b> - 내 프로필인 경우 내가 북마크한 게시글 목록을 볼 수 있습니다.
    </td>
  </tr>
</table>

📌  **게시글**

<table>
  <tr>
    <td max-width="500px" >
      <img src="https://velog.velcdn.com/images/jiseung/post/ad493aa1-720d-494b-b427-a025ec5e1517/image.png" />
    </td>
    <td>
      <b>게시글</b> - 게시글을 작성하거나 수정/삭제할 수 있습니다.<br>
      <b>댓글</b> - 게시글에 댓글/대댓글을 작성하거나 수정/삭제할 수 있습니다.<br>
      <b>좋아요</b> - 게시글에 좋아요를 하거나 취소할 수 있습니다.<br>
      <b>좋아요 개수</b> - 특정 게시물에 좋아요한 사람들을 볼 수 있습니다.<br>
      <b>북마크</b> - 특정 게시물을 북마크에 등록하거나 삭제할 수 있습니다.<br>
      <b>공유하기</b> - 특정 게시물을 URL을 통해 공유할 수 있습니다.<br>
    </td>
  </tr>
</table>

🤝 **팔로우**

<table>
  <tr>
    <td max-width="500px" >
      <img src="https://velog.velcdn.com/images/jiseung/post/d9773f7a-4015-4fbb-a55b-e45b5c2bad40/image.png" />
    </td>
    <td>유저를 팔로우하거나 팔로우 취소할 수 있습니다.</td>
  </tr>
</table>

⏰  **알림**

<table>
  <tr>
    <td max-width="500px" >
      <img src="https://velog.velcdn.com/images/jiseung/post/9d48bc9a-1887-4d80-bfa5-26a49a7dc6bd/image.png" />
    </td>
    <td>좋아요/팔로우 요청/댓글/대댓글에 대한 알림을 받을 수 있습니다.</td>
  </tr>
</table>

🔍  **검색**

<table>
  <tr>
    <td max-width="500px">
      <img src="https://velog.velcdn.com/images/jiseung/post/8a1cfb6f-9b26-44d0-af92-4fe97744b176/image.png" />
    </td>
    <td>
      <b>유저 검색</b> - 검색한 이름이나 닉네임과 일치하는 유저를 조회할 수 있습니다.<br>
      <b>해시태그 검색</b> - 검색한 해시태그가 있는 게시물들을 조회할 수 있습니다.
    </td>
  </tr>
</table>

👍  **추천**

<table>
  <tr>
    <td max-width="500px">
      <img src="https://velog.velcdn.com/images/jiseung/post/7e007551-b006-4752-8885-76895bb157b3/image.png" />
    </td>
    <td>
      <b>팔로우 추천</b> - 최근 1시간 이내에 많은 팔로우를 받은 유저들을 보여줍니다.<br>
      <b>피드 추천</b> - 팔로우한 사람이 있다면, 팔로우한 사람의 최신 글을 보여줍니다.
    없다면, 최근 1시간 이내에 많은 좋아요를 받은 글을 보여줍니다.
    </td>
  </tr>
</table>

    
### 🔫 Trouble Shooting
### 🍪 쿠키
| 문제 상황 | JWT토큰을 클라이언트에 쿠키로 설정(Set-Cookie)할 수 없음 |
| --- | --- |
| 원인 | HTTP 전송을 하면 쿠키의 Secure 옵션을 false, SameSite 옵션을 none 이 아닌 strict나 Lax로 설정해야 하는데, 이 경우 다른 도메인간 쿠키를 전송해야 하는 상황에서 에러 발생 |
| 해결 | 쿠키의 Secure 옵션을 true, SameSite 옵션을 none 으로 설정하고, HTTPS 전송 방식을 도입함으로써 쿠키를 정상 전송 |

### 📈 N + 1 문제 대응 & nGrinder로 부하 테스트
| 문제 상황 | 게시글을 읽어서 1 : N 매핑된 다른 엔티티를 참조할 때 N + 1 문제 발생 |
| --- | --- |
| 원인 | FE 리턴용 DTO를 만들 때 엔티티 내부의 다른 엔티티를 참조할 때마가 추가 쿼리가 발생 |
| 해결 | DTO에 필요한 정보를 JPQL JOIN 쿼리로 한 번에 가져와서 추가적인 엔티티 참조를 차단 |
- **성능 테스트 결과**
    
     **nGrinder로 리스트 획득 API를 테스트 했을 때 6 ~ 18 TPS의 성능 향상을 확인함.**
    
    (TPS: Transaction Per Second, 초당 트랜잭션 처리량)
    
    게시물 리스트 읽기 API  52.7 TPS   -> **59.0 TPS (+6.3)**
    
    게시물 1개 읽기 API       49.5 TPS   -> **67.1 TPS (+17.6)**
    
    피드 구성 API                43.3 TPS   -> **56.7 TPS (+13.4)**
    
    댓글 리스트 읽기 API     60.8 TPS   -> **70 TPS (+9.2)**
    
    대댓글 리스트 읽기 API  51.6 TPS   -> **64.7 TPS (+13.1)**
    
### ⚡ 리소스 부족 대응

| 문제 상황 | EC2 서버(t2.micro)에 쓰레드 부족 에러가 발생하면서 서버가 다운됨 |
| --- | --- |
| 원인 | 스케줄링용 스레드는 2개를 만들었는데, 스프링부트 앱에서 4개의 스케쥴러가 동작 |
| 해결 | 소스코드 내 스레드를 다수 사용하는 메소드를 조절. 스레드 개수를 4개에서 2개로 줄임으로써 개수를 맞춰줌. |

### ⚠️ 카카오 소셜 로그인 
| 문제 상황 | 카카오 로그인한 유저를 이메일로 식별 불가능 |
| --- | --- |
| 원인 | 카카오 서버에서 사용자의 이메일을 선택적으로 받을 수 있는 상황 |
| 해결 | Unique Key에 null이 허용됨을 확인한 후 카카오 로그인 유저의 email은 null로 처리한 다음 사용자는kakaoUserId로 식별 |

| 문제 상황 | 카카오 로그인 유저에 대해 401 UnAuthorized  발생 |
| --- | --- |
| 원인 | 카카오 로그인 유저에 비밀번호를 설정하지않아 생성된 JWT 토큰이 유효하지 않았음 |
| 해결 | 임의의 비밀번호를 사용자에게 설정해서 유효한 JWT 토큰 생성 |


## 👋 Team

### 📜 API doc & Conventions
[API doc](https://dev-traces.notion.site/API-666199dae3824b40bf38279a3aefe35f?pvs=4)<br>
[Code](https://www.notion.so/Code-2300a47af82f41d280d5a0b4957962ef)<br>
[Commit Convention](https://www.notion.so/Commit-Convention-48048f6c01ec4fecb08ad09fb4c8274f)<br>
[Issue](https://www.notion.so/Issue-dff43daa80d14af89880593a3eb39851)<br>
[PR](https://www.notion.so/PR-9bfa3b0d54c3410ebdbaa9ef0df292fb)<br>
[Git Branch](https://www.notion.so/Git-Branch-6b8d978afc424d678949954404896de3)

### Members
<table>
  <tr>
  	<td align="center">
      <a href="https://github.com/jiseung-kang">
        <img
          src="https://avatars.githubusercontent.com/jiseung-kang"
          width="100px;"
        /><br />강지승(FE)</a><br />
    </td>
	<td align="center">
      <a href="https://github.com/gwakjaeha"><img
          src="https://avatars.githubusercontent.com/gwakjaeha"
          width="100px;" 
        /><br />곽재하(BE)</a><br />
    </td>
    <td align="center">
      <a href="https://github.com/codeisneverodd">
        <img
          src="https://avatars.githubusercontent.com/codeisneverodd"
          width="100px;"
        /><br />김경현(FE)</a><br />
    </td>
    <td align="center">
      <a href="https://github.com/heyazoo1007">
        <img
          src="https://avatars.githubusercontent.com/heyazoo1007"
          width="100px;"
        /><br />김예진(BE)</a><br />
    </td>
    <td align="center">
      <a href="https://github.com/DongvinPark">
        <img
          src="https://avatars.githubusercontent.com/DongvinPark"
          width="100px;"
        /><br />박동빈(BE)</a><br />
    </td>
  </tr>
</table>
