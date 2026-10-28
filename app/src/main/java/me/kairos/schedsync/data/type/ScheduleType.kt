package me.kairos.schedsync.data.type

/**
 * 课表展示类型枚举。
 *
 * @property displayName 课表类型的UI显示名称
 */
enum class ScheduleType(val displayName: String) {
    /** 个人课表类型 */
    PERSONAL("个人课表"),

    /** 班级课表类型 */
    CLASS("班级课表")
}
