package me.kairos.schedsync.data.json.class_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 班级课表接口 JSON 响应模型。
 *
 * @property qsxqj 起始星期几代码
 * @property sjkList 实践课列表
 * @property xqjmcMap 星期名称映射字典
 * @property kbList 课程项列表
 */
data class ScheduleResponse(
    @SerializedName("qsxqj") val qsxqj: String? = null,
    @SerializedName("sjkList") val sjkList: List<Any>? = emptyList(),
    @SerializedName("xqjmcMap") val xqjmcMap: Map<String, String>? = emptyMap(),
    @SerializedName("kbList") val kbList: List<CourseItem>? = emptyList()
)
