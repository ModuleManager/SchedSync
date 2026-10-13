package me.kairos.schedsync.data.source

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.kairos.schedsync.api.LoginResult
import me.kairos.schedsync.api.ZengFangAPI
import me.kairos.schedsync.data.json.class_json.ClassInfo
import me.kairos.schedsync.data.json.class_schedule_json.ScheduleResponse
import me.kairos.schedsync.data.json.person_schedule_json.PersonScheduleResponse

/**
 * 远程数据源。
 *
 * 封装对 [ZengFangAPI] 的网络调用，负责将网络返回的原始 JSON 数据反序列化为数据模型对象。
 */
class ScheduleRemoteDataSource {
    private val gson = Gson()

    /**
     * 发起教务系统登录请求。
     *
     * @param sid 用户学号
     * @param password 明文密码
     * @param captcha 验证码文本（可选）
     * @return [LoginResult] 登录结果状态
     */
    suspend fun login(
        sid: String,
        password: String,
        captcha: String? = null
    ): LoginResult {
        return ZengFangAPI.login(sid, password, captcha)
    }

    /**
     * 刷新图形验证码图片数据。
     *
     * @return 验证码图片的字节数组，失败时返回 null
     */
    suspend fun refreshCaptcha(): ByteArray? {
        return ZengFangAPI.refreshCaptcha()
    }

    /**
     * 从远程接口拉取班级课表数据。
     *
     * @param year 学年
     * @param term 学期
     * @param selectClass 绑定的班级信息
     * @return 解析后的 [ScheduleResponse] 班级课表对象，请求失败返回 null
     */
    suspend fun fetchClassSchedule(
        year: Int,
        term: Int,
        selectClass: ClassInfo
    ): ScheduleResponse? {
        val rawJson = ZengFangAPI.getSchedule(year, term, selectClass) ?: return null
        return runCatching {
            gson.fromJson(rawJson, ScheduleResponse::class.java)
        }.getOrNull()
    }

    /**
     * 从远程接口拉取个人课表数据。
     *
     * @param year 学年
     * @param term 学期
     * @return 解析后的 [PersonScheduleResponse] 个人课表对象，请求失败返回 null
     */
    suspend fun fetchPersonalSchedule(
        year: Int,
        term: Int
    ): PersonScheduleResponse? {
        val rawJson = ZengFangAPI.getPersonalSchedule(year, term) ?: return null
        return runCatching {
            gson.fromJson(rawJson, PersonScheduleResponse::class.java)
        }.getOrNull()
    }

    /**
     * 拉取指定年级的可选班级列表。
     *
     * @param year 年级/学年
     * @return [ClassInfo] 班级列表，解析失败时返回空列表
     */
    suspend fun fetchClassInformation(year: Int): List<ClassInfo> {
        val jsonResult = ZengFangAPI.getInformation(year) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ClassInfo>>() {}.type
            gson.fromJson<List<ClassInfo>>(jsonResult, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /**
     * 发起登出并注销教务系统网络会话。
     */
    suspend fun logout() {
        ZengFangAPI.logout()
    }
}
