# 课程表管理

GitHub 仓库地址：https://github.com/chenbangguo/2025003014-FinalProject

## 1. 项目简介

- 应用名称：课程表管理
- 目标用户：大学生及需要管理课程安排的学习者
- 核心功能：
  - 可视化课程表：7×6 网格布局展示一周课程，支持按星期、节次、单双周管理
  - 课程增删改查：添加、编辑、删除课程，15种颜色标记，支持备注
  - 课程列表视图：卡片式列表展示所有课程，支持快速编辑和删除
  - 通知公告：查看学院或教师发布的通知信息
  - 作业管理：教师发布作业，学生查看和提交作业
  - 角色切换：支持教师/学生角色切换，不同角色拥有不同功能权限
  - 番茄专注计时：与课程表联动，选择课程后开启专注计时
  - 任务管理：创建学习任务，跟踪番茄完成进度
  - 历史记录：查看专注时长与完成统计

## 2. 技术栈

- UI：Jetpack Compose + Material 3
- 数据库：Room
- 网络：Retrofit 2.9.0 + OkHttp 4.12.0 + Gson（接口来源：Quotable.io）
- 状态管理：ViewModel + StateFlow
- 持久化偏好：DataStore
- 导航：Navigation Compose
- 异步处理：Kotlin Coroutines
- 其他依赖：Material Icons Extended、ViewModel Compose

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

- [x] 复杂数据库查询（`getCoursesByDay` 按天查询、`getTodayCompletedWorkSessions` 按日期统计）
- [x] 搜索防抖（任务搜索实现 300ms 防抖）
- [x] 多角色权限管理（教师端/学生端切换，不同角色拥有不同功能）
- [ ] 未完成的项保持未勾选

## 4. 数据库设计

### 表 1：courses（课程表）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键，自增 |
| name | String | 课程名称（必填） |
| teacher | String | 授课教师 |
| classroom | String | 上课教室 |
| dayOfWeek | Int | 星期（1=周一 ~ 7=周日） |
| startPeriod | Int | 起始节次（1~12） |
| endPeriod | Int | 结束节次（1~12） |
| weekType | String | 周类型：all=每周 / odd=单周 / even=双周 |
| color | Int | 课程颜色（ARGB整数值，15种预设颜色） |
| note | String | 备注 |

### 表 2：tasks（任务表）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键，自增 |
| title | String | 任务标题 |
| description | String | 任务描述 |
| estimatedPomodoros | Int | 预估番茄数 |
| completedPomodoros | Int | 已完成番茄数 |
| isActive | Boolean | 是否激活（进行中） |
| createdAt | Long | 创建时间戳 |
| updatedAt | Long | 最后更新时间戳 |

### 表 3：pomodoro_sessions（番茄会话表）

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键，自增 |
| taskId | Long? | 关联任务ID（可为空） |
| startTime | Long | 开始时间戳 |
| endTime | Long | 结束时间戳 |
| durationSeconds | Int | 持续时间（秒） |
| type | String | 会话类型：work / short_break / long_break |
| isCompleted | Boolean | 是否完成 |
| note | String | 备注 |

**表关系：** tasks 与 pomodoro_sessions 通过 taskId 一对多关联。courses 表独立，用于课程表功能。

**主要 DAO 查询方法：**
- `CourseDao.getAllCourses(): Flow<List<CourseEntity>>` — 按星期+节次排序，响应式更新
- `CourseDao.getCoursesByDay(day): Flow<List<CourseEntity>>` — 按星期几查询
- `CourseDao.insertCourse()` / `updateCourse()` / `deleteCourse()` — 完整 CRUD
- `TaskDao.searchTasks(query)` — 模糊搜索任务
- `PomodoroSessionDao.getTodayCompletedWorkSessions()` — 今日完成统计
- `PomodoroSessionDao.getTotalWorkSeconds()` — 总专注时长聚合

## 5. 网络功能设计

- API 来源：Quotable.io（免费开源名言 API）
- 接口地址：`https://api.quotable.io/random?tags=motivational|productivity`
- 请求方式：GET
- 主要返回字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| \_id | String | 名言ID |
| content | String | 名言内容 |
| author | String | 作者 |
| authorSlug | String | 作者标识 |
| tags | List\<String\> | 标签列表 |

