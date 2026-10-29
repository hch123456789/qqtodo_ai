# QQ Todo 助手

一个基于 **QQ 消息 + AI 大模型** 的自动任务整理工具。

自动接收 QQ 群消息 → 判断是否与你相关 → 抽取「时间/地点/任务」→ 生成待办清单 → 解析老师发的文件 → 定时 QQ 提醒。

核心亮点：**AI 会越用越懂你**——你在网页上确认/拒绝/修正待办，AI 会学习你的偏好，逐渐更精准地判断什么与你相关。

---

## 功能特性

- ✅ 自动接收 QQ 群消息，按「群白名单 + 标记人 + 关键词」过滤
- ✅ AI 分析消息相关性，自动抽取待办事项（时间、地点、任务、优先级）
- ✅ 自动解析老师发的文件（PDF / Word / Excel / WPS 变体），AI 生成摘要
- ✅ AI 学习机制：确认/拒绝/修正 + 不相关程度反馈，自动提炼偏好规则
- ✅ 深色毛玻璃风格 Web 界面（待办 / 消息 / 资料 / 画像 / 群管理 / 联系人）
- ✅ 定时检查临近截止的待办，通过 QQ 私聊提醒
- ✅ 画像可编辑，换班级、换身份后依然适用

---

## 技术栈

| 模块 | 技术 |
|------|------|
| 后端 | Spring Boot 3 + Java 17 |
| 数据库 | MySQL 8 + Spring Data JPA |
| QQ 接入 | NapCat（OneBot 11 协议） |
| AI | DeepSeek / 通义千问 / 智谱 GLM 等（OpenAI 兼容接口） |
| 文件解析 | Apache PDFBox + Apache POI |
| 前端 | Thymeleaf + 原生 CSS |

---

## 系统架构

```
[QQ 群/私聊]
    ↓ 消息事件（HTTP 上报）
[NapCat]  ← 独立程序，登录 QQ 小号
    ↓ 调用 HTTP API 发提醒
[Spring Boot 后端]
    ├─ 接收消息事件
    ├─ 过滤（群白名单 / 标记人 / 关键词）
    ├─ 调用 AI 分析并生成 todo
    ├─ 下载并解析文件，AI 总结
    ├─ 存储 MySQL
    └─ 提供 Web 页面 + REST API
[Web 前端]  展示 todo / 消息 / 资料 / 画像
[MySQL]     存储所有数据
```

---

## 环境要求

- JDK 17
- MySQL 8.x
- NapCat（需 Windows QQ 或 Linux QQ）
- 一个 QQ 小号（务必用小号，勿用主号）
- 一个 AI 平台账号（如 DeepSeek），获取 API Key

---

## 快速开始（本地部署）

### 第 1 步：准备环境

