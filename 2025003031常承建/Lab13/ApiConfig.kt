package com.example.bookshelf.network

/**
 * 网络层全局配置
 * 集中管理实验用到的所有网络相关常量
 * 符合单一职责原则，便于统一修改
 * @author 你的姓名
 * @date 2026-06-03
 */
object ApiConfig {
    /** Apifox Mock服务器基础地址（实验指定） */
    const val BASE_URL = "https://m1.apifoxmock.com/m1/8321477-8085280-default/"

    /** Retrofit网络请求超时时间（秒） */
    const val CONNECT_TIMEOUT = 10L
    const val READ_TIMEOUT = 10L

    /** Coil图片加载默认配置 */
    const val IMAGE_CROSSFADE_DURATION = 200 // 淡入淡出动画时长
    const val IMAGE_SCALE_TYPE = "centerCrop" // 图片缩放类型
}