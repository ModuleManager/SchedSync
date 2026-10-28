package me.kairos.schedsync.data

/**
 * 二维课表网格数据容器。
 *
 * 包装 12节 × 7天 的 [CourseCell] 单元格二维列表矩阵。
 *
 * @property scheduleData 课表单元格二维矩阵 (外层 12节次，内层 7天)
 */
data class CourseTable(
    val scheduleData: List<List<CourseCell>>
)
