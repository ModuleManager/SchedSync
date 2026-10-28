package me.kairos.schedsync.utils

import me.kairos.schedsync.data.CourseCell
import me.kairos.schedsync.data.CourseTable
import me.kairos.schedsync.data.NotificationCourse
import me.kairos.schedsync.data.TimeSlot
import me.kairos.schedsync.data.json.class_schedule_json.CourseItem
import me.kairos.schedsync.data.type.CourseState
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.ceil

/**
 * 课程表解析与网格构建工具类。
 *
 * 提供周次有效性判定 (`zcd` 字符串解析)、上课时间范围映射以及 12节 × 7天 课表二维网格构建能力。
 */
object CourseUtils {
    /** 星期标题列表 */
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    /** 节次数值列表 (1..12) */
    val periods = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    /** 节次时段分组 (上午/下午/晚上) */
    val periodGroups = listOf(
        "上午", "上午", "上午", "上午",
        "下午", "下午", "下午", "下午",
        "晚上", "晚上", "晚上", "晚上"
    )

    /** 默认每节课的时间段映射表 */
    val defaultTimeSlots = listOf(
        TimeSlot("08:30", "09:15"),
        TimeSlot("09:20", "10:05"),

        TimeSlot("10:25", "11:10"),
        TimeSlot("11:15", "12:00"),

        TimeSlot("13:30", "14:15"),
        TimeSlot("14:20", "15:05"),

        TimeSlot("15:15", "16:00"),
        TimeSlot("16:05", "16:55"),

        TimeSlot("18:00", "18:45"),
        TimeSlot("18:45", "19:30"),

        TimeSlot("19:30", "20:15"),
        TimeSlot("20:15", "21:00")
    )

    /**
     * 校验目标周次是否属于教务系统返回的周次描述表达式 (`zcd`) 范畴内。
     *
     * 支持解析如 "1-16周", "1-8周(单)", "9-16周(双)", "3,5,7周" 等复杂周次表达。
     *
     * @param targetWeek 待校验的目标周次
     * @param zcd 周次描述表达式
     * @return 该课程在目标周次是否有效
     */
    fun isWeekInZcd(targetWeek: Int?, zcd: String?): Boolean {
        if (targetWeek == null || zcd.isNullOrBlank()) return true

        return runCatching {
            val segments = zcd.split(Regex("[,，;；\\s]+"))
            segments.any { segment ->
                if (segment.isBlank()) return@any false

                val isOddOnly = segment.contains("单")
                val isEvenOnly = segment.contains("双")

                if (isOddOnly && targetWeek % 2 == 0) return@any false
                if (isEvenOnly && targetWeek % 2 != 0) return@any false

                val normalizedSegment = segment.replace(Regex("[~—至]"), "-")
                val cleanSegment = normalizedSegment.replace(Regex("[^0-9-]"), "")

                if (cleanSegment.contains("-")) {
                    val parts = cleanSegment.split("-")
                    if (parts.size == 2) {
                        val start = parts[0].toIntOrNull() ?: return@any false
                        val end = parts[1].toIntOrNull() ?: return@any false
                        targetWeek in start..end
                    } else {
                        false
                    }
                } else {
                    val singleWeek = cleanSegment.toIntOrNull()
                    singleWeek == targetWeek
                }
            }
        }.getOrDefault(false)
    }

    /**
     * 根据节次代码 (`jcor` 或 `jc`) 映射得出格式化的起止时间对。
     *
     * @param jcor 节次代码表达式，如 "1-2" 或 "3-4"
     * @param timeSlots 时间段列表，默认为 [defaultTimeSlots]
     * @return 包含开始时间与结束时间的 [Pair]，若解析失败则返回 null
     */
    fun getCourseTimeRange(
        jcor: String?,
        timeSlots: List<TimeSlot> = defaultTimeSlots
    ): Pair<String, String>? {
        if (jcor.isNullOrBlank()) return null

        val periodsList = jcor.replace(Regex("[^0-9-]"), "")
            .split("-")
            .mapNotNull { it.toIntOrNull() }

        if (periodsList.isEmpty()) return null

        val startPeriod = periodsList.first()
        val endPeriod = periodsList.last()

        val startIndex = startPeriod - 1
        val endIndex = endPeriod - 1

        val startTime = timeSlots.getOrNull(startIndex)?.startText
        val endTime = timeSlots.getOrNull(endIndex)?.endText

        return if (startTime != null && endTime != null) {
            Pair(startTime, endTime)
        } else null
    }

