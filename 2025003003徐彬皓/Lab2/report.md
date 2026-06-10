# Lab2 名片应用开发报告
## 一、名片展示的个人信息
本次实现的电子名片包含以下信息：
- 基础信息：姓名、职位
- 联系方式：电话、邮箱、社交账号

## 二、布局思路
整体采用 `Column` 作为根容器，通过 `Arrangement.SpaceBetween` 实现上下两部分的空间分布，核心布局逻辑如下：
Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) 


## 三、布局结构说明
本次名片应用基于 Jetpack Compose 实现，核心通过**可组合函数嵌套**和`Column`/`Row`线性布局容器搭建界面，整体遵循**主布局-分区域-子组件**的分层设计思路，使用的核心可组合函数及嵌套关系如下：
1. **根布局**：`BusinessCard`为核心根可组合函数，作为整个名片的容器，内部通过`Column`纵向排列**上半部分**和**下半部分**，并统一设置页面背景色、内边距和元素对齐/排列方式。
2. **区域布局**：
   - `CardTop`：名片上半部分专属可组合函数，内部嵌套`Column`，纵向排列`Image`（头像）、`Text`（姓名/职位）和`Spacer`（间距），实现头像+个人信息的居中展示。
   - `CardBottom`：名片下半部分专属可组合函数，内部嵌套`Column`，纵向排列`Divider`（分隔线）和多个`ContactRow`，实现联系方式的分行展示，且行与行之间用分隔线分隔。
3. **子组件**：`ContactRow`为最小粒度可组合函数，内部嵌套`Row`，横向排列`Icon`（功能图标）、`Spacer`（间距）和`Text`（联系信息），封装了单行联系方式的通用样式，实现代码复用。

**核心布局嵌套关系**：`BusinessCard` → `Column` → 「`CardTop`/`CardBottom`」→ 「`Column`/`Row`」→ 「`Image`/`Text`/`Icon`/`Divider`/`Spacer`」

## 四、遇到的问题与解决过程
### 问题1：头像展示为方形，无法实现圆形裁剪效果
**问题描述**：使用`Image`组件加载头像图片后，默认以方形展示，不符合名片的视觉设计要求。
**解决过程**：通过`Modifier`修饰符的`.clip(CircleShape)`方法为`Image`添加圆形裁剪效果，同时配合`.contentScale = ContentScale.Crop`属性，保证图片在圆形容器内居中裁剪，避免拉伸变形。

### 问题2：联系方式行内图标与文字间距混乱，整体对齐不美观
**问题描述**：`ContactRow`中图标和文字直接横向排列，无固定间距，且不同行的元素上下对齐不一致，视觉杂乱。
**解决过程**：
1. 在图标和文字之间添加`Spacer(modifier = Modifier.width(16.dp))`，设置固定水平间距，保证所有行的间距统一；
2. 为`Row`添加`verticalAlignment = Alignment.CenterVertically`属性，让图标和文字在垂直方向居中对齐，提升整体规整度。

### 问题3：自定义颜色在多个可组合函数中重复定义，代码冗余
**问题描述**：初期在`CardTop`、`CardBottom`、`ContactRow`中分别定义主色、背景色、文字色，重复代码多，且后期修改颜色需多处调整。
**解决过程**：在根布局`BusinessCard`中统一定义所有自定义颜色（`primaryColor`/`bgColor`/`textColor`），并将颜色作为**参数传递**给子可组合函数（`CardTop`/`CardBottom`/`ContactRow`），实现颜色的统一管理和代码解耦。

### 问题4：页面元素的垂直分布不均，上下部分留白不合理
**问题描述**：整体`Column`布局中，上半部分和下半部分的元素挤在一起，顶部和底部留白过多/过少，页面布局失衡。
**解决过程**：
1. 为根布局的`Column`设置`verticalArrangement = Arrangement.SpaceBetween`，让上下两部分在垂直方向上均匀分布，占满整个屏幕；
2. 为`CardTop`添加`top = 60.dp`的内边距，为`CardBottom`添加`bottom = 60.dp`的内边距，手动调整上下部分的留白，保证视觉平衡。

### 问题5：预览函数中无法正常显示自定义背景色，预览效果与真机不一致
**问题描述**：使用`@Preview`注解预览界面时，背景色始终为Material主题默认色，无法显示自定义的深蓝色背景。
**解决过程**：在预览函数`BusinessCardPreview`中，将`BusinessCard`包裹在`MaterialTheme`中，保证预览环境与实际运行的主题环境一致，同时确保根布局的`background`修饰符直接作用于最外层`Column`，而非主题`Surface`。

## 五、实验总结
本次实验通过 Jetpack Compose 完成名片界面开发，熟练掌握 Column/Row 线性布局、自定义可组合函数、Modifier 修饰符的使用，解决了图片裁剪、界面排版、代码复用、预览适配等问题，理解了声明式 UI的布局思路与组件化设计思想