# 로컬 Parameter Store 실행 가이드

로컬에서도 DB 비밀번호와 JWT secret을 파일에 저장하지 않기 위해, 실행 시작 시 AWS Systems Manager Parameter Store에서 값을 한 번 읽고 Spring Boot 환경변수로 넘깁니다.

## 전제

- AWS CLI 로그인과 리전 설정이 끝나 있어야 합니다.
- Parameter Store에 아래 dev 값이 있어야 합니다.

```text
/schedly/dev/schedly-be/spring.datasource.url
/schedly/dev/schedly-be/spring.datasource.username
/schedly/dev/schedly-be/spring.datasource.password
/schedly/dev/schedly-be/app.jwt.secret
```

값 자체는 터미널, GitHub, 이슈, PR, 채팅에 출력하지 않습니다.

## 터미널 실행

BE 프로젝트에서 실행합니다.

```bash
cd /Users/gwon-yeonghyeon/schedly/schedly_be
./scripts/run-dev-with-ssm.sh
```

특정 AWS profile을 쓰는 경우:

```bash
AWS_PROFILE=schedly-dev AWS_REGION=ap-northeast-2 ./scripts/run-dev-with-ssm.sh
```

포트를 바꿔 실행하는 경우:

```bash
SERVER_PORT=18080 ./scripts/run-dev-with-ssm.sh
```

운영 환경을 나중에 따로 실행할 때는 Parameter 경로를 바꿉니다.

```bash
SCHEDLY_ENV=prod ./scripts/run-dev-with-ssm.sh
```

## IntelliJ GUI 실행

Spring Boot Run Configuration으로 바로 실행하면 Parameter Store를 읽지 못하고 H2 메모리 DB로 fallback 될 수 있습니다.

IntelliJ에서는 External Tool로 스크립트를 실행하는 방식을 사용합니다.

1. `Settings` 또는 `Preferences` 열기
2. `Tools` > `External Tools` 이동
3. `+` 클릭
4. Name: `Schedly BE dev`
5. Program: `/bin/zsh`
6. Arguments:

```text
-lc './scripts/run-dev-with-ssm.sh'
```

7. Working directory:

```text
$ProjectFileDir$
```

특정 AWS profile을 쓰면 Arguments를 이렇게 둡니다.

```text
-lc 'AWS_PROFILE=schedly-dev AWS_REGION=ap-northeast-2 ./scripts/run-dev-with-ssm.sh'
```

실행은 IntelliJ 상단 메뉴의 `Tools` > `External Tools` > `Schedly BE dev`에서 합니다.

## 확인

BE가 뜬 뒤 다른 터미널에서 확인합니다.

```bash
curl http://localhost:8080/api/health
```

정상 실행이면 `status`가 `ok`로 응답합니다.

이 방식으로 실행하면 회원가입과 일정 데이터는 Supabase PostgreSQL에 저장됩니다. 단순히 `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`만 실행하면 환경변수가 없을 때 H2 메모리 DB로 떠서 BE 재시작 시 데이터가 사라질 수 있습니다.
