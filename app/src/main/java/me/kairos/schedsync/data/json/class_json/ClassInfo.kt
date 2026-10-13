package me.kairos.schedsync.data.json.class_json

import com.google.gson.annotations.SerializedName

/**
 * 班级信息模型。
 *
 * 映射正方教务系统返回的班级代码与专业描述数据。
 *
 * @property bhId 班级 ID
 * @property bh 班级编号
 * @property bj 班级名称
 * @property zymc 专业名称
 * @property jgmc 学院机构名称
 * @property zyhId 专业号 ID
 */
data class ClassInfo(
    @SerializedName("bh_id") val bhId: String,
    @SerializedName("bh") val bh: String,
    @SerializedName("bj") val bj: String,
    @SerializedName("zymc") val zymc: String,
    @SerializedName("jgmc") val jgmc: String,
    @SerializedName("zyh_id") val zyhId: String
)
