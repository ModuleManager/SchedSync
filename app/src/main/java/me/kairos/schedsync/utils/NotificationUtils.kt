package me.kairos.schedsync.utils

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

/**
 * 系统通知与前台服务权限辅助管理工具类。
 */
object NotificationUtils {
    /**
     * 检查当前应用是否有发送通知的权限（兼容全 Android 版本）。
     *
     * @param context 应用程序上下文
     * @return 若已授权发送通知则返回 true，否则返回 false
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * 判断当前系统版本是否支持实时更新 notification 机制 (Android 16+ / API 36+)。
     *
     * @return 是否支持实时通知升级机制
     */
    fun isLiveUpdateSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 36
    }

    /**
     * 检查应用是否具备发布 Promoted Ongoing 实时通知的系统授权。
     *
     * @param context 应用程序上下文
     * @return 是否具备该权限
     */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun canPostLiveUpdate(context: Context): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        return notificationManager.canPostPromotedNotifications()
    }

    /**
     * 启动实时课程更新前台服务。
     *
     * @param context 应用程序上下文
     */
    fun startLiveUpdateService(context: Context) {
        if (isLiveUpdateSupported()) {
            val intent = Intent(context, me.kairos.schedsync.service.LiveUpdateService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }

    /**
     * 停止实时课程更新前台服务。
     *
     * @param context 应用程序上下文
     */
    fun stopLiveUpdateService(context: Context) {
        val intent = Intent(context, me.kairos.schedsync.service.LiveUpdateService::class.java)
        context.stopService(intent)
    }

    /**
     * 检查系统版本与设置条件，若允许则自动启动后台实时服务，否则关闭服务。
     *
     * @param context 应用程序上下文
     * @param isNotificationAllow 用户设置中是否开启了实时通知
     */
    fun checkAndStartServiceIfAllowed(context: Context, isNotificationAllow: Boolean) {
        if (isLiveUpdateSupported()) {
            if (canPostLiveUpdate(context) && isNotificationAllow) {
                startLiveUpdateService(context)
            } else {
                stopLiveUpdateService(context)
            }
        }
    }

    /**
     * 打开 Android 16+ 实时通知 Promotion 特别权限设置页面。
     *
     * @param context 应用程序上下文
     */
    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    fun openLiveUpdateSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS
        ).apply {
            putExtra(
                Settings.EXTRA_APP_PACKAGE,
                context.packageName
            )
        }

        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                openNotificationSettings(context)
            }
        } catch (_: ActivityNotFoundException) {
            openNotificationSettings(context)
        }
    }

    /**
     * 跳转至系统的应用通知权限设置页。
     *
     * @param context 应用程序上下文
     */
    fun openNotificationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
            }
        }
    }
}
