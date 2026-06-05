package com.example.myapplicationlab10.bookshelf.model

import com.google.gson.annotations.SerializedName

/**
 * 网络数据传输对象
 * 对应Apifox Mock接口返回的JSON结构
 * 字段与接口严格对应
 * id：书籍唯一标识
 * img_src：封面图片地址，使用SerializedName映射
 */
data class BookDto(
    val id: String = "",
    @SerializedName("img_src")
    val imgSrc: String = ""
)

/**
 * DTO转领域模型扩展函数
 * 将网络数据转换为UI可用实体
 * 自动生成标题，格式为Book+编号
 */
fun BookDto.asDomainModel(): Book {
    return Book(
        id = this.id,
        coverUrl = this.imgSrc,
        title = "Book #${this.id}"
    )
}