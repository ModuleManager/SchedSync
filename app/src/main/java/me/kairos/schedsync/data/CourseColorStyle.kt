package me.kairos.schedsync.data

import androidx.compose.ui.graphics.Color

/**
 * 课程卡片色彩主题表达对象。
 *
 * @property containerColor 卡片背景容器颜色
 * @property contentColor 卡片内部文字与图标颜色
 */
data class CourseColorStyle(
    val containerColor: Color,
    val contentColor: Color
)
