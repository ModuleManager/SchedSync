package me.kairos.schedsync.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import me.kairos.schedsync.R
import me.kairos.schedsync.data.type.CourseState

/**
 * 实时课程状态通知构建与管理单例工具。
 */
@RequiresApi(Build.VERSION_CODES_FULL.BAKLAVA_1)
object LiveNotification {
    private const val CHANNEL_ID = "live_course_updates"
    private const val CHANNEL_NAME = "实时课程状态"

    /** 实时通知统一 ID */
    const val NOTIFICATION_ID = 1001

    /**
     * 检查当前状态栏中是否依然展示着该实时通知。
     *
     * @param context 应用程序上下文
     * @return 若通知正在活跃显示则返回 true，否则返回 false
     */
    fun isNotificationActive(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
    }

    /**
     * 创建实时课程通知渠道。
     *
     * @param context 应用程序上下文
     */
    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "用于显示当前课程进度"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * 判断当前课程状态是否应当主动弹出/刷新通知。
     *
     * 规则：正在上课 (Running) 或 开课前指定分钟以内 (Next <= advanceMinutes) 允许更新通知；
     * 无课、结课或在提前提醒时间范围之外时不主动弹出。
     *
     * @param state 目标课程状态
     * @param advanceMinutes 提前提醒分钟数，默认为 30
     * @return 是否需要进行通知推送
     */
    fun shouldNotify(state: CourseState, advanceMinutes: Int = 30): Boolean {
        return when (state) {
            is CourseState.Running -> true
            is CourseState.Next -> state.startsInMinutes <= advanceMinutes
            is CourseState.NoCourse -> false
        }
    }

    /**
     * 根据实时课程状态构建系统通知对象。
     *
     * @param context 应用程序上下文
     * @param state 当前课程状态 ([CourseState])
     * @return 构建完成的 [Notification] 对象
     */
    fun buildLiveNotification(context: Context, state: CourseState): Notification {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isOngoing = state !is CourseState.NoCourse

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setRequestPromotedOngoing(isOngoing)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_EVENT)

        when (state) {
            is CourseState.Running -> {
                val course = state.course
                val progressStyle = Notification.ProgressStyle()
                    .setProgressPoints(
                        listOf(
                            Notification.ProgressStyle.Point(state.progress)
                        )
                    )

                val locationPrefix = if (!course.location.isNullOrBlank()) course.location.replace("教学楼", "") else "未知"
                builder.setContentTitle(formatTitle(course.name))
                    .setContentText("$locationPrefix 剩余 ${state.remainingMinutes} 分钟")
                    .setShortCriticalText("${state.remainingMinutes}min").style = progressStyle
            }
            is CourseState.Next -> {
                val course = state.course
                val locationPrefix = if (!course.location.isNullOrBlank()) course.location.replace("教学楼", "") else "未知"
                builder.setContentTitle(formatTitle(course.name))
                    .setContentText("$locationPrefix ${state.startsInMinutes} 分钟后开始")
                    .setShortCriticalText(locationPrefix)
            }
            is CourseState.NoCourse -> {
                builder.setContentTitle("暂无实时课程")
                    .setContentText("课程已结束或设置时间内无课程")
            }
        }

        return builder.build()
    }

    /**
     * 格式化课程标题文本，过长时自动截断。
     *
     * @param title 原始标题
     * @return 格式化后的标题文本
     */
    private fun formatTitle(title: String?): String {
        if (title.isNullOrEmpty()) return ""
        return if (title.length > 10) {
            title.take(10) + "..."
        } else {
            title
        }
    }
}
