package com.example.myapplicationlab10.bookshelf.network

/**
 * 接口基础配置文件
 * 用于统一管理网络请求的基础地址
 * 避免硬编码，便于维护和修改
 * 本实验使用固定Apifox Mock接口
 * 地址为：https://m1.apifoxmock.com/m1/8321477-8085280-default/
 * 所有网络请求均以此地址为根路径
 */
const val BASE_URL = "https://m1.apifoxmock.com/m1/8321477-8085280-default/"