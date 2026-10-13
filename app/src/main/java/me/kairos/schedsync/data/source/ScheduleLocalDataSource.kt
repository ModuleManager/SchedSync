package me.kairos.schedsync.data.source

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kairos.schedsync.data.AccountInfo
import me.kairos.schedsync.data.AccountSettingsData
import me.kairos.schedsync.data.json.class_json.ClassInfo
import me.kairos.schedsync.data.json.class_schedule_json.ScheduleResponse
import me.kairos.schedsync.data.json.person_schedule_json.PersonScheduleResponse
import me.kairos.schedsync.data.type.DisplayMode
import me.kairos.schedsync.data.type.ScheduleType
import org.json.JSONObject
import java.io.File

/**
 * 本地数据存储数据源。
 *
 * 负责管理账号配置、加密凭据、班级课表及个人课表数据的本地持久化与读取。
 * 密码敏感信息通过 [EncryptedSharedPreferences] 存储，通用配置保存在 JSON 文件中。
 *
 * @param context 应用程序上下文
 */
@Suppress("DEPRECATION")
class ScheduleLocalDataSource(private val context: Context) {
    companion object {
        private const val TAG = "ScheduleLocalDataSource"
    }

    private val configFile = File(context.filesDir, "account_config.json")
    private val classScheduleFile = File(context.filesDir, "class_schedule.json")
    private val personScheduleFile = File(context.filesDir, "person_schedule.json")
    private val selectedClassFile = File(context.filesDir, "selected_class.json")

    private val gson = Gson()

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_account_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * 读取账号配置与偏好设置。
     *
     * 从本地 JSON 文件加载学号、学期、界面显示模式和课表类型，并从加密首选项中提取解密后的密码。
     * 若检测到旧版本配置文件中包含明文密码，会自动平滑迁移至加密首选项中并清理明文字段。
     *
     * @return [AccountSettingsData] 加载得到的账号设置数据结构
     */
    suspend fun loadAccountSettings(): AccountSettingsData = withContext(Dispatchers.IO) {
        var user = ""
        var year = "2026"
        var sem = "1"
        var mode = DisplayMode.FOLLOW
        var type = ScheduleType.PERSONAL
        var notificationAllow = false
        var notificationAdvance = 30
        var savedWeek = 1

        if (configFile.exists()) {
            runCatching {
                val jsonString = configFile.readText()
                val jsonObject = JSONObject(jsonString)

                user = jsonObject.optString("userId", "")
                year = jsonObject.optString("academicYear", "2026")
                sem = jsonObject.optString("semester", "1")

                val modeName = jsonObject.optString("displayMode", DisplayMode.FOLLOW.name)
                mode = runCatching { DisplayMode.valueOf(modeName) }.getOrDefault(DisplayMode.FOLLOW)

                notificationAllow = jsonObject.optBoolean("isNotificationAllow", false)
                notificationAdvance = jsonObject.optInt("notificationAdvanceMinutes", 30)

                val typeName = jsonObject.optString("scheduleType", ScheduleType.PERSONAL.name)
                type = runCatching { ScheduleType.valueOf(typeName) }.getOrDefault(ScheduleType.PERSONAL)

                savedWeek = jsonObject.optInt("courseWeek", 1)

                if (jsonObject.has("password")) {
                    val plainPassword = jsonObject.optString("password", "")
                    if (plainPassword.isNotEmpty()) {
                        encryptedPrefs.edit { putString("password", plainPassword) }
                    }
                    jsonObject.remove("password")
                    configFile.writeText(jsonObject.toString())
                }
            }.onFailure { e ->
                Log.e(TAG, "解析账号配置文件失败", e)
            }
        }

        val pass = runCatching {
            encryptedPrefs.getString("password", "") ?: ""
        }.getOrDefault("")

        AccountSettingsData(
            accountInfo = AccountInfo(user, pass, year, sem),
            displayMode = mode,
            scheduleType = type,
            isNotificationAllow = notificationAllow,
            notificationAdvanceMinutes = notificationAdvance,
            courseWeek = savedWeek
        )
    }

