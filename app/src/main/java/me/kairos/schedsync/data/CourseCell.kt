package me.kairos.schedsync.data

import me.kairos.schedsync.data.json.class_schedule_json.CourseItem

/**
 * 课表网格单元格数据结构。
 *
 * 表示 12节 × 7天 矩阵中特定星期与节次落入的课程项集合，支持重叠课程冲突判断。
 *
 * @property courses 该单元格包含的课程项列表
 */
data class CourseCell(
    val courses: List<CourseItem> = emptyList()
) {
    /**
     * 该单元格是否为空（无课程）。
     */
    val isEmpty: Boolean
        get() = courses.isEmpty()

    /**
     * 该单元格展示的首要主课程（多课程重叠时默认呈现第一门）。
     */
    val primaryCourse: CourseItem?
        get() = courses.firstOrNull()

    /**
     * 该单元格是否存在多门课程重叠冲突。
     */
    val hasConflict: Boolean
        get() = courses.size > 1

    /**
     * 该单元格重叠的课程数量。
     */
    val conflictCount: Int
        get() = courses.size
}
