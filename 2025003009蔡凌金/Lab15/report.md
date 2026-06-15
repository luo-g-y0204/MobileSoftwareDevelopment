# 航班搜索应用实验报告
## 一、实验目的
1. 掌握Room数据库三层架构（Entity、Dao、Database）的本地数据持久化实现。
2. 熟悉Jetpack Compose声明式UI开发，实现响应式界面交互。
3. 理解MVVM架构下ViewModel+Flow的状态管理模式，完成UI与业务逻辑解耦。
4. 掌握DataStore偏好存储，实现搜索文本持久化与状态恢复。
5. 实现机场搜索、航班展示、航线收藏等核心业务逻辑。

## 二、实验环境
- 开发工具：Android Studio Hedgehog | 2023.1.1
- 编程语言：Kotlin
- 核心框架：Jetpack Compose、Room、ViewModel、Coroutines、Flow
- 最低SDK版本：Android 8.0 (API 26)

## 三、系统架构设计
采用MVVM架构，数据单向流转：
```
UI层(Compose) → ViewModel层 → 数据层(Room/DataStore)
```
- **数据层**：封装Room数据库（Airport/Favorite实体、FlightDao接口）与DataStore偏好存储，负责数据存储、查询、修改。
- **ViewModel层**：通过Flow管理数据流，封装业务逻辑，向UI层暴露统一的UiState。
- **UI层**：基于Compose实现界面渲染，响应状态变化并处理用户交互。

## 四、核心功能实现
### 4.1 数据库设计
1. **实体类**：
   - Airport：存储机场信息（id、name、iataCode、passengers）。
   - Favorite：存储收藏航线（id、departureCode、destinationCode）。
2. **Dao核心能力**：
   - 机场模糊搜索（IATA码/名称，按客流量排序+LIMIT优化）。
   - CROSS JOIN动态生成出发机场的航班列表（排除自身）。
   - 多表关联查询收藏航线，补充机场名称。
   - 收藏状态判断、添加/取消收藏操作。

### 4.2 状态管理（ViewModel）
1. **核心数据流**：
   - 搜索文本防抖处理（250ms），避免频繁数据库查询。
   - 合并搜索文本、选中机场、收藏状态等，生成UiState。
   - 启动时从DataStore恢复搜索文本，完整IATA码自动加载航班。
2. **业务逻辑**：
   - 航班列表关联收藏状态，实时更新。
   - 收藏/取消收藏、清空搜索等操作的协程安全处理。

### 4.3 UI界面实现（Compose）
1. **状态切换**：Loading/收藏列表/机场建议/航班列表四态平滑切换（Crossfade动画）。
2. **核心组件**：
   - 搜索框：大写输入、清空、占位提示，绑定搜索文本状态。
   - 机场建议卡片：展示机场信息，点击切换至航班列表。
   - 航班/收藏卡片：展示航线信息，支持收藏/取消收藏操作。

### 4.4 偏好存储（DataStore）
- 存储键`search_text`，持久化用户最后一次搜索文本。
- 以Flow暴露搜索文本，协程中安全写入，支持状态恢复。

## 五、关键技术亮点
1. Flow操作符组合（debounce/combine/flatMapLatest），实现数据流高效联动。
2. Room查询优化（LIMIT、COLLATE NOCASE、CROSS JOIN），减少性能损耗。
3. Compose状态驱动UI，动画切换提升交互体验。
4. WhileSubscribed(5000)实现数据流自动回收，优化性能。

## 六、实验总结
### 6.1 功能完成度
✅ 机场模糊搜索 ✅ 航班列表展示 ✅ 航线收藏/取消收藏 ✅ 搜索文本持久化 ✅ 多状态UI切换

### 6.2 待优化方向
1. 增加航班筛选、收藏航线拖拽排序功能。
2. 对接真实航班API替换模拟数据。
3. 优化数据库初始化速度，支持增量加载。

### 6.3 技术收获
- 掌握Flow响应式编程与Room数据库优化。
- 熟练使用Compose实现状态驱动的UI开发。
- 理解MVVM架构下各层职责与数据流转逻辑。