    /**
     * 保存账号配置与偏好设置。
     *
     * 密码保存至底层硬件保护的加密首选项中，其余非敏感字段保存至本地配置文件。
     *
     * @param accountInfo 账号基础配置
     * @param displayMode 界面暗亮模式
     * @param scheduleType 课表显示类型
     * @param isNotificationAllow 实时通知开启状态
     * @param notificationAdvanceMinutes 课程提前提醒分钟数
     * @param courseWeek 当前教学周数
     */
    suspend fun saveAccountSettings(
        accountInfo: AccountInfo,
        displayMode: DisplayMode,
        scheduleType: ScheduleType,
        isNotificationAllow: Boolean,
        notificationAdvanceMinutes: Int,
        courseWeek: Int
    ) = withContext(Dispatchers.IO) {
        runCatching {
            encryptedPrefs.edit { putString("password", accountInfo.password) }

            val jsonObject = JSONObject().apply {
                put("userId", accountInfo.userId)
                put("academicYear", accountInfo.academicYear)
                put("semester", accountInfo.semester)
                put("displayMode", displayMode.name)
                put("scheduleType", scheduleType.name)
                put("isNotificationAllow", isNotificationAllow)
                put("notificationAdvanceMinutes", notificationAdvanceMinutes)
                put("courseWeek", courseWeek)
            }
            configFile.writeText(jsonObject.toString())
        }.onFailure { e ->
            Log.e(TAG, "保存账号配置文件失败", e)
        }
    }

    /**
     * 从本地文件加载班级课表。
     *
     * @return [ScheduleResponse] 班级课表响应数据，若无本地文件或解析失败则返回 null
     */
    suspend fun loadClassSchedule(): ScheduleResponse? = withContext(Dispatchers.IO) {
        if (!classScheduleFile.exists()) return@withContext null
        runCatching {
            val jsonString = classScheduleFile.readText()
            gson.fromJson(jsonString, ScheduleResponse::class.java)
        }.getOrNull()
    }

    /**
     * 保存班级课表到本地文件。
     *
     * @param schedule 待保存的班级课表数据
     * @return 保存是否成功
     */
    suspend fun saveClassSchedule(schedule: ScheduleResponse): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            classScheduleFile.writeText(gson.toJson(schedule))
            true
        }.getOrDefault(false)
    }

    /**
     * 从本地文件加载个人课表。
     *
     * @return [PersonScheduleResponse] 个人课表响应数据，若无本地文件或解析失败则返回 null
     */
    suspend fun loadPersonSchedule(): PersonScheduleResponse? = withContext(Dispatchers.IO) {
        if (!personScheduleFile.exists()) return@withContext null
        runCatching {
            val jsonString = personScheduleFile.readText()
            gson.fromJson(jsonString, PersonScheduleResponse::class.java)
        }.getOrNull()
    }

    /**
     * 保存个人课表到本地文件。
     *
     * @param schedule 待保存的个人课表数据
     * @return 保存是否成功
     */
    suspend fun savePersonSchedule(schedule: PersonScheduleResponse): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            personScheduleFile.writeText(gson.toJson(schedule))
            true
        }.getOrDefault(false)
    }

    /**
     * 从本地文件加载选中的班级信息。
     *
     * @return [ClassInfo] 选中的班级对象，若无本地文件或解析失败则返回 null
     */
    suspend fun loadSelectedClass(): ClassInfo? = withContext(Dispatchers.IO) {
        if (!selectedClassFile.exists()) return@withContext null
        runCatching {
            val jsonString = selectedClassFile.readText()
            gson.fromJson(jsonString, ClassInfo::class.java)
        }.getOrNull()
    }

    /**
     * 保存选中的班级信息到本地文件。
     *
     * @param classInfo 班级对象信息
     * @return 保存是否成功
     */
    suspend fun saveSelectedClass(classInfo: ClassInfo): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            selectedClassFile.writeText(gson.toJson(classInfo))
            true
        }.getOrDefault(false)
    }
}