- App 中使用这些网络数据的页面或功能：TimerScreen（番茄钟页面，以励志格言卡片形式展示）
- 网络失败时的处理方式：
  - `fetchMotivationalQuote()` 返回 `Result<QuoteDto>` 封装成功/失败
  - 失败时 catch 异常，显示本地默认格言："千里之行，始于足下。"
  - UI 层通过 `QuoteState` sealed interface 区分 Idle / Loading / Success / Error 四种状态

## 6. 架构设计

采用 MVVM + Repository 模式，严格遵循单向数据流：

```
┌──────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                  │
│  CourseScheduleScreen / CourseTable / CourseDialog   │
│  仅持有 ViewModel 引用，通过 collectAsState() 收集状态 │
├──────────────────────────────────────────────────────┤
│               ViewModel Layer                        │
│              CourseViewModel                         │
│  管理 CourseUiState，调用 Repository，暴露 StateFlow   │
│  方法：showAddDialog / showEditDialog / saveCourse / │
│        deleteCourse                                  │
├──────────────────────────────────────────────────────┤
│              Repository Layer                        │
│           PomodoroRepository                         │
│  统一数据访问入口，封装 CourseDao / TaskDao 操作       │
├──────────────────────────┬───────────────────────────┤
│       Local Data          │       Remote Data         │
│  Room DB（3张表）          │  Retrofit + OkHttp        │
│  DataStore Preferences    │  Quotable.io API          │
└──────────────────────────┴───────────────────────────┘
```

**数据流向（以课程表为例）：**
1. 用户在 UI 触发操作（如点击"添加课程"）→ ViewModel 调用 Repository
2. Repository 操作 CourseDao（insert/update/delete）
3. Room 数据变更，Flow 自动发射新数据
4. ViewModel 中 `courseDao.getAllCourses().collect` 接收变化
5. `CourseUiState.courses` 更新 → Compose UI 自动重组

**关键原则：**
- Composable 不直接访问 Room/Retrofit，全部通过 ViewModel → Repository → DAO
- `CourseUiState` 封装 UI 状态：`courses`（列表）、`editingCourse`（编辑中）、`showDialog`（对话框显隐）
- ViewModel 通过 `ViewModelProvider.Factory` 注入依赖
- 使用 `viewModelScope` 管理协程生命周期

## 7. 核心功能截图

### 首页 — 课程表主页面


![alt text](image.png)

说明：展示一周课程安排的可视化表格。顶部为星期标题（周一至周日），左侧为时间段（1-2节至11-12节）。每个单元格根据 dayOfWeek 和 startPeriod/endPeriod 自动定位课程，不同课程用不同颜色区分。右上角 + 按钮可添加新课程。

### 课程添加/编辑对话框

![alt text](image-1.png)

说明：弹出式对话框用于添加或编辑课程。包含课程名称输入框（必填）、教师/教室输入框、星期选择（FilterChip 组，7选1）、起始/结束节次下拉选择（联动约束，起始≤结束）、周类型选择（每周/单周/双周）、15色颜色选择器、备注输入框。

### 通知公告

![alt text](image-2.png)

说明：展示学院或教师发布的通知公告列表，支持查看公告详情，方便学生及时获取课程变动、考试安排等重要信息。

### 发布作业

![alt text](image-3.png)

说明：教师端发布作业功能，支持填写作业标题、内容描述、截止时间等信息。学生端可查看已发布的作业列表并提交。

### 设置页面 — 角色切换

![alt text](image-4.png)

说明：设置页面支持教师/学生角色切换。切换后界面功能和权限自动调整，教师端拥有发布公告和作业的功能，学生端侧重于查看和提交。
## 8. 技术难点与解决方案

### 难点 1：课程表 7×6 网格布局与跨节次渲染

