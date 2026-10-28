package me.kairos.schedsync.data

/**
 * 节次时间范围表示模型。
 *
 * @property startText 上课开始时间文本 (例如 "08:30")
 * @property endText 下课结束时间文本 (例如 "09:15")
 */
data class TimeSlot(
    val startText: String,
    val endText: String
)
