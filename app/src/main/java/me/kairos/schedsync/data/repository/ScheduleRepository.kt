package me.kairos.schedsync.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.kairos.schedsync.api.LoginResult
import me.kairos.schedsync.data.AccountInfo
import me.kairos.schedsync.data.json.class_json.ClassInfo
import me.kairos.schedsync.data.json.class_schedule_json.ScheduleResponse
import me.kairos.schedsync.data.json.person_schedule_json.PersonScheduleResponse
import me.kairos.schedsync.data.source.ScheduleLocalDataSource
import me.kairos.schedsync.data.source.ScheduleRemoteDataSource
import me.kairos.schedsync.data.type.DisplayMode
import me.kairos.schedsync.data.type.ScheduleType

/**
 * 课表数据仓库。
 *
 * 统一管理本地缓存数据 ([ScheduleLocalDataSource]) 与远程网络数据 ([ScheduleRemoteDataSource])，
 * 提供响应式的状态管理和数据合并能力。
 *
 * @param localDataSource 本地持久化存储数据源
 * @param remoteDataSource 远程 API 数据源
 * @param scope 作用域，用于 StateFlow 共享控制
 */
class ScheduleRepository(
    private val localDataSource: ScheduleLocalDataSource,
    private val remoteDataSource: ScheduleRemoteDataSource,
    private val scope: CoroutineScope
) {
    private val _accountState = MutableStateFlow(AccountInfo())
    /** 账号配置状态流 */
    val accountState: StateFlow<AccountInfo> = _accountState.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.FOLLOW)
    /** 界面显示模式状态流 */
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _scheduleType = MutableStateFlow(ScheduleType.PERSONAL)
    /** 当前选择的课表类型状态流（个人课表 / 班级课表） */
    val scheduleType: StateFlow<ScheduleType> = _scheduleType.asStateFlow()

    private val _isNotificationAllow = MutableStateFlow(false)
    /** 实时通知开启状态流 */
    val isNotificationAllow: StateFlow<Boolean> = _isNotificationAllow.asStateFlow()

    private val _notificationAdvanceMinutes = MutableStateFlow(30)
    /** 课程提前提醒分钟数状态流 (范围 10-180) */
    val notificationAdvanceMinutes: StateFlow<Int> = _notificationAdvanceMinutes.asStateFlow()

    private val _courseWeek = MutableStateFlow<Int?>(1)
    /** 当前教学周次状态流 */
    val courseWeek: StateFlow<Int?> = _courseWeek.asStateFlow()

    private val _selectedClass = MutableStateFlow<ClassInfo?>(null)
    /** 当前选中的班级信息状态流 */
    val selectedClass: StateFlow<ClassInfo?> = _selectedClass.asStateFlow()

    private val _classScheduleState = MutableStateFlow<ScheduleResponse?>(null)
    /** 原始班级课表响应状态流 */
    val classScheduleState: StateFlow<ScheduleResponse?> = _classScheduleState.asStateFlow()

    private val _personScheduleState = MutableStateFlow<PersonScheduleResponse?>(null)
    /** 原始个人课表响应状态流 */
    val personScheduleState: StateFlow<PersonScheduleResponse?> = _personScheduleState.asStateFlow()

    private val _classListState = MutableStateFlow<List<ClassInfo>>(emptyList())
    /** 班级列表数据状态流 */
    val classListState: StateFlow<List<ClassInfo>> = _classListState.asStateFlow()

    /**
     * 归一化展示的课表状态流。
     *
     * 根据当前选择的 [scheduleType]，自动将个人课表 (`PersonScheduleResponse`) 或班级课表 (`ScheduleResponse`)
     * 合并映射为统一的 [ScheduleResponse] 数据格式供 UI 侧无缝订阅。
     */
    val scheduleState: StateFlow<ScheduleResponse?> = combine(
        scheduleType,
        _classScheduleState,
        _personScheduleState
    ) { type, classRes, personRes ->
        if (type == ScheduleType.PERSONAL) {
            personRes?.let {
                ScheduleResponse(
                    kbList = it.kbList?.map { p -> p.toCourseItem() },
                    xqjmcMap = it.xqjmcMap
                )
            }
        } else {
            classRes
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * 初始化加载本地持久化数据。
     */
    suspend fun loadInitialData() {
        val settings = localDataSource.loadAccountSettings()
        _accountState.value = settings.accountInfo
        _displayMode.value = settings.displayMode
        _scheduleType.value = settings.scheduleType
        _isNotificationAllow.value = settings.isNotificationAllow
        _notificationAdvanceMinutes.value = settings.notificationAdvanceMinutes
        _courseWeek.value = settings.courseWeek

        _selectedClass.value = localDataSource.loadSelectedClass()
        _classScheduleState.value = localDataSource.loadClassSchedule()
        _personScheduleState.value = localDataSource.loadPersonSchedule()
    }

    /**
     * 设置界面显示模式并持久化存储。
     *
     * @param mode 主题显示模式 (DARK / LIGHT / FOLLOW)
     */
    suspend fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        saveSettings()
    }

    /**
     * 设置课表显示类型并持久化存储。
     *
     * @param type 课表类型 (PERSONAL / CLASS)
     */
    suspend fun setScheduleType(type: ScheduleType) {
        _scheduleType.value = type
        saveSettings()
    }

    /**
     * 设置实时通知开关状态并持久化存储。
     *
     * @param type 通知开启标记
     */
    suspend fun setNotificationAllow(type: Boolean) {
        _isNotificationAllow.value = type
        saveSettings()
    }

    /**
     * 设置课程提前提醒时间并持久化存储。
     *
     * @param minutes 提前分钟数，会自动限定在 10~180 范围内
     */
    suspend fun setNotificationAdvanceMinutes(minutes: Int) {
        val safeMinutes = minutes.coerceIn(10, 180)
        _notificationAdvanceMinutes.value = safeMinutes
        saveSettings()
    }

    /**
     * 设置当前教学周数。
     *
     * @param week 周数值，会自动限定在 1~30 范围内
     */
    suspend fun setCourseWeek(week: Int) {
        val safeWeek = week.coerceIn(1, 30)
        _courseWeek.value = safeWeek
        saveSettings()
    }

    /**
     * 减少当前教学周数。
     */
    suspend fun decreaseWeek() {
        val current = _courseWeek.value ?: 1
        setCourseWeek(if (current > 1) current - 1 else 1)
    }

    /**
     * 增加当前教学周数。
     */
    suspend fun increaseWeek() {
        val current = _courseWeek.value ?: 1
        setCourseWeek(if (current < 30) current + 1 else 30)
    }

    /**
     * 保存并更新账号认证信息。
     *
     * @param userId 学号
     * @param password 密码
     */
    suspend fun saveAccountInfo(userId: String, password: String) {
        _accountState.update { AccountInfo(userId, password, it.academicYear, it.semester) }
        saveSettings()
    }

    /**
     * 保存学年与学期配置。
     *
     * @param year 学年 (如 "2026")
     * @param semester 学期 (如 "1" 或 "2")
     */
    suspend fun saveAcademicConfig(year: String, semester: String) {
        _accountState.update { it.copy(academicYear = year, semester = semester) }
        saveSettings()
    }

    /**
     * 保存选中的班级信息。
     *
     * @param classInfo 班级对象
     */
    suspend fun saveSelectedClass(classInfo: ClassInfo) {
        _selectedClass.value = classInfo
        localDataSource.saveSelectedClass(classInfo)
    }

    /**
     * 发起登录操作，若登录成功则自动触发拉取当前学年的班级列表信息。
     *
     * @param userId 学号
     * @param password 密码
     * @param captcha 验证码文本（可选）
     * @return [LoginResult] 登录状态结果
     */
    suspend fun login(userId: String, password: String, captcha: String? = null): LoginResult {
        val result = remoteDataSource.login(userId, password, captcha)
        if (result is LoginResult.Success) {
            saveAccountInfo(userId, password)
            val yearInt = accountState.value.academicYear.toIntOrNull() ?: 2026
            fetchClassInformation(yearInt)
        }
        return result
    }

    /**
     * 刷新图形验证码字节数据。
     *
     * @return 验证码图片的字节数组
     */
    suspend fun refreshCaptcha(): ByteArray? {
        return remoteDataSource.refreshCaptcha()
    }

    /**
     * 拉取指定年级的可选班级列表。
     *
     * @param year 年级/学年
     * @return 班级列表
     */
    suspend fun fetchClassInformation(year: Int): List<ClassInfo> {
        val list = remoteDataSource.fetchClassInformation(year)
        _classListState.value = list
        return list
    }

    /**
     * 同步最新课表数据。根据当前选中的 [scheduleType] 拉取个人课表或班级课表，并更新本地存储及内存状态。
     *
     * @param selectClass 绑定的班级（仅在选择班级课表时使用）
     * @return 同步是否成功
     */
    suspend fun syncSchedule(selectClass: ClassInfo?): Boolean {
        val account = accountState.value
        val yearInt = account.academicYear.toIntOrNull() ?: 2026
        val termInt = account.semester.toIntOrNull() ?: 1

        return if (scheduleType.value == ScheduleType.PERSONAL) {
            val personalData = remoteDataSource.fetchPersonalSchedule(yearInt, termInt)
            if (personalData != null) {
                localDataSource.savePersonSchedule(personalData)
                _personScheduleState.value = personalData
                true
            } else false
        } else {
            if (selectClass == null) return false
            val classData = remoteDataSource.fetchClassSchedule(yearInt, termInt, selectClass)
            if (classData != null) {
                localDataSource.saveClassSchedule(classData)
                _classScheduleState.value = classData
                true
            } else false
        }
    }

    /**
     * 登出并注销当前网络会话。
     */
    suspend fun logout() {
        remoteDataSource.logout()
    }

    /**
     * 内部方法：保存全局偏好设置到本地。
     */
    private suspend fun saveSettings() {
        localDataSource.saveAccountSettings(
            accountInfo = _accountState.value,
            displayMode = _displayMode.value,
            scheduleType = _scheduleType.value,
            isNotificationAllow = _isNotificationAllow.value,
            notificationAdvanceMinutes = _notificationAdvanceMinutes.value,
            courseWeek = _courseWeek.value ?: 1
        )
    }
}
