package me.kairos.schedsync.data

import me.kairos.schedsync.data.type.DisplayMode
import me.kairos.schedsync.data.type.ScheduleType

/**
 * 账号设置与应用程序全局偏好组合数据模型。
 *
 * @property accountInfo 账号学号与密码配置
 * @property displayMode 界面暗亮主题显示模式
 * @property isNotificationAllow 实时通知开启状态
 * @property notificationAdvanceMinutes 课程提前提醒分钟数
 * @property scheduleType 当前显示的课表类型 (个人课表 / 班级课表)
 * @property courseWeek 当前教学周数
 */
data class AccountSettingsData(
    val accountInfo: AccountInfo = AccountInfo(),
    val displayMode: DisplayMode = DisplayMode.FOLLOW,
    val isNotificationAllow: Boolean = false,
    val notificationAdvanceMinutes: Int = 30,
    val scheduleType: ScheduleType = ScheduleType.PERSONAL,
    val courseWeek: Int = 1
)
