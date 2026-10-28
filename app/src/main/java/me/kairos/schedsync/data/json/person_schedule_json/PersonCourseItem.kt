package me.kairos.schedsync.data.json.person_schedule_json

import com.google.gson.annotations.SerializedName
import me.kairos.schedsync.data.json.class_schedule_json.CourseItem

/**
 * 个人课表单项课程响应模型。
 *
 * @property kcmc 课程名称
 * @property xm 教师姓名
 * @property cdmc 场地/教室名称
 * @property zcd 周次描述
 * @property xqjmc 星期几名称
 * @property jc 节次
 * @property jcs 节数
 * @property kclb 课程类别
 * @property kcxz 课程性质
 * @property xf 学分
 * @property xnm 学年码
 * @property xqm 学期码
 * @property xqj 星期几数字
 * @property date 日期
 */
data class PersonCourseItem(
    @SerializedName("kcmc") val kcmc: String? = null,
    @SerializedName("xm") val xm: String? = null,
    @SerializedName("cdmc") val cdmc: String? = null,
    @SerializedName("zcd") val zcd: String? = null,
    @SerializedName("xqjmc") val xqjmc: String? = null,
    @SerializedName("jc") val jc: String? = null,
    @SerializedName("jcs") val jcs: String? = null,
    @SerializedName("kclb") val kclb: String? = null,
    @SerializedName("kcxz") val kcxz: String? = null,
    @SerializedName("xf") val xf: String? = null,
    @SerializedName("xnm") val xnm: String? = null,
    @SerializedName("xqm") val xqm: String? = null,
    @SerializedName("xqj") val xqj: String? = null,
    @SerializedName("dateDigitSeparator") val date: String? = null
) {
    /**
     * 将个人课表数据模型转换映射为通用的 [CourseItem] 数据模型。
     *
     * @return 转换后的 [CourseItem] 对象
     */
    fun toCourseItem(): CourseItem {
        val cleanJc = jc?.replace(Regex("[^0-9-]"), "") ?: ""
        return CourseItem(
            kcmc = kcmc,
            xm = xm,
            cdmc = cdmc,
            zcd = zcd,
            xqjmc = xqjmc,
            jc = cleanJc,
            jcor = cleanJc,
            xqj = xqj,
            xf = xf,
            xnm = xnm,
            xqm = xqm,
            dateDigitSeparator = date
        )
    }
}
