package me.kairos.schedsync.data

/**
 * 实时课程状态通知专用的课程元数据。
 *
 * @property name 课程名称
 * @property teacher 授课教师姓名
 * @property location 上课教室或地点
 * @property startTime 课程开始时间戳
 * @property endTime 课程结束时间戳
 */
data class NotificationCourse(
    val name: String,
    val teacher: String? = null,
    val location: String? = null,
    val startTime: java.time.Instant?,
    val endTime: java.time.Instant?
)
