package me.kairos.schedsync.setting

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import me.kairos.schedsync.api.LoginResult
import me.kairos.schedsync.data.AccountInfo
import me.kairos.schedsync.data.json.class_json.ClassInfo
import me.kairos.schedsync.data.json.class_schedule_json.ScheduleResponse
import me.kairos.schedsync.data.json.person_schedule_json.PersonScheduleResponse
import me.kairos.schedsync.data.repository.ScheduleRepository
import me.kairos.schedsync.data.source.ScheduleLocalDataSource
import me.kairos.schedsync.data.source.ScheduleRemoteDataSource
import me.kairos.schedsync.data.type.DisplayMode
import me.kairos.schedsync.data.type.ScheduleType

/**
 * 设置与主界面状态管理 ViewModel。
 *
 * 连接 UI 视图与 [ScheduleRepository]，维护账号信息绑定、验证码弹窗交互、学期及周次切换、
 * 界面显示模式切换以及课表异步同步等核心业务逻辑。
 *
 * @param application 应用程序 Application 实例
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScheduleRepository = ScheduleRepository(
        localDataSource = ScheduleLocalDataSource(application.applicationContext),
        remoteDataSource = ScheduleRemoteDataSource(),
        scope = viewModelScope
    )

    private val _uiEvent = MutableSharedFlow<String>()
    /** UI 消息事件流（如提示 Toast），解耦 ViewModel 与 Context */
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    /** 账号配置状态流 */
    val accountState: StateFlow<AccountInfo> = repository.accountState
    /** 界面显示模式状态流 */
    val displayMode: StateFlow<DisplayMode> = repository.displayMode
    /** 课表显示类型状态流 */
    val scheduleType: StateFlow<ScheduleType> = repository.scheduleType
    /** 实时通知设置状态流 */
    val notificationAllow: StateFlow<Boolean> = repository.isNotificationAllow
    /** 课程提前提醒分钟数状态流 */
    val notificationAdvanceMinutes: StateFlow<Int> = repository.notificationAdvanceMinutes
    /** 原始班级课表状态流 */
    val classScheduleState: StateFlow<ScheduleResponse?> = repository.classScheduleState
    /** 原始个人课表状态流 */
    val personScheduleState: StateFlow<PersonScheduleResponse?> = repository.personScheduleState
    /** 统一归一化后的课表状态流 */
    val scheduleState: StateFlow<ScheduleResponse?> = repository.scheduleState
    /** 当前周次状态流 */
    val courseWeek: StateFlow<Int?> = repository.courseWeek

    /** 班级列表状态 */
    var classListState by mutableStateOf<List<ClassInfo>>(emptyList())
        private set

    /** 是否正在加载班级列表 */
    var isLoadingClassList by mutableStateOf(false)
        private set

    /** 当前选中的班级 */
    var selectedClass by mutableStateOf<ClassInfo?>(null)

    /** 账号设置多步骤对话框当前步骤 (0:关闭, 1:输入凭据, 2:选择班级) */
    var currentDialogStep by mutableIntStateOf(0)
        private set

    /** 是否正在登录鉴权 */
    var isLoggingIn by mutableStateOf(false)
        private set

    /** 登录失败错误提示文本 */
    var loginErrorMessage by mutableStateOf<String?>(null)
        private set

    /** 是否正在同步课表数据 */
    var isSyncing by mutableStateOf(false)

    /** 验证码图片字节数据 */
    var captchaImageBytes by mutableStateOf<ByteArray?>(null)
        private set

    /** 是否正在展示验证码输入弹窗 */
    var isShowingCaptchaDialog by mutableStateOf(false)

    /** 用户输入的验证码文本 */
    var captchaInputText by mutableStateOf("")

    private var pendingUserId = ""
    private var pendingPassword = ""
    private var pendingSyncAction: (suspend () -> Unit)? = null

    init {
        viewModelScope.launch {
            repository.loadInitialData()
        }
        viewModelScope.launch {
            repository.selectedClass.collect { classInfo ->
                selectedClass = classInfo
            }
        }
        viewModelScope.launch {
            repository.classListState.collect { list ->
                classListState = list
            }
        }
    }

    /** 减少当前教学周数 */
    fun decreaseWeek() {
        viewModelScope.launch {
            repository.decreaseWeek()
        }
    }

    /** 增加当前教学周数 */
    fun increaseWeek() {
        viewModelScope.launch {
            repository.increaseWeek()
        }
    }

    /**
     * 设置特定教学周数。
     *
     * @param week 目标周数
     */
    fun setCourseWeek(week: Int) {
        viewModelScope.launch {
            repository.setCourseWeek(week)
        }
    }

    /** 打开账号绑定设置弹窗 */
    fun openAccountSetup() {
        loginErrorMessage = null
        viewModelScope.launch {
            repository.logout()
            currentDialogStep = 1
        }
    }

    /** 关闭并重置所有设置弹窗状态 */
    fun dismissDialog() {
        currentDialogStep = 0
        loginErrorMessage = null
        isShowingCaptchaDialog = false
        captchaImageBytes = null
        captchaInputText = ""
    }

    /** 刷新图形验证码 */
    fun refreshCaptcha() {
        viewModelScope.launch {
            captchaImageBytes = repository.refreshCaptcha()
        }
    }

    /** 提交输入的验证码进行二次登录校验 */
    fun submitCaptcha() {
        if (captchaInputText.isBlank()) return
        verifyAndSaveAccount(pendingUserId, pendingPassword, captchaInputText)
    }

    /**
     * 设置主题界面显示模式。
     *
     * @param mode 主题模式 (DARK / LIGHT / FOLLOW)
     */
    fun setDisplayMode(mode: DisplayMode) {
        viewModelScope.launch {
            repository.setDisplayMode(mode)
        }
    }

    /**
     * 设置课表类型（个人/班级）。
     *
     * @param type 课表显示类型
     */
    fun setScheduleType(type: ScheduleType) {
        viewModelScope.launch {
            repository.setScheduleType(type)
        }
    }

    /**
     * 设置实时通知开启状态。
     *
     * @param type 开启标记
     */
    fun setNotificationAllow(type: Boolean) {
        viewModelScope.launch {
            repository.setNotificationAllow(type)
        }
    }

    /**
     * 设置课程提前提醒时间 (单位：分钟，范围 10-180)。
     *
     * @param minutes 提前分钟数
     */
    fun setNotificationAdvanceMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.setNotificationAdvanceMinutes(minutes)
        }
    }

    /**
     * 保存账号密码至本地。
     *
     * @param userId 学号
     * @param password 密码
     */
    fun saveAccountToJson(userId: String, password: String) {
        viewModelScope.launch {
            repository.saveAccountInfo(userId, password)
        }
    }

    /**
     * 保存学年与学期配置。
     *
     * @param year 学年
     * @param semester 学期
     */
    fun saveAcademicConfig(year: String, semester: String) {
        viewModelScope.launch {
            repository.saveAcademicConfig(year, semester)
        }
    }

    /**
     * 验证教务网账号并在登录成功后保存凭据。
     *
     * @param userId 学号
     * @param password 密码
     * @param captcha 验证码文本（可选）
     */
    fun verifyAndSaveAccount(userId: String, password: String, captcha: String? = null) {
        viewModelScope.launch {
            loginInternal(userId, password, captcha, isFromUserAction = true)
        }
    }

    /**
     * 内部方法：执行登录鉴权逻辑。
     */
    private suspend fun loginInternal(
        userId: String,
        password: String,
        captcha: String? = null,
        isFromUserAction: Boolean = false
    ): LoginResult {
        isLoggingIn = true
        loginErrorMessage = null

        pendingUserId = userId
        pendingPassword = password

        val result = repository.login(userId, password, captcha)

        isLoggingIn = false

        when (result) {
            is LoginResult.Success -> {
                isShowingCaptchaDialog = false
                captchaImageBytes = null
                if (currentDialogStep == 1) currentDialogStep = 2

                if (isFromUserAction) {
                    pendingSyncAction?.let { action ->
                        viewModelScope.launch {
                            action()
                            pendingSyncAction = null
                        }
                    }
                }
            }
            is LoginResult.CaptchaRequired -> {
                captchaImageBytes = result.captchaBytes
                isShowingCaptchaDialog = true
                captchaInputText = ""
            }
            is LoginResult.Failure -> {
                loginErrorMessage = result.message
                if (isShowingCaptchaDialog) {
                    refreshCaptcha()
                    captchaInputText = ""
                }
            }
        }
        return result
    }

    /**
     * 保存并持久化选中的班级。
     *
     * @param classInfo 目标班级对象
     */
    fun saveSelectedClass(classInfo: ClassInfo) {
        selectedClass = classInfo
        viewModelScope.launch {
            repository.saveSelectedClass(classInfo)
        }
    }

    /**
     * 读取本地缓存的班级课表。
     *
     * @return 班级课表响应对象
     */
    fun loadLocalClassSchedule(): ScheduleResponse? {
        return repository.classScheduleState.value
    }

    /**
     * 读取本地缓存的个人课表。
     *
     * @return 个人课表响应对象
     */
    fun loadLocalPersonSchedule(): PersonScheduleResponse? {
        return repository.personScheduleState.value
    }

    /**
     * 从网络拉取并覆盖保存班级课表。
     *
     * @param selectClass 目标班级对象
     * @return 同步是否成功
     */
    suspend fun fetchAndSaveClassSchedule(selectClass: ClassInfo?): Boolean {
        isSyncing = true
        if (selectClass == null) {
            _uiEvent.emit("请先在设置页绑定班级！")
            isSyncing = false
            return false
        }

        val success = repository.syncSchedule(selectClass)
        isSyncing = false

        if (success) {
            _uiEvent.emit("班级课表已成功同步并覆盖至本地！")
        } else {
            _uiEvent.emit("同步失败：未获取到有效数据或写文件失败")
        }
        return success
    }

    /**
     * 从网络拉取并覆盖保存个人课表。
     *
     * @return 同步是否成功
     */
    suspend fun fetchAndSavePersonSchedule(): Boolean {
        isSyncing = true
        val success = repository.syncSchedule(null)
        isSyncing = false

        if (success) {
            _uiEvent.emit("个人课表已成功同步并覆盖至本地！")
        } else {
            _uiEvent.emit("同步失败：未获取到有效数据或写文件失败")
        }
        return success
    }

    /**
     * 异步拉取指定学年的可选班级列表信息。
     *
     * @param year 学年
     */
    fun fetchClassInformation(year: Int) {
        viewModelScope.launch {
            isLoadingClassList = true
            try {
                repository.fetchClassInformation(year)
            } finally {
                isLoadingClassList = false
            }
        }
    }

    /**
     * 触发课表数据同步，包含自动账号校验逻辑。
     *
     * @param selectClass 当前绑定的班级信息
     */
    fun syncSchedule(selectClass: ClassInfo?) {
        val syncAction: suspend () -> Unit = {
            if (scheduleType.value == ScheduleType.PERSONAL) {
                fetchAndSavePersonSchedule()
            } else {
                fetchAndSaveClassSchedule(selectClass)
            }
            pendingSyncAction = null
        }

        pendingSyncAction = syncAction

        viewModelScope.launch {
            val account = accountState.value
            if (account.userId.isEmpty() || account.password.isEmpty()) {
                _uiEvent.emit("请先在设置页绑定账号密码！")
                return@launch
            }

            isSyncing = true
            val result = loginInternal(account.userId, account.password, isFromUserAction = true)

            if (result !is LoginResult.Success) {
                isSyncing = false
                _uiEvent.emit(result.let { (it as? LoginResult.Failure)?.message } ?: "登录鉴权失败")
            }
        }
    }
}
