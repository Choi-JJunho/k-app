# Supabase Internal Network Deployment Guide

이 문서는 Docker로 실행 중인 Supabase 스택(`supabase_default`)에 애플리케이션 컨테이너를 붙여서
DB를 내부 DNS로 접속하는 표준 방식을 설명합니다.

## 1. 목표 구조

- App Container -> `supabase_default` 네트워크
- DB 접속 대상 -> `supabase-pooler:5432`
- DB 계정 -> `postgres.<POOLER_TENANT_ID>` (예: `postgres.supabase`)
- DB 이름 -> 서비스별 DB (`kapp` 등)

호스트 IP(`192.168.x.x`) 경유보다 컨테이너 내부 DNS 사용이 안정적입니다.

## 2. 현재 k-app 적용 방식

`/Users/junho/private/project/k-app/deploy.sh`는 배포 시 아래를 자동 처리합니다.

- 컨테이너를 `supabase_default`에 연결
- DB 호스트를 `supabase-pooler`로 설정
- `kmeal-scraping` 환경변수 또는 Supabase `.env`에서 DB 계정/비밀번호를 자동 수집
- 배포 전 DB 연결 테스트(`select 1`) 수행

실행:

```bash
cd /Users/junho/private/project/k-app
./deploy.sh
```

필요 시 override:

```bash
DB_NAME_OVERRIDE=my_service_db \
DB_USERNAME_OVERRIDE=postgres.supabase \
HOST_PORT=9301 \
./deploy.sh
```

## 3. 다른 프로젝트에 적용하기 (Docker Run)

1. 컨테이너를 Supabase 네트워크에 붙입니다.
2. DB 환경변수를 내부 DNS 기준으로 지정합니다.

예시:

```bash
docker run -d \
  --name my-app \
  --network supabase_default \
  -e DB_HOST=supabase-pooler \
  -e DB_PORT=5432 \
  -e DB_NAME=kapp \
  -e DB_USER=postgres.supabase \
  -e DB_PASSWORD='<same as /opt/stacks/supabase/.env POSTGRES_PASSWORD>' \
  my-app:latest
```

## 4. 다른 프로젝트에 적용하기 (Docker Compose)

```yaml
services:
  my-app:
    image: my-app:latest
    networks:
      - supabase_default
    environment:
      DB_HOST: supabase-pooler
      DB_PORT: "5432"
      DB_NAME: kapp
      DB_USER: postgres.supabase
      DB_PASSWORD: ${SUPABASE_POSTGRES_PASSWORD}

networks:
  supabase_default:
    external: true
```

## 5. 점검 커맨드

네트워크 연결 확인:

```bash
docker inspect my-app --format '{{json .NetworkSettings.Networks}}'
```

DB 접속 확인:

```bash
docker run --rm --network supabase_default -e PGPASSWORD='<password>' postgres:15-alpine \
  psql -h supabase-pooler -p 5432 -U postgres.supabase -d kapp -Atc "select 1"
```

## 6. 다른 프로젝트 적용 체크리스트

1. `docker network ls`에서 `supabase_default` 존재 확인
2. 앱 컨테이너를 `supabase_default`에 연결
3. DB host를 `supabase-pooler`로 지정
4. DB user를 `postgres.<tenant>` 형식으로 지정 (기본 `postgres.supabase`)
5. DB 연결 테스트(`select 1`) 후 앱 배포

## 7. 권장사항

- 애플리케이션별 DB(`kapp`, `eggnova` 등)를 명시적으로 분리
- 비밀번호/시크릿은 `.env` 또는 시크릿 매니저로 주입
- 호스트 포트 공개는 최소화하고, 내부 통신은 네트워크 DNS로 고정