    /**
     * 根据原始课程列表与当前周次构建二维课表数据结构 [CourseTable]。
     *
     * 自动筛选出当前周次生效的课程，并将其填充至 12节 × 7天 的二维数组网格中，支持课程重叠与冲突记录。
     *
     * @param kbList 原始课程项列表
     * @param currentWeek 当前周次
     * @return 构建完成的 [CourseTable] 课表网格数据对象
     */
    fun buildCourseTable(kbList: List<CourseItem>?, currentWeek: Int?): CourseTable {
        val grid = List(12) { List(7) { mutableListOf<CourseItem>() } }

        val filteredList = kbList?.filter { isWeekInZcd(currentWeek, it.zcd) }

        filteredList?.forEach { item ->
            val dayIndex = (item.xqj?.toIntOrNull() ?: 1) - 1

            val jcStr = item.jc ?: ""
            if (dayIndex in 0..6 && jcStr.isNotEmpty()) {
                val cleanJc = jcStr.replace(Regex("[^0-9-]"), "")
                val parts = cleanJc.split("-").mapNotNull { it.toIntOrNull() }
                val startJc = parts.firstOrNull() ?: 1
                val endJc = parts.lastOrNull() ?: startJc

                for (p in startJc..endJc) {
                    if (p in 1..12) {
                        val cellList = grid[p - 1][dayIndex]
                        if (!cellList.contains(item)) {
                            cellList.add(item)
                        }
                    }
                }
            }
        }

        val tableGrid = grid.map { row ->
            row.map { list ->
                CourseCell(courses = list)
            }
        }

        return CourseTable(scheduleData = tableGrid)
    }

    /**
     * 根据当前时间与课表，计算并返回当前的课程状态 (Running / Next / NoCourse)。
     *
     * @param kbList 课程列表
     * @param currentWeek 当前周次
     * @param nowDateTime 当前时间，默认 LocalDateTime 当前时间
     * @param timeSlots 时间段列表，默认为 [defaultTimeSlots]
     * @return [CourseState] 状态
     */
    fun getCurrentCourseState(
        kbList: List<CourseItem>?,
        currentWeek: Int?,
        nowDateTime: LocalDateTime = LocalDateTime.now(),
        timeSlots: List<TimeSlot> = defaultTimeSlots
    ): CourseState {
        if (kbList.isNullOrEmpty()) return CourseState.NoCourse

        val dayOfWeek = nowDateTime.dayOfWeek.value
        val todayCourses = kbList.filter { item ->
            item.xqj?.toIntOrNull() == dayOfWeek && isWeekInZcd(currentWeek, item.zcd)
        }

        if (todayCourses.isEmpty()) return CourseState.NoCourse

        data class CourseTimeInfo(
            val item: CourseItem,
            val startDateTime: LocalDateTime,
            val endDateTime: LocalDateTime
        )

        val parsedCourses = todayCourses.mapNotNull { item ->
            val jcCode = if (!item.jc.isNullOrBlank()) item.jc else item.jcor
            val timeRange = getCourseTimeRange(jcCode, timeSlots) ?: return@mapNotNull null
            val (startTimeStr, endTimeStr) = timeRange

            runCatching {
                val startTime = LocalTime.parse(startTimeStr)
                val endTime = LocalTime.parse(endTimeStr)
                val todayDate = nowDateTime.toLocalDate()

                val startDateTime = todayDate.atTime(startTime)
                val endDateTime = todayDate.atTime(endTime)

                CourseTimeInfo(item, startDateTime, endDateTime)
            }.getOrNull()
        }.sortedBy { it.startDateTime }

        if (parsedCourses.isEmpty()) return CourseState.NoCourse

        val runningCourse = parsedCourses.firstOrNull { info ->
            !nowDateTime.isBefore(info.startDateTime) && nowDateTime.isBefore(info.endDateTime)
        }

        if (runningCourse != null) {
            val totalSeconds = Duration.between(runningCourse.startDateTime, runningCourse.endDateTime).seconds
            val elapsedSeconds = Duration.between(runningCourse.startDateTime, nowDateTime).seconds
            val progress = if (totalSeconds > 0) {
                ((elapsedSeconds.toDouble() / totalSeconds.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else 0

            val remainingSeconds = Duration.between(nowDateTime, runningCourse.endDateTime).seconds
            val remainingMinutes = ceil(remainingSeconds / 60.0).toLong().coerceAtLeast(0)

            val zone = ZoneId.systemDefault()
            val notificationCourse = NotificationCourse(
                name = runningCourse.item.kcmc ?: "未知课程",
                teacher = runningCourse.item.xm,
                location = runningCourse.item.cdmc,
                startTime = runningCourse.startDateTime.atZone(zone).toInstant(),
                endTime = runningCourse.endDateTime.atZone(zone).toInstant()
            )

            return CourseState.Running(
                course = notificationCourse,
                progress = progress,
                remainingMinutes = remainingMinutes
            )
        }

        val nextCourse = parsedCourses.firstOrNull { info ->
            nowDateTime.isBefore(info.startDateTime)
        }

        if (nextCourse != null) {
            val startsInSeconds = Duration.between(nowDateTime, nextCourse.startDateTime).seconds
            val startsInMinutes = ceil(startsInSeconds / 60.0).toLong().coerceAtLeast(0)

            val zone = ZoneId.systemDefault()
            val notificationCourse = NotificationCourse(
                name = nextCourse.item.kcmc ?: "未知课程",
                teacher = nextCourse.item.xm,
                location = nextCourse.item.cdmc,
                startTime = nextCourse.startDateTime.atZone(zone).toInstant(),
                endTime = nextCourse.endDateTime.atZone(zone).toInstant()
            )

            return CourseState.Next(
                course = notificationCourse,
                startsInMinutes = startsInMinutes
            )
        }

        return CourseState.NoCourse
    }
}
