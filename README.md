# EGAP Backend

基于 Spring Boot 的后端服务，提供与前端联调所需的完整接口、文档、监控与测试。

## 快速开始

- 运行要求：JDK 17+，Maven 3.9+
- 启动：
  - `mvn -DskipTests spring-boot:run`
  - 或 `mvn -DskipTests package && java -jar target/egap-backend-0.1.0.jar`

## 环境变量

- `SERVER_PORT`：服务端口，默认 `8080`
- `EGAP_ALLOWED_ORIGINS`：CORS 允许来源，默认 `http://localhost:5173`

## 主要接口

- `GET /api/health`
- `GET /api/search?q=...`
- `GET /api/tags?query=...`
- `GET /api/tags/distribution`
- `GET /api/enterprise/basic-info?category=...`
- `GET /api/modeling/pressure`
- `GET /api/modeling/training`

## 文档与监控

- Swagger UI：`http://localhost:8080/swagger-ui/index.html`
- Actuator：
  - 健康：`/actuator/health`
  - 指标：`/actuator/metrics`
  - Prometheus：`/actuator/prometheus`

## 联调说明

- 前端将 `VITE_API_BASE_URL` 配置为 `http://localhost:8080/api`
- 若前端有本地 Mock 插件，建议在存在 `VITE_API_BASE_URL` 时跳过 Mock。

## 测试

- 单元与集成测试：`mvn test`
- 样例数据：内存 H2 数据库自动建表并初始化，H2 控制台 `/h2-console`