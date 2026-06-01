package com.example.myapplicationlab10.bookshelf.model

/**
 * 书籍领域模型
 * 应用内部使用的数据实体
 * 用于UI展示和业务逻辑处理
 * 隔离网络层DTO，降低耦合
 * 包含id、封面地址、标题三个核心字段
 */
data class Book(
    val id: String,
    val coverUrl: String,
    val title: String
)