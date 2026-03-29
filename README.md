# Task API

Spring Bootで作成したシンプルなタスク管理APIです。W04では `Spring Security + JWT` をベースに、実務寄りの認証基盤として以下を実装しています。

- stateless な access token 認証
- DB管理のユーザー
- ADMIN向けユーザー管理API
- role ベースの認可
- refresh token ローテーション
- logout による token revoke
- DB永続化された認証API rate limit
- 監査ログ

## 技術スタック

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Docker
- Gradle

## 機能

- ログインAPIによる access token / refresh token 発行
- refresh token による再発行
- logout による access token / refresh token 無効化
- 管理者によるユーザー一覧取得
- 管理者によるユーザー作成
- 管理者によるユーザー無効化 / ロール変更 / パスワード更新
- タスク作成
- タスク一覧取得
- タスク詳細取得
- タスク更新
- タスク削除

## セットアップ

このリポジトリでは、DB接続情報と認証設定を環境変数で管理します。JWT secret や初期ユーザーのパスワードはアプリ内に固定値を持たせていません。

1. `.env.example` をコピーして `.env` を作成します。
2. 少なくとも `POSTGRES_PASSWORD`、`APP_SECURITY_PASSWORD`、`JWT_SECRET` を自分の値に変更します。

```bash
cp .env.example .env
```

`.env` は Docker Compose とローカル `bootRun` の両方で使えます。ローカル実行時は `DB_HOST=localhost` を使い、Docker Compose では API コンテナ側だけ `DB_HOST=db` に上書きします。

主な設定値:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`
- `DB_HOST`
- `DB_PORT`
- `API_PORT`
- `APP_SECURITY_USERNAME`
- `APP_SECURITY_PASSWORD`
- `APP_SECURITY_ROLE`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `AUTH_RATE_LIMIT_MAX_ATTEMPTS`
- `AUTH_RATE_LIMIT_WINDOW`
- `AUTH_RATE_LIMIT_BLOCK_DURATION`

## 起動方法

### Docker Composeで起動

```bash
docker compose up --build
```

APIは `http://localhost:${API_PORT:-8080}` で利用できます。

初回ビルドや設定変更後の確認:

```bash
docker compose config
docker compose up --build
```

停止や後片付け:

```bash
docker compose down
```

DBの初期化変数を変更してクリーンにやり直したい場合:

```bash
docker compose down -v
docker compose up --build
```

- Swagger UI: `http://localhost:${API_PORT:-8080}/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:${API_PORT:-8080}/v3/api-docs`
- Health Check: `http://localhost:${API_PORT:-8080}/actuator/health`
- 学習用ページ: `http://localhost:${API_PORT:-8080}/study-guide`
- W03学習用ページ: `http://localhost:${API_PORT:-8080}/w03-exception-test-guide`
- W04学習用ページ: `http://localhost:${API_PORT:-8080}/w04-jwt-auth-guide`
- 環境・構築ガイド: `http://localhost:${API_PORT:-8080}/project-setup-guide`

学習用ページや静的HTMLを変更したのに画面へ反映されない場合:

- `docker compose` で起動しているなら、`Dockerfile` でソースをイメージへ `COPY` しているため、`docker compose up --build` で再ビルドが必要です
- `bootRun` で起動しているなら、アプリを再起動してください
- ブラウザは `Ctrl + F5` などでハードリロードすると確認しやすいです

### Spring Bootだけ起動する場合

PostgreSQLを先に起動し、`.env` を読み込んでから起動します。`DB_HOST` の初期値は `localhost` なので、そのままローカルDBへ接続できます。

```bash
set -a
source .env
set +a
./gradlew bootRun
```

すでに `DB_URL` を使うローカル手順がある場合も、そのまま上書きして利用できます。

初回起動時には、`APP_SECURITY_USERNAME` / `APP_SECURITY_PASSWORD` / `APP_SECURITY_ROLE` をもとに bootstrap user をDBへ作成します。

## テスト実行

```bash
./gradlew test
```

## 認証の考え方

このAPIは stateless な access token 認証です。

- `POST /auth/login` で access token / refresh token を発行します
- `POST /auth/refresh` で refresh token を1回だけ使って新しい token pair を再発行します
- `POST /auth/logout` で access token を即時失効し、保存済み refresh token を削除します
- `/tasks/**` は `ROLE_USER` 以上が必要です
- `/admin/users/**` は `ROLE_ADMIN` のみ利用できます
- `ADMIN` ロールは `USER` 権限も持ちます
- 認証APIの rate limit は DB に保存されるため、アプリ再起動後や複数インスタンスでも維持されます
- 監査ログとして login success / failure、refresh、logout、invalid token を出力します

## 公開URL / 認証必須URL

### 公開URL

