package me.kairos.schedsync.data

/**
 * 课程时间段分钟跨度映射模型。
 *
 * @property startMin 一天中自 00:00 算起的开始分钟数
 * @property endMin 一天中自 00:00 算起的结束分钟数
 * @property duration 该时间段的时长分钟数
 */
data class TimeSegment(
    val startMin: Int,
    val endMin: Int,
    val duration: Int
)