- 问题描述：需要实现类似真实课表的网格布局（7天×6个时间段），且课程可能跨多个节次（如 1-3 节），要求只在起始格显示课程内容，后续格留空但保持占位。
- 原因分析：Compose 没有原生 TableLayout 组件；课程跨节次导致行列渲染逻辑复杂；需在有限空间内显示课程名称、教室、颜色等信息。
- 解决方案：
  - 使用嵌套 `Row` + `Column` 实现表格：外层 `Column` 遍历 6 个时间段，内层 `Row` 遍历 7 天
  - 每个单元格过滤课程时判断 `dayOfWeek == day && startPeriod <= endPeriod && endPeriod >= startPeriod`
  - 只在 `cellCourse.startPeriod == startPeriod` 时渲染内容，实现跨节次合并
  - 使用 `weight(1f)` 等宽列，`border(0.5.dp)` 绘制表格线
  - 背景色用 `Color(cellCourse.color).copy(alpha = 0.15f)` 半透明处理

```kotlin
for (day in 1..7) {
    val dayCourses = courses.filter {
        it.dayOfWeek == day &&
        it.startPeriod <= endPeriod &&
        it.endPeriod >= startPeriod
    }
    val cellCourse = dayCourses.firstOrNull()
    if (cellCourse != null && cellCourse.startPeriod == startPeriod) {
        // 渲染课程名称、教室、颜色条
    }
}
```

### 难点 2：课程表单的双下拉框联动约束

- 问题描述：添加课程时，起始节次和结束节次需要满足"起始 ≤ 结束"的约束，且两个下拉框需要联动更新。
- 原因分析：使用两个独立的 `ExposedDropdownMenuBox`，选择起始节次后如果结束节次小于起始节次需自动调整，反之亦然。
- 解决方案：
  - 使用 `selectedStartPeriod` 和 `selectedEndPeriod` 两个状态变量
  - 起始节次选择时：`if (selectedEndPeriod < index) selectedEndPeriod = index`
  - 结束节次选择时：`if (selectedStartPeriod > index) selectedStartPeriod = index`
  - 下拉菜单项用 `enabled = index <= selectedEndPeriod` 禁用不合规选项

```kotlin
DropdownMenuItem(
    onClick = {
        selectedStartPeriod = index
        if (selectedEndPeriod < index) selectedEndPeriod = index
    },
    enabled = index <= selectedEndPeriod
)
```

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
- 特殊权限：网络权限（`INTERNET`），用于获取网络励志格言
- 运行步骤：
  1. 克隆仓库：`git clone https://github.com/chenbangguo/MobileSoftwareDevelopment`
  2. 使用 Android Studio 打开项目
  3. 等待 Gradle 同步完成
  4. 连接模拟器或真机，点击 Run

## 11. 项目亮点（可选）

1. **完整的课程表网格布局**：使用 Compose 自定义实现 7×6 课表表格，支持课程跨节次合并显示，视觉效果接近真实大学课表。
2. **课程表单交互优化**：起始/结束节次下拉框联动约束，自动保证逻辑正确性；15种预设颜色快速选择；FilterChip 组件实现星期和周类型单选。
3. **多角色权限管理**：支持教师/学生角色切换，不同角色拥有不同功能权限。教师端可发布公告和作业，学生端可查看和提交。
4. **响应式数据更新**：课程增删改后，通过 Room Flow 自动刷新 UI，无需手动刷新页面，用户体验流畅。
5. **单双周课程支持**：`weekType` 字段支持每周/单周/双周三种类型，为未来扩展按教学周筛选功能预留接口。
6. **完善的错误处理**：删除前确认对话框、课程名称必填校验、空状态友好提示，覆盖各类边界情况。
7. **Canvas 自定义进度环**：番茄钟圆形进度条使用 Compose Canvas 绘制，支持弧形进度动画。

## 12. 未来改进方向（可选）

1. 按教学周筛选：在课程表顶部增加周次选择器，根据 weekType 动态过滤单周/双周课程。
2. 课程冲突检测：添加/编辑课程时自动检测同一时间是否已有课程。
3. 课程导入导出：支持从教务系统导入课表（JSON/CSV格式），或导出分享。
4. 多课表支持：允许创建多个学期课表（如秋季/春季学期），方便切换。
5. 课程提醒：在课程开始前推送通知提醒。
6. Widget 桌面小组件：快速查看今日课程安排。
