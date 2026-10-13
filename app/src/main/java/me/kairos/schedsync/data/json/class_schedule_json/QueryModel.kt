package me.kairos.schedsync.data.json.class_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 分页查询模型。
 *
 * @property currentPage 当前页码
 * @property currentResult 当前记录结果偏移
 * @property entityOrField 实体或字段标识
 * @property limit 限制数量
 * @property offset 偏移量
 * @property pageNo 页号
 * @property pageSize 单页记录数
 * @property showCount 展示计数
 * @property sorts 排序列表
 * @property totalCount 总记录条数
 * @property totalPage 总页数
 * @property totalResult 总结果数
 */
data class QueryModel(
    @SerializedName("currentPage") val currentPage: Int? = 0,
    @SerializedName("currentResult") val currentResult: Int? = 0,
    @SerializedName("entityOrField") val entityOrField: Boolean? = false,
    @SerializedName("limit") val limit: Int? = 0,
    @SerializedName("offset") val offset: Int? = 0,
    @SerializedName("pageNo") val pageNo: Int? = 0,
    @SerializedName("pageSize") val pageSize: Int? = 0,
    @SerializedName("showCount") val showCount: Int? = 0,
    @SerializedName("sorts") val sorts: List<Any>? = emptyList(),
    @SerializedName("totalCount") val totalCount: Int? = 0,
    @SerializedName("totalPage") val totalPage: Int? = 0,
    @SerializedName("totalResult") val totalResult: Int? = 0
)
