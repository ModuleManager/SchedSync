package me.kairos.schedsync.data.json.person_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 学生基础信息模型。
 *
 * @property bjmc 班级名称
 * @property xnmc 学年名称
 * @property xh 学号
 * @property xm 姓名
 * @property zymc 专业名称
 * @property njdmId 年级代码 ID
 */
data class StudentInfo(
    @SerializedName("BJMC") val bjmc: String? = null,
    @SerializedName("XNMC") val xnmc: String? = null,
    @SerializedName("XH") val xh: String? = null,
    @SerializedName("XM") val xm: String? = null,
    @SerializedName("ZYMC") val zymc: String? = null,
    @SerializedName("NJDM_ID") val njdmId: String? = null
)
