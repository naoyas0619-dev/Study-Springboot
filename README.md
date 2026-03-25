# Task API

Spring Bootで作成したシンプルなタスク管理APIです。

## 技術スタック

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker
- Gradle

---

## 機能

このAPIでは以下の操作が可能です。

- タスク作成
- タスク一覧取得
- タスク詳細取得
- タスク更新
- タスク削除

---

## API一覧

### タスク作成

POST /tasks

例
# Task API

Spring Bootで作成したシンプルなタスク管理APIです。

## 技術スタック

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Docker
- Gradle

---

## 機能

このAPIでは以下の操作が可能です。

- タスク作成
- タスク一覧取得
- タスク詳細取得
- タスク更新
- タスク削除

---

## API一覧

### タスク作成

POST /tasks

例
curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title":"task"}'

---

### タスク一覧

GET /tasks

例
curl http://localhost:8080/tasks

---

### タスク取得

GET /tasks/{id}

例
curl -X PUT http://localhost:8080/tasks/1 -H "Content-Type: application/json" -d '{"title":"updated"}'

---

### タスク削除

DELETE /tasks/{id}

例
curl -X DELETE http://localhost:8080/tasks/1

---

## DB構成

tasks テーブル

| column | type |
|------|------|
| id | bigint |
| title | varchar |
| created_at | timestamp |
| updated_at | timestamp |

---

## 起動方法

### PostgreSQL起動
docker run -d
-p 5432:5432
-e POSTGRES_DB=taskdb
-e POSTGRES_USER=app
-e POSTGRES_PASSWORD=app
--name task-db
postgres:16

### Spring Boot起動
./gradlew bootRun

---

## 学習ポイント

このプロジェクトでは以下を学習しました。

- REST API設計
- Spring Bootの基本構造
- Service層の分離
- DTOの導入
- Validation
- 例外処理の共通化
- JPAによるDB操作
- DockerによるDB環境構築
