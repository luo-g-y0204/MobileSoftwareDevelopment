# Lab2 实验报告

## 一、名片展示的个人信息
- 姓名：柳航
- 职位：计算机专业学生
- 电话：+86 138-0000-0000
- 邮箱：starry0616@gmail.com
- 社交账号：@starry0616

## 二、布局结构简要说明
1. 整体使用 `Column` 纵向布局，将界面分为上下两个区域。

2. 上半部分 `CardTop`：使用 `Column` 嵌套 `Image`（头像）、`Text`（姓名）、`Text`（职位），通过 `horizontalAlignment = Alignment.CenterHorizontally` 实现水平居中展示。

3. 下半部分 `CardBottom`：使用 `Column` 嵌套多个 `ContactRow`，每个 `ContactRow` 是 `Row` 横向布局，包含 `Icon`（电话/邮箱/分享图标）和 `Text`（对应联系方式文字），并使用 `HorizontalDivider` 绘制分隔线。

4. 通过 `Modifier` 控制组件的大小、内边距、背景颜色和填充方式，实现美观的界面排版。

## 三、遇到的问题和解决过程
1. **问题**：头像图片无法在代码中引用，运行后显示空白。
   **解决**：将图片重命名为 `logo.png`，放入 `res/drawable` 文件夹，代码中使用 `R.drawable.logo` 成功引用并显示。

2. **问题**：界面内容默认靠左对齐，没有水平居中。
   **解决**：在 `Column` 布局中添加 `horizontalAlignment = Alignment.CenterHorizontally` 参数，使所有子组件水平居中。

3. **问题**：默认图标颜色与蓝色背景搭配不协调，视觉效果差。
   **解决**：通过 `Icon` 组件的 `tint` 参数，将图标统一设置为绿色，与背景形成清晰对比，提升整体美观度。s