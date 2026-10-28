package me.kairos.schedsync.data.type

import me.kairos.schedsync.data.NotificationCourse

/**
 * 实时课程状态密封接口。
 *
 * 表示当前时间点下课程的实时状态 (正在进行 / 即将开始 / 暂无课程)。
 */
sealed interface CourseState {

    /**
     * 当前正在上课状态。
     *
     * @property course 目标课程信息
     * @property progress 课程百分比进度 (0~100)
     * @property remainingMinutes 距离结课剩余分钟数
     */
    data class Running(
        val course: NotificationCourse,
        val progress: Int,
        val remainingMinutes: Long
    ) : CourseState

    /**
     * 当前无课但有下一节即将开始的课程状态。
     *
     * @property course 即将开始的课程信息
     * @property startsInMinutes 距离开课倒计时分钟数
     */
    data class Next(
        val course: NotificationCourse,
        val startsInMinutes: Long
    ) : CourseState

    /**
     * 当前暂无实时课程状态。
     */
    data object NoCourse : CourseState
}
