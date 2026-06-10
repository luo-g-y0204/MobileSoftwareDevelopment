# Lab2 实验报告
 ## 一、名片展示信息
 1.  头像：个人头像图片（`R.drawable.pictur`）
 2.  姓名：Lang Meng Ting
 3.  职位：Music Lover
 4.  联系方式：
     - 电话：+11 (13) 888 888 666
     - 社交账号：@Peace.
     - 邮箱：3886140801@qq.com
 ## 二、布局结构说明
 - **根容器**：`Column` 垂直排列，整体居中对齐
 - **上半部分**：封装为 `CardTop` 可组合函数，内部嵌套 `Column`，依次排列 `Image`（头像）、`Text`（姓名）、`Text`（职位）
 - **下半部分**：封装为 `CardBottom` 可组合函数，内部嵌套 `Column`，重复调用 `ContactRow` 组件
 - **联系方式条目**：封装为 `ContactRow` 可组合函数，内部使用 `Row` 水平排列 `Icon`（Material 图标）和 `Text`（联系信息），通过 `Spacer` 控制间距
 ## 三、遇到的问题与解决
 1.  **问题**：编译报错 `Unresolved reference 'Alignment'`
     - **解决**：添加导入语句 `import androidx.compose.ui.Alignment`
 2.  **问题**：图标与文字间距不美观
     - **解决**：使用 `Spacer` 组件显式设置 16.dp 宽度，替代直接 `padding`，布局更清晰
 3.  **问题**：图标颜色与整体风格不统一
     - **解决**：通过 `Icon` 的 `tint` 参数设置为 `Color(0xFF388E3C)`，与职位文字颜色保持一致