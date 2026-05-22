# Java 后端学习项目

一个面向初学者的 **Maven 多模块** 示例项目，技术栈与你提到的企业常用组合一致：

| 技术 | 版本 | 作用 |
|------|------|------|
| JDK | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 应用框架（最后支持 Java 8 的 2.x 版本） |
| MyBatis Plus | 3.5.7 | ORM，简化 CRUD |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存（本项目用于用户详情缓存） |
| Dubbo | 3.2.15 | RPC 远程调用 |
| Nacos | 2.2.3 | Dubbo 注册中心 |

## 项目结构

```
java-backend-learning/
├── learning-api/          # Dubbo 接口 + DTO（Provider/Consumer 共享）
├── learning-service/      # 业务层：MyBatis Plus + Redis + Dubbo Provider
├── learning-web/          # Web 层：REST API + Dubbo Consumer
├── sql/init.sql           # 数据库初始化脚本
└── docker-compose.yml     # 一键启动 MySQL / Redis / Nacos
```

### 请求链路（建议先理解这条链路）

```
HTTP 请求
  → learning-web (Controller)
    → Dubbo RPC
      → learning-service (UserServiceImpl)
        → UserDomainService
          → MyBatis Plus → MySQL
          → Redis 缓存
```

## 环境准备

### 1. 确认 JDK

```bash
java -version
# 应显示 openjdk version "1.8.x"
```

### 2. 安装 Maven（若尚未安装）

```bash
# Debian/Ubuntu
sudo apt install maven

# 或手动下载：https://maven.apache.org/download.cgi
mvn -version
```

### 3. 安装 Docker（用于 MySQL / Redis / Nacos）

```bash
docker compose version
```

## 快速启动

### 第一步：启动基础设施

```bash
cd /home/eason/projects/java-backend-learning
docker compose up -d
```

等待约 30 秒后，确认容器运行：

```bash
docker compose ps
```

- MySQL: `127.0.0.1:3306`，用户名 `root`，密码 `root123`，库名 `learning_db`
- Redis: `127.0.0.1:6379`
- Nacos 控制台: http://127.0.0.1:8848/nacos （默认无鉴权）

### 第二步：编译项目

```bash
mvn clean package -DskipTests
```

### 第三步：启动服务（先 Provider，后 Consumer）

**终端 1 — 业务服务（Dubbo Provider）**

```bash
java -jar learning-service/target/learning-service-1.0.0-SNAPSHOT.jar
```

**终端 2 — Web 服务（Dubbo Consumer）**

```bash
java -jar learning-web/target/learning-web-1.0.0-SNAPSHOT.jar
```

## API 测试

### 创建用户

```bash
curl -X POST http://127.0.0.1:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"charlie","email":"charlie@example.com","age":28}'
```

### 查询用户列表

```bash
curl http://127.0.0.1:8080/api/users
```

### 按 ID 查询（会走 Redis 缓存）

```bash
curl http://127.0.0.1:8080/api/users/1
```

### 更新用户

```bash
curl -X PUT http://127.0.0.1:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"username":"alice-updated","age":26}'
```

### 删除用户

```bash
curl -X DELETE http://127.0.0.1:8080/api/users/1
```

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

## 学习路线建议

按以下顺序阅读代码，每步配合 `curl` 或 Postman 动手实验：

1. **`learning-api`** — 理解接口与 DTO 如何定义，为什么 Provider/Consumer 要共享这一层
2. **`learning-web/controller/UserController`** — REST 层如何接收请求、做参数校验
3. **`learning-web` 的 `@DubboReference`** — Consumer 如何发现远程服务
4. **`learning-service/dubbo/UserServiceImpl`** — Dubbo Provider 如何暴露服务
5. **`learning-service/entity` + `mapper`** — MyBatis Plus 实体与 Mapper
6. **`learning-service/service/UserDomainService`** — 业务逻辑 + Redis 缓存读写
7. **`application.yml`** — 数据源、Redis、Dubbo 配置

更详细的分步说明见 [docs/LEARNING_PATH.md](docs/LEARNING_PATH.md)。

## 常见问题

**Q: Dubbo 调用失败 / 503**

- 确认 `learning-service` 已启动且 Nacos 正常运行
- 访问 Nacos 控制台，查看 `learning-service` 是否已注册

**Q: 连接 MySQL 失败**

- 确认 `docker compose up -d` 已执行
- 检查 `learning-service/src/main/resources/application.yml` 中的数据库配置

**Q: 想用 IDE 调试**

- IntelliJ IDEA：导入根目录 `pom.xml` 为 Maven 项目
- 分别运行 `LearningServiceApplication` 和 `LearningWebApplication`

## 下一步可练习

- 给 `listUsers` 增加分页（MyBatis Plus `Page`）
- 增加登录接口 + JWT 鉴权
- 为 Dubbo 接口增加集成测试
- 将 Nacos 替换为 Zookeeper，对比注册中心差异
