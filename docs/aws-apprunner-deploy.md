# AWSデプロイ手順（App Runner + RDS PostgreSQL）

## このリポジトリで選ぶ方式

- アプリ公開: AWS App Runner
- データベース: Amazon RDS for PostgreSQL
- コンテナ保管: Amazon ECR
- 任意の自動化: GitHub Actions

## この方式を選んだ理由

- 既存の `Dockerfile` をそのまま使える
- HTTPS付きの公開URLを比較的少ない設定で持てる
- ECS + ALB より構成が軽く、学習兼ポートフォリオ用途に向く
- PostgreSQL前提のため、DBはマネージドな RDS に寄せるのが自然

## リポジトリ内の関連ファイル

- `Dockerfile`: App Runner に載せるコンテナイメージを作る
- `deploy/aws/apprunner.env.example`: App Runner 用の環境変数テンプレート
- `.github/workflows/deploy-apprunner.yml`: ECR への push と App Runner 再デプロイ用の workflow
- `src/main/resources/application.properties`: `PORT` とプロキシ配下を考慮したクラウド向け設定

## 事前に必要なもの

- AWS アカウント
- デプロイ先リージョン
- GitHub リポジトリ
- AWS 側で作成する ECR リポジトリ
- AWS 側で作成する RDS for PostgreSQL
- App Runner から RDS に接続するための VPC Connector
- GitHub Actions から AWS に入るための IAM Role（OIDC）

## App Runner に設定する環境変数

`deploy/aws/apprunner.env.example` をベースに、App Runner の環境変数または Secrets Manager へ設定します。

最低限必要な値:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_SECURITY_USERNAME`
- `APP_SECURITY_PASSWORD`
- `JWT_SECRET`

補足:

- `PORT` は `8080` のままで問題ありません
- `APP_SECURITY_ROLE` は初回公開なら `ADMIN` のままで開始できます
- `JWT_SECRET` は 32 byte 以上の十分長い値を使ってください

## 初回デプロイ手順

### 1. ローカルで動作確認する

```bash
./gradlew test
./gradlew build
docker compose config
```

### 2. ECR リポジトリを作る

例:

```bash
aws ecr create-repository --repository-name task-api
```

### 3. RDS for PostgreSQL を作る

推奨方針:

- App Runner と同じリージョンに作る
- RDS は private subnet に置く
- RDS の security group は `5432` を App Runner VPC Connector 側の security group からだけ許可する

作成後に控える値:

- RDS endpoint
- DB 名
- DB ユーザー名
- DB パスワード

### 4. App Runner から RDS へ届くようにする

App Runner Service 作成時に:

- VPC Connector を設定する
- RDS に到達できる subnet / security group を選ぶ
- Health check path を `/actuator/health` にする
- Container port を `8080` にする

### 5. 最初の Docker イメージを ECR に push する

```bash
AWS_REGION=ap-northeast-1
AWS_ACCOUNT_ID=<your-account-id>
ECR_REPOSITORY=task-api
IMAGE_URI=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

docker build -t "${IMAGE_URI}:latest" .
docker push "${IMAGE_URI}:latest"
```

### 6. App Runner Service を作る

AWS Console での作成が最も簡単です。入力ポイントは次のとおりです。

- Source: `Container registry`
- Provider: `Amazon ECR`
- Image: `task-api:latest`
- Deployment trigger: 最初は `Manual` で開始してもよい
- Port: `8080`
- Health check path: `/actuator/health`
- Runtime environment variables: `deploy/aws/apprunner.env.example` を元に設定
- Network: RDS 接続用の VPC Connector を設定

公開後の確認:

- `GET /actuator/health`
- `GET /swagger-ui/index.html`
- `POST /auth/login`

## GitHub Actions で更新デプロイする

このリポジトリには `.github/workflows/deploy-apprunner.yml` を追加しています。実行前に GitHub 側へ以下を設定してください。

### GitHub Secrets

- `AWS_ROLE_TO_ASSUME`: GitHub OIDC から AssumeRole する IAM Role ARN

### GitHub Variables

- `AWS_REGION`: 例 `ap-northeast-1`
- `ECR_REPOSITORY`: 例 `task-api`
- `APP_RUNNER_SERVICE_ARN`: 既存 App Runner サービス ARN

`APP_RUNNER_SERVICE_ARN` は、App Runner 側で ECR 自動デプロイを有効にしている場合は必須ではありません。workflow から明示的に再デプロイしたい場合に設定してください。

workflow がやること:

- AWS に OIDC でログイン
- Docker イメージを build
- ECR に `sha-<commit>` と `latest` を push
- `aws apprunner start-deployment` で App Runner を更新

## App Runner 側で入れる値の例

```env
PORT=8080
DB_URL=jdbc:postgresql://your-rds-endpoint.ap-northeast-1.rds.amazonaws.com:5432/taskdb
DB_USERNAME=app
DB_PASSWORD=<set-in-app-runner-or-secrets-manager>
APP_SECURITY_USERNAME=appuser
APP_SECURITY_PASSWORD=<set-in-app-runner-or-secrets-manager>
APP_SECURITY_ROLE=ADMIN
JWT_SECRET=<set-in-app-runner-or-secrets-manager>
JWT_ACCESS_EXPIRATION=PT15M
JWT_REFRESH_EXPIRATION=P7D
AUTH_RATE_LIMIT_MAX_ATTEMPTS=5
AUTH_RATE_LIMIT_WINDOW=PT15M
AUTH_RATE_LIMIT_BLOCK_DURATION=PT15M
```

## このリポジトリで実施できる範囲

実施できること:

- App Runner 向けのアプリ設定追加
- App Runner / ECR / RDS 前提の環境変数テンプレート追加
- GitHub Actions workflow 追加
- 手順書整備

このリポジトリだけでは完了できないこと:

- AWS リソース作成
- IAM / OIDC 設定
- VPC / Security Group 設定
- 実際の App Runner デプロイ実行

## 人が次にやる操作

1. AWS で ECR / RDS / App Runner / VPC Connector を作る
2. App Runner の環境変数または Secrets Manager に本番値を設定する
3. GitHub Secrets / Variables を登録する
4. GitHub Actions `Deploy to AWS App Runner` を手動実行する
5. App Runner の公開URLで `health` と `login` を確認する
