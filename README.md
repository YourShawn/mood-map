# 心情地图 / Mood Map

Anonymous nearby moods. One tap. About 24 hours, then gone.

MIT. Stack: **Java 17+ / Spring Boot 3** (layered MVC) · **MySQL 8** · **React + Vite + TypeScript** · Leaflet.

This repository is **only** mood-map. No weather shell, no “mom” binding, no prediction, no social profiles.

---

## 中文

打开页面 → 定位（或点地图）→ 点一下心情，可选一句短注 → 看见附近还没淡去的心情。匿名或昵称均可。约 24 小时自动淡出并过期。

### 功能

- 匿名会话（JWT），昵称可选
- 十种心情一键落下
- 附近查询（默认 5 km，Haversine + 包围盒）
- 地图按剩余寿命降低透明度
- 他人坐标约 100 米取整
- 自己的针可撤下

不做：天气、预测、关注/好友、个人主页、账号密码。

### 架构

```
backend/     Spring Boot 3，包分层 MVC
  common/  配置、JWT、地理、错误处理
  auth/    匿名会话与昵称
  mood/    心情写入、附近、过期清理
frontend/  React SPA + Leaflet
docs/      schema.md
```

Flyway 管表结构（主键、外键、CHECK、索引）。OpenAPI：`/swagger-ui.html` 与 `/v3/api-docs`。

### 本地运行

需要 Java 17+、Maven、MySQL 8、Node 20+。MySQL **只能**通过本机回环或私有网络访问，不要把数据库绑到公网 IP，也不要在已提交的配置里写公网主机名。

```bash
cp .env.example .env
# 在本机回环上创建库与用户（MYSQL_HOST=127.0.0.1，与 application.yml 默认值一致）
mysql -h 127.0.0.1 -e "CREATE DATABASE moodmap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
          CREATE USER 'moodmap'@'localhost' IDENTIFIED BY 'moodmap_dev';
          GRANT ALL ON moodmap.* TO 'moodmap'@'localhost';"

cd backend && mvn spring-boot:run
# 另一个终端
cd frontend && npm install && npm run dev
```

浏览器打开 [http://localhost:5173](http://localhost:5173)。允许定位，或直接点地图。

可选：仅在本机回环启动数据库：`docker compose -f docker-compose.dev.yml up`（主机侧仅监听 `127.0.0.1:3306`）。

### Docker Compose

默认 `docker compose up` **不会**启动 MySQL，请把 `MYSQL_HOST` 指向外部实例：

- 与 Docker 同机：`MYSQL_HOST=host.docker.internal` 或宿主机网关
- 同一 Compose 内部网络：用内部服务名（例如可选 profile 下的 `mysql`）
- 生产：未提交的 `.env` 里写私有主机名或 localhost

```bash
cp .env.example .env   # 至少改 JWT_SECRET
# 容器内访问宿主机 MySQL 请把 MYSQL_HOST 设为 host.docker.internal
docker compose up --build
```

可选的单机演示（启动捆绑 MySQL，主机侧仅监听 `127.0.0.1:3306`）：

```bash
# 在 .env 中设置 MYSQL_HOST=mysql，然后：
docker compose --profile bundled-mysql up --build
```

应用：`http://localhost` · API：`http://localhost:8080` · OpenAPI UI：`http://localhost:8080/swagger-ui.html`

### 测试

```bash
cd backend && mvn test
cd frontend && npm test
```

后端冒烟测试覆盖匿名会话、落针、附近、过期隐藏、权限。前端测试覆盖心情目录与淡出计算。

### 配置

见 `.env.example`。`MOOD_TTL_HOURS` 默认 24。`JWT_SECRET` 至少 32 字节。`MYSQL_HOST` 在已提交示例中仅为 `127.0.0.1`；生产请在未提交的 `.env` 里写私有主机名或 localhost，绝不要写公网 IP。

---

## English

Open the app → geolocate (or tap the map) → one-tap a mood, optional short note → see nearby pins that have not faded yet. Anonymous or nickname. Pins auto-fade and expire in about 24 hours.

### Features

- Anonymous JWT session, optional nickname
- Ten one-tap moods
- Nearby query (5 km default, bounding box + Haversine)
- Marker opacity follows remaining TTL
- Other people’s coordinates rounded (~100 m)
- Remove your own pin

Out of scope: weather, prediction, follows/friends, profiles, passwords.

### Architecture

Layered MVC inside `backend/` packages `common`, `auth`, and `mood`. The React SPA talks to `/api`. Schema is Flyway-owned; see [docs/schema.md](docs/schema.md).

```
users 1──< moods   (PK/FK, expires_at, geo index)
```

### Run locally

Java 17+, Maven, MySQL 8, Node 20+. MySQL must be reachable **only on localhost or a private network**. Do not expose it on a public IP or put a public host in committed config.

```bash
cp .env.example .env
# Create the DB on loopback (MYSQL_HOST=127.0.0.1)
mysql -h 127.0.0.1 -e "CREATE DATABASE moodmap CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
          CREATE USER 'moodmap'@'localhost' IDENTIFIED BY 'moodmap_dev';
          GRANT ALL ON moodmap.* TO 'moodmap'@'localhost';"

cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

Vite proxies `/api` to `http://127.0.0.1:8080`.

Optional: start MySQL only on loopback with `docker compose -f docker-compose.dev.yml up` (published only on `127.0.0.1:3306`).

### Docker Compose

Default `docker compose up` does **not** start MySQL; point `MYSQL_HOST` at an external instance:

- Same host as Docker: `host.docker.internal` or the host gateway (Linux Compose maps `host.docker.internal` via `extra_hosts`).
- Another container on an internal Compose network: that service hostname (for example `mysql` with the optional profile below).
- Production: a private hostname or localhost in uncommitted `.env`.

```bash
cp .env.example .env
# Compose cannot use 127.0.0.1 inside the backend container for host MySQL.
# Set MYSQL_HOST=host.docker.internal in .env (or the host gateway).
docker compose up --build
```

Optional solo local demo (bundled MySQL on the Compose network, published only on `127.0.0.1:3306`):

```bash
# Set MYSQL_HOST=mysql in .env, then:
docker compose --profile bundled-mysql up --build
```

Then open `http://localhost`.

### Tests / OpenAPI

`mvn test` in `backend/` (API smoke tests against MySQL `moodmap_test` on `127.0.0.1`). `npm test` in `frontend/`. Live spec: `/v3/api-docs`.

See `.env.example`. `MYSQL_HOST` in committed examples is `127.0.0.1` only; production uses a private hostname or localhost in uncommitted `.env`. Never a public IP.

### License

[MIT](LICENSE)
