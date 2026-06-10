# Lab2 实验报告

## 一、名片展示信息

本实验实现了一个基于 **Jetpack Compose** 的个人电子名片界面，主要用于展示用户的基本信息与联系方式。界面整体分为上下两个区域，内容如下：

### 1. 基本信息（上半部分）

- **头像 / Logo**：使用 `Image` 组件加载本地资源（Android Logo）
- **姓名**：Zirong Guo
- **职位**：Android Developer Extraordinaire

### 2. 联系方式（下半部分）

每一项联系方式由一个图标和对应文本组成：

- 📞 电话：+86 190 6928 0669
- 🔗 社交账号：@yxnu.edu.cn
- ✉️ 邮箱：g184888000@gmali.com

界面整体采用深绿色背景（`#073042`），辅以 Android 绿色（`#3DDC84`）作为强调色，使界面风格统一、简洁。

------

## 二、布局结构说明

本实验完全基于声明式 UI 框架 **Jetpack Compose** 实现，核心是使用 `@Composable` 函数构建界面。

### 1. 整体布局结构

界面采用一个顶层 `Column` 作为容器，实现上下结构划分：

```
Column（整屏）
├── CardTop（上半部分）
│   ├── Image（头像）
│   ├── Text（姓名）
│   └── Text（职位）
└── CardBottom（下半部分）
    ├── ContactRow（电话）
    ├── ContactRow（社交）
    └── ContactRow（邮箱）
```

通过设置：

- `verticalArrangement = Arrangement.SpaceBetween`
- `horizontalAlignment = Alignment.CenterHorizontally`

实现上下区域分离与水平居中对齐。

------

### 2. 上半部分布局（CardTop）

使用 `Column` 垂直排列元素：

- 图片（居中）
- 姓名（较大字体）
- 职位（强调色 + 粗体）

关键技术点：

- 使用 `Modifier.size()` 控制图片大小
- 使用 `Spacer` 控制元素间距
- 使用 `FontWeight.Bold` 突出职位信息

------

### 3. 下半部分布局（CardBottom）

使用 `Column` 包裹多个 `ContactRow`，每一行表示一条联系方式。

每一行结构如下：

```
Row
├── Icon（图标）
├── Spacer（间距）
└── Text（信息）
```

关键点：

- `Row` 实现横向排列
- `Icon` 使用 Material Icons（如 Phone、Email）
- `Spacer` 控制图标与文字间距
- 使用 `Divider` 分隔各行，提高可读性

------

### 4. Modifier 的使用

本实验中大量使用了 `Modifier` 进行布局与样式控制：

| 方法              | 作用         |
| ----------------- | ------------ |
| `.fillMaxSize()`  | 填充整个屏幕 |
| `.fillMaxWidth()` | 横向填满     |
| `.padding()`      | 控制内边距   |
| `.size()`         | 控制组件大小 |
| `.background()`   | 设置背景颜色 |

------

## 三、遇到的问题与解决

### 问题 1：图标无法显示（Icons 报错）

**原因：**
未引入 Material Icons 扩展库。

**解决方法：**
在 `build.gradle` 中添加依赖：

```kotlin
implementation("androidx.compose.material:material-icons-extended")
```

------

### 问题 2：图片资源无法加载

**原因：**
图片未放入 `res/drawable` 目录或资源名错误。

**解决方法：**

- 将图片放入 `res/drawable/`
- 使用 `painterResource(id = R.drawable.xxx)` 正确引用

------

### 问题 3：布局未居中或间距异常

**原因：**
未正确设置对齐方式或缺少 `Spacer`。

**解决方法：**

- 使用 `horizontalAlignment = Alignment.CenterHorizontally`
- 使用 `Spacer` 控制间距，而不是滥用 padding

------

### 问题 4：界面上下分布不合理

**原因：**
未使用 `Arrangement.SpaceBetween`。

**解决方法：**
在外层 `Column` 中设置：

```kotlin
verticalArrangement = Arrangement.SpaceBetween
```
