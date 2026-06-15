# FlightSearch 航班搜索项目说明文档

## 项目概述
FlightSearch 是一款基于 Jetpack Compose 开发的 Android 本地航班查询应用，采用 MVVM 架构模式，实现了机场搜索、航线浏览、收藏管理等核心功能，所有数据均在本地持久化存储。

## 核心功能
- 机场模糊搜索：支持输入 IATA 代码或机场名称进行匹配，结果按旅客吞吐量降序排列
- 目的地航线展示：选中出发机场后，自动加载所有可选目的地机场列表
- 收藏航线管理：支持一键添加/取消收藏航线，收藏数据本地持久化
- 搜索状态恢复：应用重启后自动还原上次输入的搜索内容，无需重复输入

## 技术栈
- UI 框架：Jetpack Compose + Material3 设计规范
- 架构模式：MVVM + 单向数据流
- 本地数据库：原生 SQLiteOpenHelper 实现预打包数据库读取
- 轻量存储：DataStore Preferences 存储用户搜索偏好
- 异步方案：Kotlin Coroutines + StateFlow 实现响应式状态管理

## 数据库设计
1. airport 表：存储所有机场基础信息，包含 id、iata_code、name、passengers 字段
2. favorite 表：存储用户收藏的航线，包含 id、departure_code、destination_code 字段，设置联合唯一约束

## 运行环境要求
- 最低支持系统：Android 8.0（API Level 26）
- 编译环境：Android Studio 新版本 + Kotlin 2.0+
- 编译 SDK：Android API 36