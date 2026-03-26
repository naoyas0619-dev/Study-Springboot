# Task API

Spring Bootで作成したシンプルなタスク管理APIです。

## 技術スタック

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker
- Gradle

## 機能

- タスク作成
- タスク一覧取得
- タスク詳細取得
- タスク更新
- タスク削除

## セットアップ

このリポジトリでは、DB接続情報を環境変数で管理します。

1. `.env.example` をコピーして `.env` を作成します。
2. `POSTGRES_PASSWORD` を自分の値に変更します。

```bash
cp .env.example .env
```

## 起動方法

### Docker Composeで起動

```bash
docker compose up --build
```

APIは `http://localhost:8080` で利用できます。

学習用ページは `http://localhost:8080/study-guide` で開けます。

環境・構築ガイドページは `http://localhost:8080/project-setup-guide` で開けます。

### Spring Bootだけ起動する場合

PostgreSQLを先に起動し、環境変数を設定してから起動します。

```bash
export DB_URL=jdbc:postgresql://localhost:5432/taskdb
export DB_USERNAME=app
export DB_PASSWORD=your-password
./gradlew bootRun
```

## テスト実行

```bash
./gradlew test
```

バリデーションエラーや存在しないIDでは、共通のエラーレスポンス形式でJSONを返します。

## API一覧

### タスク作成

`POST /tasks`

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"task"}'
```

### タスク一覧

`GET /tasks`

```bash
curl http://localhost:8080/tasks
```

### タスク詳細取得

`GET /tasks/{id}`

```bash
curl http://localhost:8080/tasks/1
```

### タスク更新

`PUT /tasks/{id}`

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"updated"}'
```

### タスク削除

`DELETE /tasks/{id}`

```bash
curl -X DELETE http://localhost:8080/tasks/1
```

## DB構成

`tasks` テーブル

| column | type |
| --- | --- |
| id | bigint |
| title | varchar |
| created_at | timestamp |
| updated_at | timestamp |

## 学習ポイント

- REST API設計
- Spring Bootの基本構造
- Service層の分離
- DTOの導入
- Validation
- 例外処理の共通化
- JPAによるDB操作
- DockerによるDB環境構築
