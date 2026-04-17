# Lab5 ArtSpace 实验报告
## 1. 应用内容
主题：世界经典名画
展示作品数量：3 幅
- 作品1：星空 - 梵高
- 作品2：蒙娜丽莎 - 达芬奇
- 作品3：呐喊 - 蒙克

## 2. 界面结构
界面分为 3 个区块：
1. 作品展示区：使用 Image + Surface 实现画框效果
2. 作品信息区：使用 Column 包裹 3 个 Text
3. 控制按钮区：使用 Row 包裹 Previous、Next 按钮

整体使用 Column 垂直布局。

## 3. 状态管理
使用 remember + mutableStateOf 保存当前作品索引：
var currentArtwork by remember { mutableStateOf(1) }
图片、标题、作者、年份全部根据这个状态动态切换。

## 4. 按钮逻辑
- Next：1→2→3→1 循环
- Previous：3→2→1→3 循环
使用 when 表达式实现边界判断。

## 5. 遇到的问题与解决
1. 图片不显示：检查 drawable 文件名是否正确
2. 按钮没反应：确认 onClick 里更新了 currentArtwork
3. 布局错乱：添加 Modifier.padding 与 Arrangement 调整