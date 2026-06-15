# 项目名称

GitHub 仓库地址：https://github.com/你的用户名/仓库名

## 1. 项目简介

- 应用名称：
- 目标用户：
- 核心功能：

## 2. 技术栈

- UI：Jetpack Compose + Material 3
- 数据库：Room
- 网络：Retrofit / OkHttp / Ktor（接口来源：XXX）
- 状态管理：ViewModel + StateFlow
- 持久化偏好：DataStore
- 导航：Navigation Compose
- 异步处理：Kotlin Coroutines
- 其他依赖：（根据实际情况填写）

## 3. 功能清单

### 必做项完成情况

**UI 层**
- [x] Jetpack Compose 构建全部 UI
- [x] 至少 2 个主要页面
- [x] Compose Navigation 导航
- [x] LazyColumn / LazyVerticalGrid 列表
- [x] Material 3 组件和主题
- [x] 浅色 / 深色模式支持

**数据层**
- [x] Room 数据库，至少 2 张表
- [x] 完整 CRUD 操作
- [x] DAO 查询方法返回 Flow 类型
- [x] 至少一种查询功能
- [x] DataStore 保存用户偏好或最近状态

**网络层**
- [x] 声明并使用 Internet 权限
- [x] 使用网络请求获取真实 API 或 Mock API 数据
- [x] 网络数据在核心页面中展示或参与主要功能流程
- [x] 处理 Loading / Success / Error 等网络状态
- [x] Composable 不直接发起网络请求

**架构层**
- [x] ViewModel 状态管理
- [x] Repository 模式
- [x] StateFlow / Flow 数据流
- [x] Kotlin 协程异步处理
- [x] UiState 描述界面状态
- [x] Composable 不直接访问数据库或网络

**功能完整性**
- [x] 新增 / 编辑 / 删除 / 搜索等核心操作（至少 2 项）
- [x] 输入验证和错误提示
- [x] 状态展示（空 / 加载 / 错误中的至少一种）
- [x] 屏幕旋转后状态保持

### 选做项完成情况

- [x] 示例：复杂数据库查询
- [x] 示例：搜索防抖或搜索历史
- [ ] 未完成的项保持未勾选

## 4. 数据库设计

### 表 1：XXX

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Int | 主键，自增 |
| name | String | 名称 |
| createdAt | Long | 创建时间戳 |

### 表 2：XXX

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Int | 主键，自增 |
| ... | ... | ... |

说明表关系和主要 DAO 查询方法。

## 5. 网络功能设计

- API 来源：
- 接口地址：
- 请求方式：
- 主要返回字段：
- App 中使用这些网络数据的页面或功能：
- 网络失败时的处理方式：

## 6. 架构设计

说明 Data Layer、Repository、ViewModel、UiState、UI Layer 之间的关系。

## 7. 核心功能截图

### 首页
![首页截图](screenshots/home.png)
说明：展示 XXX 列表，用户可以...

### 详情页或核心功能页
![详情页或核心功能页截图](screenshots/detail.png)
说明：...

### 核心功能页
![功能页截图](screenshots/other.png)
说明：...

## 8. 技术难点与解决方案

### 难点 1：XXX

- 问题描述：
- 原因分析：
- 解决方案：
- 参考资料：

### 难点 2：XXX

- 问题描述：
- 原因分析：
- 解决方案：

## 9. AI 使用说明

请在以下选项中勾选，可多选：

- [ ] 未使用 AI
- [ ] 网页版 AI（如 ChatGPT、Claude、Kimi、豆包等）
- [ ] AI Agent / 编程代理（如 Claude Code、Codex、OpenCode、Cursor Agent 等）
- [ ] 国产大模型服务（如 DeepSeek、GLM、通义千问、文心一言等）
- [ ] IDE 插件或代码补全工具（如 GitHub Copilot、Cursor、CodeGeeX 等）
- [ ] 其他：

具体工具名称：

AI 主要用于哪些环节：（如选题分析、代码生成、调试、报告整理等）

说明：是否使用 AI 以及使用了什么 AI 工具不会影响分值，请如实填写。

## 10. 运行说明

- 最低 Android 版本：API 24（Android 7.0）
- 推荐 Android 版本：API 34（Android 14）
- 特殊权限：网络权限；如使用相机、通知等功能，也需要说明对应权限
- 运行步骤：
  1. 克隆仓库：`git clone https://github.com/你的用户名/仓库名`
  2. 使用 Android Studio 打开项目
  3. 等待 Gradle 同步完成
  4. 连接模拟器或真机，点击 Run

## 11. 项目亮点（可选）

说明你认为做得比较好的地方。

## 12. 未来改进方向（可选）

说明如果有更多时间会如何改进。
