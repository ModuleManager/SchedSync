package me.kairos.schedsync.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kairos.schedsync.data.source.ScheduleLocalDataSource
import me.kairos.schedsync.data.type.CourseState
import me.kairos.schedsync.data.type.ScheduleType
import me.kairos.schedsync.notification.LiveNotification
import me.kairos.schedsync.utils.CourseUtils
import kotlin.time.Duration.Companion.minutes

/**
 * 实时课程状态更新前台服务。
 *
 * 在后台定时计算当前及即将开始的课程状态，并实时刷新前台系统通知。
 */
@RequiresApi(Build.VERSION_CODES_FULL.BAKLAVA_1)
class LiveUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var updateJob: Job? = null
    private lateinit var localDataSource: ScheduleLocalDataSource

    /**
     * 服务创建生命周期，初始化本地数据源。
     */
    override fun onCreate() {
        super.onCreate()
        localDataSource = ScheduleLocalDataSource(applicationContext)
    }

    /**
     * 服务启动入口。
     *
     * 异步加载本地课表与配置信息并启动后台定时更新任务，避免主线程 IO 阻塞。
     *
     * @param intent 启动 Intent
     * @param flags 启动标志
     * @param startId 启动 ID
     * @return [START_STICKY] 服务终止后由系统尝试重启
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LiveNotification.createNotificationChannel(this)

        // 规范：Foreground Service 必须在 onStartCommand 5 秒内同步调用 startForeground 避免系统强制杀死进程
        val initialNotification = LiveNotification.buildLiveNotification(this, CourseState.NoCourse)
        startForeground(
            LiveNotification.NOTIFICATION_ID,
            initialNotification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        serviceScope.launch(Dispatchers.IO) {
            val settings = runCatching { localDataSource.loadAccountSettings() }.getOrNull()

            if (settings?.isNotificationAllow == false) {
                withContext(Dispatchers.Main) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                return@launch
            }

            val currentWeek = settings?.courseWeek
            val kbList = if (settings?.scheduleType == ScheduleType.PERSONAL) {
                runCatching { localDataSource.loadPersonSchedule() }.getOrNull()?.kbList?.map { it.toCourseItem() }
            } else {
                runCatching { localDataSource.loadClassSchedule() }.getOrNull()?.kbList
            }

            val initialState = CourseUtils.getCurrentCourseState(kbList, currentWeek)
            val advanceMinutes = settings?.notificationAdvanceMinutes ?: 30

            withContext(Dispatchers.Main) {
                updateNotificationState(initialState, advanceMinutes)
            }
        }

        // 启动后台自动定时更新任务（每 1 分钟刷新一次）
        startAutoUpdate()

        return START_STICKY
    }

    /**
     * 根据实时课程状态刷新或移除前台通知。
     *
     * @param state 当前课程状态
     * @param advanceMinutes 课程提前提醒分钟数
     */
    private fun updateNotificationState(state: CourseState, advanceMinutes: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (LiveNotification.shouldNotify(state, advanceMinutes)) {
            val notification = LiveNotification.buildLiveNotification(this, state)
            startForeground(
                LiveNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            if (LiveNotification.isNotificationActive(this)) {
                val noCourseNotification = LiveNotification.buildLiveNotification(this, CourseState.NoCourse)
                notificationManager.notify(LiveNotification.NOTIFICATION_ID, noCourseNotification)
                stopForeground(STOP_FOREGROUND_DETACH)
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
    }

    /**
     * 启动循环轮询任务，每分钟重新计算并刷新课程状态。
     */
    private fun startAutoUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                val settings = localDataSource.loadAccountSettings()

                if (!settings.isNotificationAllow) {
                    withContext(Dispatchers.Main) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    break
                }

                val currentWeek = settings.courseWeek
                val kbList = if (settings.scheduleType == ScheduleType.PERSONAL) {
                    localDataSource.loadPersonSchedule()?.kbList?.map { it.toCourseItem() }
                } else {
                    localDataSource.loadClassSchedule()?.kbList
                }

                val currentState = CourseUtils.getCurrentCourseState(kbList, currentWeek)
                val advanceMinutes = settings.notificationAdvanceMinutes

                withContext(Dispatchers.Main) {
                    updateNotificationState(currentState, advanceMinutes)
                }

                delay(1.minutes)
            }
        }
    }

    /**
     * 服务销毁生命周期，取消后台所有协程任务。
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    /**
     * 不提供 Binder 绑定，返回 null。
     *
     * @param intent 绑定 Intent
     * @return null
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