1. 安装 JDK 17、MySQL 8；
2. 下载并解压 [NapCat](https://github.com/NapNeko/NapCatQQ/releases)；
3. 注册 [DeepSeek](https://platform.deepseek.com) 获取 API Key。

### 第 2 步：初始化数据库

```bash
mysql -u root -p < schema.sql
```

### 第 3 步：配置 application.yml

在项目 `src/main/resources/application.yml` 中填写：

```yaml
spring:
  datasource:
    password: 你的数据库密码
ai:
  api-key: 你的AIKey
qq:
  self-id: 你的QQ号
```

### 第 4 步：启动后端

IDEA 中运行 `QqTodoAiApplication`，或打包后：

```bash
mvn clean package -DskipTests
java -jar target/qq-todo-0.0.1-SNAPSHOT.jar
```

### 第 5 步：配置 NapCat

1. 运行 NapCat，扫码登录 QQ 小号；
2. 打开 NapCat WebUI（默认 `http://127.0.0.1:6099/webui`）；
3. 配置 **HTTP 上报**：
   - 上报地址：`http://127.0.0.1:8080/qq/event`
   - 上报事件：`消息事件（message）`
   - 消息格式：`array`
4. 配置 **HTTP API 服务器**（用于后端发提醒）：
   - 监听地址：`127.0.0.1`
   - 端口：`3000`
   - access_token：留空（本地开发）

### 第 6 步：网页配置

打开 `http://localhost:8080/`：

- **我的画像**（`/profile`）：填写你的身份（姓名、班级、职务等）；
- **群管理**（`/groups`）：添加你要关注的群；
- **联系人**（`/contacts`）：标记老师、辅导员、班委。

配置完成后，在群里发消息测试即可。

---

## 打包发布（给同学用）

本项目**不含任何个人信息**，可直接打包分发。

### 打包

```bash
mvn clean package -DskipTests
```

产物：`target/qq-todo-0.0.1-SNAPSHOT.jar`

### 交付物

1. `qq-todo-0.0.1-SNAPSHOT.jar`
2. `schema.sql`
3. 一份 `application.yml` 模板（占位符，同学自己填）

### 同学部署步骤

1. 安装 JDK 17 + MySQL 8 + NapCat；
2. 执行 `schema.sql` 建库建表；
3. 填写自己的 `application.yml`（数据库密码、AI Key、QQ 号）；
4. 运行 jar；
5. 配置 NapCat（登录自己的 QQ 小号）；
6. 网页填写画像、群、联系人。

> 注意：每个同学使用**自己的 QQ 小号 + 自己的数据库 + 自己的 AI Key**，数据完全隔离。

---

## 配置说明

| 配置项 | 说明 | 必填 |
|--------|------|------|
| `spring.datasource.password` | 数据库密码 | ✅ |
| `ai.api-key` | AI 平台 API Key | ✅ |
| `ai.api-url` | AI 接口地址 | ✅（默认 DeepSeek） |
| `ai.model` | 模型名 | ✅（默认 deepseek-chat） |
| `qq.self-id` | 你自己的 QQ 号（提醒发给自己） | ✅ |
| `qq.http-api-url` | NapCat HTTP API 地址 | ✅ |
| `qq.reminder-hours` | 提前多少小时提醒 | 否（默认 24） |
| `file.storage-path` | 文件下载目录 | 否（默认 ./files） |

---

## Docker 部署（可选）

```bash
# 1. 打包
mvn clean package -DskipTests

# 2. 修改 docker-compose.yml 里的密码和 AI Key

# 3. 启动 MySQL + 后端
docker compose up -d
```

NapCat 建议在宿主机单独运行（需扫码登录），配置 HTTP 上报到后端、HTTP API 供后端调用。

---

## 常见问题

### 1. QQ 收不到消息（后端没反应）
- 检查 NapCat HTTP 上报地址是否填对：`http://127.0.0.1:8080/qq/event`
- 检查后端是否启动、端口是否 8080

### 2. QQ 收不到提醒
- 检查 NapCat HTTP API 端口（默认 3000）是否和后端 `qq.http-api-url` 一致
- 若报 `token verify failed!`，清空 NapCat 的 access_token，或后端配置 `qq.access-token`

### 3. 文件解析失败
- 只支持 pdf / doc / docx / xlsx；扫描版 PDF 需 OCR（暂不支持）
- 部分 `.doc` 实际是 Word 2003 XML 格式，系统已兼容

### 4. 消息被过滤
- 确认群在「群管理」白名单里
- 确认发送者是标记人，或消息含关键词（作业/截止/提交/考试/通知/报告/实验等）

### 5. AI 判断不准
- 在待办页对误判的 todo 点「拒绝」并给不相关程度、备注，AI 会学习
- 在画像页点「让 AI 提炼规则」可加速学习

---

## 目录结构

```
qq-todo
├── pom.xml
├── schema.sql
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/main
    ├── java/com/example/qqtodoAi
    │   ├── config/          # 线程池、RestTemplate 配置
    │   ├── controller/      # REST API + 页面控制器
    │   ├── model/           # 实体 + DTO
    │   ├── repository/      # JPA DAO
    │   └── service/         # 业务逻辑（AI、解析、学习、提醒）
    └── resources
        ├── application.yml
        ├── static/css/style.css
        └── templates/       # Thymeleaf 页面
```

---

## 免责声明

本项目仅供**个人学习、自用**。QQ 个人号自动化存在账号风险，请使用小号测试，控制消息频率，勿用于商业或骚扰用途。由此产生的账号风险由使用者自行承担。