- `POST /auth/login`
- `POST /auth/refresh`
- `GET /hello`
- `GET /actuator/health`
- `GET /actuator/health/**`
- `GET /swagger-ui.html`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`
- `GET /study-guide`
- `GET /project-setup-guide`
- `GET /w03-exception-test-guide`
- `GET /w04-jwt-auth-guide`
- 静的リソース配信 (`/*.html`, `/*.css`, `/*.js`, `/webjars/**` など)

### 認証必須URL

- `POST /auth/logout`
- `GET /admin/users`
- `GET /admin/users/{id}`
- `POST /admin/users`
- `PATCH /admin/users/{id}`
- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{id}`
- `PUT /tasks/{id}`
- `DELETE /tasks/{id}`

## JWT発行〜利用手順

### 1. ログインして token pair を取得

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"appuser","password":"change-this-password-123"}'
```

レスポンス例:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "3tf0Q6S3....",
  "tokenType": "Bearer",
  "accessExpiresInSeconds": 900,
  "refreshExpiresInSeconds": 604800,
  "role": "ADMIN"
}
```

### 2. JWTなしで保護APIにアクセスすると401

```bash
curl http://localhost:8080/tasks
```

レスポンス例:

```json
{
  "timestamp": "2026-03-27T18:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/tasks"
}
```

### 3. JWTありで保護APIにアクセスすると成功

以下の例では `jq` を使ってレスポンスJSONから token を取り出しています。

```bash
ACCESS_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"appuser","password":"change-this-password-123"}' | jq -r '.accessToken')

curl http://localhost:8080/tasks \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 4. refresh token で再発行

```bash
REFRESH_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"appuser","password":"change-this-password-123"}' | jq -r '.refreshToken')

curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"${REFRESH_TOKEN}\"}"
```

refresh token はローテーションされるため、使い終わった古い token は再利用できません。

### 5. logout で revoke

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

logout 後は、同じ access token でも `/tasks` にアクセスできず、保存済み refresh token も使えません。

## API一覧

### ログイン

`POST /auth/login`

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"appuser","password":"change-this-password-123"}'
```

### refresh

`POST /auth/refresh`

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your-refresh-token"}'
```

### logout

`POST /auth/logout`

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 管理者向けユーザー一覧

`GET /admin/users`

```bash
curl http://localhost:8080/admin/users \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### 管理者向けユーザー作成

`POST /admin/users`

```bash
curl -X POST http://localhost:8080/admin/users \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"username":"member-user","password":"member-password","role":"USER"}'
```

### 管理者向けユーザー更新

`PATCH /admin/users/{id}`

```bash
curl -X PATCH http://localhost:8080/admin/users/2 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"enabled":false}'
```

### タスクAPI共通

タスクAPIはすべて `Authorization: Bearer <access-token>` が必要です。

### タスク作成

`POST /tasks`

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"title":"task"}'
```

### タスク一覧

`GET /tasks`

```bash
curl http://localhost:8080/tasks \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### タスク詳細取得

`GET /tasks/{id}`

```bash
curl http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

### タスク更新

`PUT /tasks/{id}`

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"title":"updated"}'
```

### タスク削除

`DELETE /tasks/{id}`

```bash
curl -X DELETE http://localhost:8080/tasks/1 \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

## エラーレスポンス

バリデーションエラー、認証エラー、存在しないID、rate limit などでは、共通のエラーレスポンス形式でJSONを返します。

```json
{
  "timestamp": "2026-03-27T02:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired refresh token",
  "path": "/auth/refresh"
}
```

レート制限時の例:

```json
{
  "timestamp": "2026-03-27T02:00:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Too many authentication attempts. Try again later.",
  "path": "/auth/login"
}
```

## セキュリティ強化の内容

- in-memory ユーザーを廃止し、`app_users` テーブルで認証します
- パスワードは BCrypt ハッシュで保存します
- access token には role と token version を持たせています
- logout で token version を進め、発行済み access token を無効化します
- refresh token は DB 保存時にハッシュ化し、再利用不可のローテーション方式です
- JWT secret は環境変数から受け取り、アプリ内デフォルトを持ちません
- 認証APIの rate limit を DB に保持し、再起動や多重起動でもブルートフォース耐性を維持します
- ADMIN 向けユーザー管理APIにより bootstrap user 1件だけに依存しません
- 認証イベントは監査ログとして出力されます

## DB構成

`tasks` テーブル

| column | type |
| --- | --- |
| id | bigint |
| title | varchar |
| created_at | timestamp |
| updated_at | timestamp |

`app_users` テーブル

| column | type |
| --- | --- |
| id | bigint |
| username | varchar |
| password_hash | varchar |
| role | varchar |
| token_version | integer |
| enabled | boolean |
| created_at | timestamp |
| updated_at | timestamp |

`refresh_tokens` テーブル

| column | type |
| --- | --- |
| id | bigint |
| user_id | bigint |
| token_hash | varchar |
| expires_at | timestamp |
| revoked_at | timestamp |
| created_at | timestamp |

`auth_rate_limits` テーブル

| column | type |
| --- | --- |
| id | bigint |
| bucket_key | varchar |
| failure_count | integer |
| window_started_at | timestamp |
| blocked_until | timestamp |
| updated_at | timestamp |

## 学習ポイント

- REST API設計
- Spring Bootの基本構造
- Service層の分離
- DTOの導入
- Validation
- 例外処理の共通化
- JPAによるDB操作
- Spring Securityによる stateless 認証
- JWTの発行と検証
- refresh token ローテーション
- DB永続化された rate limit
- role ベース認可
- 管理者向けユーザー運用API
- rate limit と監査ログ
- DockerによるDB環境構築
