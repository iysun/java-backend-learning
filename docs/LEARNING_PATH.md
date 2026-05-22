# 学习路径详解

本文档配合源码，帮助你从零理解 Java 后端各层职责。

## 模块 1：learning-api（契约层）

**你要学会什么：** 微服务/RPC 架构中，调用方和被调用方如何约定接口。

| 文件 | 说明 |
|------|------|
| `UserService.java` | Dubbo 服务接口，只有方法签名 |
| `dto/UserDTO.java` | 返回给前端的数据结构 |
| `dto/CreateUserRequest.java` | 创建用户的入参 |

**关键点：**

- 接口模块不依赖 Spring、MyBatis，保持轻量
- DTO 必须实现 `Serializable`（Dubbo 网络传输需要）
- Provider 和 Consumer 都依赖 `learning-api`，保证接口一致

---

## 模块 2：learning-service（业务 + 数据层）

**你要学会什么：** 数据如何持久化、如何缓存、如何对外提供 RPC 服务。

### 2.1 启动类

`LearningServiceApplication.java`

- `@SpringBootApplication`：Spring Boot 入口
- `@EnableDubbo`：启用 Dubbo
- `@MapperScan`：扫描 MyBatis Mapper

### 2.2 实体与 Mapper

`entity/User.java` + `mapper/UserMapper.java`

```java
// MyBatis Plus 方式：继承 BaseMapper 即拥有 CRUD
public interface UserMapper extends BaseMapper<User> { }
```

对比传统 MyBatis：无需写 XML 即可完成基础增删改查。

### 2.3 业务服务

`service/UserDomainService.java`

重点阅读 `getUserById` 方法：

1. 先查 Redis
2. 缓存未命中再查 MySQL
3. 查完后写入 Redis（30 分钟过期）

这就是典型的 **Cache-Aside（旁路缓存）** 模式。

### 2.4 Dubbo Provider

`dubbo/UserServiceImpl.java`

```java
@DubboService(version = "1.0.0")
public class UserServiceImpl implements UserService { ... }
```

`@DubboService` 把实现类注册到 Nacos，供其他服务调用。

### 2.5 配置

`application.yml` 中关注三块：

- `spring.datasource` → MySQL
- `spring.redis` → Redis
- `dubbo.registry.address` → Nacos 地址

---

## 模块 3：learning-web（接入层）

**你要学会什么：** 如何把 HTTP 请求转成 RPC 调用，并统一返回格式。

### 3.1 Controller

`controller/UserController.java`

- `@RestController` + `@RequestMapping` 定义 REST 路由
- `@Valid` + JSR-303 注解做参数校验
- `@DubboReference` 注入远程 `UserService`

### 3.2 统一响应

`common/Result.java`

企业项目通常不会直接返回实体，而是包装成 `{ code, message, data }`。

### 3.3 全局异常

`exception/GlobalExceptionHandler.java`

- 校验失败 → 400
- Dubbo 不可用 → 503
- 其他异常 → 500

---

## 动手实验清单

完成以下实验，基本就入门了：

- [ ] 启动 docker-compose，确认三个容器 healthy
- [ ] 编译并启动两个 Spring Boot 应用
- [ ] 用 curl 完成 CRUD 全流程
- [ ] 打开 Redis CLI，观察 `user:id:1` 缓存键
- [ ] 打开 Nacos 控制台，查看服务注册列表
- [ ] 修改 `UserDomainService` 中缓存过期时间，观察行为变化
- [ ] 故意停掉 `learning-service`，看 Web 层返回什么错误

---

## 架构图

```
┌─────────────┐     HTTP      ┌──────────────┐
│   浏览器     │ ────────────► │ learning-web │ :8080
│  / curl     │               │  Controller  │
└─────────────┘               └──────┬───────┘
                                     │ @DubboReference
                                     ▼
                              ┌──────────────┐
                              │    Nacos     │ :8848
                              │  注册中心     │
                              └──────┬───────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │learning-service│ :8081 / :20880
                              │ UserServiceImpl│
                              └──────┬───────┘
                          ┌──────────┴──────────┐
                          ▼                     ▼
                    ┌──────────┐         ┌──────────┐
                    │  MySQL   │         │  Redis   │
                    │  :3306   │         │  :6379   │
                    └──────────┘         └──────────┘
```

---

## 推荐延伸阅读

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Apache Dubbo 快速开始](https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/quick-start/)
