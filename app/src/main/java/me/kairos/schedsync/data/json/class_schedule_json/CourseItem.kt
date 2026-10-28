package me.kairos.schedsync.data.json.class_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 正方教务系统班级课表单项课程数据模型。
 *
 * 映射 JSON 响应中的课程属性字段。
 *
 * @property cdId 场地 ID
 * @property cdlbmc 场地类别名称
 * @property cdmc 场地名称（上课教室）
 * @property date 上课日期
 * @property dateDigit 日期数字格式
 * @property dateDigitSeparator 日期分隔符
 * @property day 星期几
 * @property jc 节次表达式
 * @property jcor 原始节次
 * @property jcs 节数
 * @property jghId 教师教工号 ID
 * @property jgpxzd 机构排序字段
 * @property jxbId 教学班 ID
 * @property jxbmc 教学班名称
 * @property jxbrs 教学班人数
 * @property jxbzc 教学班组成
 * @property kch 课程号
 * @property kcmc 课程名称
 * @property kcxszc 课程学时组成
 * @property kcxzjc 课程性质简称
 * @property kczxs 课程总学时
 * @property khfsmc 考核方式名称
 * @property kkzt 开课状态
 * @property ksfsmc 考试方式名称
 * @property listnav 导航标识
 * @property localeKey 语言键
 * @property month 月份
 * @property njxh 年级序号
 * @property oldjc 原节次
 * @property oldzc 原周次
 * @property pageTotal 总页数
 * @property pageable 是否可分页
 * @property pkbj 排课标记
 * @property px 排序
 * @property queryModel 查询模型
 * @property rangeable 是否可范围选择
 * @property rsdzjs 人数递增计数
 * @property sfkckkb 是否可查看课表
 * @property skfsmc 授课方式名称
 * @property totalResult 总结果数
 * @property userModel 用户模型
 * @property xf 学分
 * @property xkbz 选课备注
 * @property xkrs 选课人数
 * @property xm 教师姓名
 * @property xnm 学年码
 * @property xqdm 校区代码
 * @property xqh1 校区号1
 * @property xqhId 校区号 ID
 * @property xqj 星期几数字（1~7）
 * @property xqjmc 星期几名称
 * @property xqm 学期码
 * @property xqmc 校区名称
 * @property xsdj 学时等级
 * @property xslxbj 学生类型标记
 * @property year 年份
 * @property zcd 周次描述（例如 "1-16周"）
 * @property zcds 周次段数
 * @property zcmc 周次名称
 * @property zfjmc 主辅进名称
 * @property zhxs 周学时
 * @property zxs 总学时
 * @property zxxx 选修信息
 * @property zyfxId 专业方向 ID
 * @property zyfxmc 专业方向名称
 * @property zyhxkcbj 专业核心课程标记
 * @property zzmm 政治面貌
 * @property zzrl 限制容量
 */
data class CourseItem(
    @SerializedName("cd_id") val cdId: String? = null,
    @SerializedName("cdlbmc") val cdlbmc: String? = null,
    @SerializedName("cdmc") val cdmc: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("dateDigit") val dateDigit: String? = null,
    @SerializedName("dateDigitSeparator") val dateDigitSeparator: String? = null,
    @SerializedName("day") val day: String? = null,
    @SerializedName("jc") val jc: String? = null,
    @SerializedName("jcor") val jcor: String? = null,
    @SerializedName("jcs") val jcs: String? = null,
    @SerializedName("jgh_id") val jghId: String? = null,
    @SerializedName("jgpxzd") val jgpxzd: String? = null,
    @SerializedName("jxb_id") val jxbId: String? = null,
    @SerializedName("jxbmc") val jxbmc: String? = null,
    @SerializedName("jxbrs") val jxbrs: String? = null,
    @SerializedName("jxbzc") val jxbzc: String? = null,
    @SerializedName("kch") val kch: String? = null,
    @SerializedName("kcmc") val kcmc: String? = null,
    @SerializedName("kcxszc") val kcxszc: String? = null,
    @SerializedName("kcxzjc") val kcxzjc: String? = null,
    @SerializedName("kczxs") val kczxs: String? = null,
    @SerializedName("khfsmc") val khfsmc: String? = null,
    @SerializedName("kkzt") val kkzt: String? = null,
    @SerializedName("ksfsmc") val ksfsmc: String? = null,
    @SerializedName("listnav") val listnav: String? = null,
    @SerializedName("localeKey") val localeKey: String? = null,
    @SerializedName("month") val month: String? = null,
    @SerializedName("njxh") val njxh: Int? = 0,
    @SerializedName("oldjc") val oldjc: String? = null,
    @SerializedName("oldzc") val oldzc: String? = null,
    @SerializedName("pageTotal") val pageTotal: Int? = 0,
    @SerializedName("pageable") val pageable: Boolean? = false,
    @SerializedName("pkbj") val pkbj: String? = null,
    @SerializedName("px") val px: String? = null,
    @SerializedName("queryModel") val queryModel: QueryModel? = null,
    @SerializedName("rangeable") val rangeable: Boolean? = false,
    @SerializedName("rsdzjs") val rsdzjs: Int? = 0,
    @SerializedName("sfkckkb") val sfkckkb: Boolean? = false,
    @SerializedName("skfsmc") val skfsmc: String? = null,
    @SerializedName("totalResult") val totalResult: String? = null,
    @SerializedName("userModel") val userModel: UserModel? = null,
    @SerializedName("xf") val xf: String? = null,
    @SerializedName("xkbz") val xkbz: String? = null,
    @SerializedName("xkrs") val xkrs: String? = null,
    @SerializedName("xm") val xm: String? = null,
    @SerializedName("xnm") val xnm: String? = null,
    @SerializedName("xqdm") val xqdm: String? = null,
    @SerializedName("xqh1") val xqh1: String? = null,
    @SerializedName("xqh_id") val xqhId: String? = null,
    @SerializedName("xqj") val xqj: String? = null,
    @SerializedName("xqjmc") val xqjmc: String? = null,
    @SerializedName("xqm") val xqm: String? = null,
    @SerializedName("xqmc") val xqmc: String? = null,
    @SerializedName("xsdj") val xsdj: String? = null,
    @SerializedName("xslxbj") val xslxbj: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("zcd") val zcd: String? = null,
    @SerializedName("zcds") val zcds: String? = null,
    @SerializedName("zcmc") val zcmc: String? = null,
    @SerializedName("zfjmc") val zfjmc: String? = null,
    @SerializedName("zhxs") val zhxs: String? = null,
    @SerializedName("zxs") val zxs: String? = null,
    @SerializedName("zxxx") val zxxx: String? = null,
    @SerializedName("zyfx_id") val zyfxId: String? = null,
    @SerializedName("zyfxmc") val zyfxmc: String? = null,
    @SerializedName("zyhxkcbj") val zyhxkcbj: String? = null,
    @SerializedName("zzmm") val zzmm: String? = null,
    @SerializedName("zzrl") val zzrl: String? = null
)
