package me.kairos.schedsync.data.json.person_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 个人课表 JSON 响应模型。
 *
 * @property studentInfo 学生基础信息
 * @property kbList 个人课程项列表
 * @property xqjmcMap 星期映射字典
 */
data class PersonScheduleResponse(
    @SerializedName("xsxx") val studentInfo: StudentInfo? = null,
    @SerializedName("kbList") val kbList: List<PersonCourseItem>? = emptyList(),
    @SerializedName("xqjmcMap") val xqjmcMap: Map<String, String>? = emptyMap()
)
