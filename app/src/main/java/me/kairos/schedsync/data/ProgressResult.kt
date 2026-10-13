package me.kairos.schedsync.data

/**
 * 今日课程进度计算结果结构。
 *
 * @property progress 当前累积进度比例 (范围 0.0 ~ 1.0)
 * @property totalCourses 今日总课程数量
 * @property finishedCount 今日已结课的课程数量
 */
data class ProgressResult(
    val progress: Float,
    val totalCourses: Int,
    val finishedCount: Int
)
