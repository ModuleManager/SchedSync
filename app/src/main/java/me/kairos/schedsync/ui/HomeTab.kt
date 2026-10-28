package me.kairos.schedsync.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.kairos.schedsync.R
import me.kairos.schedsync.data.CourseCell
import me.kairos.schedsync.data.CourseColorStyle
import me.kairos.schedsync.data.CourseTable
import me.kairos.schedsync.data.TimeSlot
import me.kairos.schedsync.data.json.class_schedule_json.CourseItem
import me.kairos.schedsync.setting.SettingsViewModel
import me.kairos.schedsync.utils.CourseUtils
import kotlin.math.abs

/**
 * 首页课表查看视图组件单例。
 *
 * 负责展现 12节 × 7天 二维网格课表、节次与时间映射列、课程卡片颜色散列、
 * 多课程重叠冲突提示以及点击课程弹窗查看详情。
 */
object HomeTab {
    private val SLOT_HEIGHT = 60.dp

    /**
     * 首页选项卡内容入口。
     *
     * @param settingsViewModel 设置 ViewModel
     * @param scrollState 垂直滚动状态对象
     */
    @Composable
    fun HomeTabContent(settingsViewModel: SettingsViewModel, scrollState: ScrollState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "课表查看",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            ScheduleScreen(
                settingsViewModel = settingsViewModel,
                onImportClick = {
                    settingsViewModel.syncSchedule(
                        settingsViewModel.selectedClass
                    )
                }
            )
        }
    }

    /**
     * 课表主界面网格与状态判定组件。
     *
     * @param settingsViewModel 设置 ViewModel
     * @param onImportClick 点击导入课表按钮触发的回调
     */
    @Composable
    private fun ScheduleScreen(
        settingsViewModel: SettingsViewModel,
        onImportClick: () -> Unit = {}
    ) {
        val scheduleData by settingsViewModel.scheduleState.collectAsState()
        val currentWeek by settingsViewModel.courseWeek.collectAsState()

        if (scheduleData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_info_24),
                            contentDescription = "未导入课表",
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无课表信息",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "你还没有导入你的学期课表！\n在设置页添加账户后可以点击按钮导入。\n导入后可以开启智能课表管理与日程提醒功能。\n( •̀ ω •́ )✧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onImportClick,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(text = "立即导入课表")
                        }
                    }
                }
            }
        } else {
            val courseList: List<CourseItem>? = scheduleData!!.kbList
            var selectedCell by remember { mutableStateOf<CourseCell?>(null) }
            val courseTable = remember(courseList, currentWeek) {
                CourseUtils.buildCourseTable(courseList, currentWeek)
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                settingsViewModel.selectedClass?.let {
                                    Text(
                                        it.zymc,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                settingsViewModel.selectedClass?.let {
                                    Text(
                                        it.bj,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.7f
                                        )
                                    )
                                }
                            }

                            if (currentWeek != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "第 $currentWeek 周",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        LeftPeriodColumn(courseTable)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            WeekHeaderRow()
                            ScheduleGrid(
                                courseTable = courseTable,
                                currentWeek = currentWeek,
                                onCellClick = { cell -> selectedCell = cell }
                            )
                        }
                    }
                }
            }

            selectedCell?.let { cell ->
                AlertDialog(
                    onDismissRequest = { selectedCell = null },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (cell.hasConflict) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "⚠️ 冲突 (${cell.conflictCount} 门课程)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = cell.primaryCourse?.kcmc ?: "课程详情",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (cell.hasConflict) {
                                Text(
                                    text = "当前时段重叠了多门课程，详情如下：",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            cell.courses.forEachIndexed { index, course ->
                                val timeRange = CourseUtils.getCourseTimeRange(course.jcor)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${course.kcmc ?: "未命名课程"}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "🚩 地点: ${course.cdmc ?: "无"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "👨‍🏫 教师: ${course.xm ?: "无"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        if (!course.zcd.isNullOrEmpty()) {
                                            Text(
                                                "📅 周数: ${course.zcd}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }

                                        if (!course.jcor.isNullOrEmpty()) {
                                            val timeText = if (timeRange != null) {
                                                "⏰ 时间: 第 ${course.jcor} 节 (${timeRange.first} ~ ${timeRange.second})"
                                            } else {
                                                "⏰ 节次: 第 ${course.jcor} 节"
                                            }
                                            Text(
                                                timeText,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedCell = null }) { Text("关闭") }
                    }
                )
            }
        }
    }

    /**
     * 单个课程卡片渲染组件。
     *
     * @param cell 单元格对象
     * @param modifier 修饰符
     * @param onClick 点击事件回调
     */
    @Composable
    private fun CourseCard(
        cell: CourseCell,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val course = cell.primaryCourse ?: return
        val colorStyle = getSmartCourseColor(course.kcmc ?: "")

        Box(modifier = modifier) {
            ElevatedCard(
                onClick = onClick,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorStyle.containerColor,
                    contentColor = colorStyle.contentColor
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = course.kcmc ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = colorStyle.contentColor
                    )
                    Column {
                        if (!course.cdmc.isNullOrEmpty()) {
                            Text(
                                text = "🚩${course.cdmc.replace("教学楼", "")} 教学楼",
                                fontSize = 11.sp,
                                color = colorStyle.contentColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!course.xm.isNullOrEmpty()) {
                            Text(
                                text = "👨‍🏫 ${course.xm}",
                                fontSize = 11.sp,
                                color = colorStyle.contentColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (cell.hasConflict) {
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp, topStart = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "⚠️(${cell.conflictCount})",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }

    /**
     * 按天排列的单列课程渲染组件，支持节次跨度高度换算。
     *
     * @param courseTable 课表网格数据对象
     * @param dayIndex 星期列索引 (0~6)
     * @param currentWeek 当前周次
     * @param onCellClick 单元格点击事件
     */
    @Composable
    private fun DayColumn(
        courseTable: CourseTable,
        dayIndex: Int,
        currentWeek: Int?,
        onCellClick: (CourseCell) -> Unit
    ) {
        Column(modifier = Modifier.width(100.dp)) {
            var currentPeriod = 0
            val totalPeriods = courseTable.scheduleData.size
            while (currentPeriod < totalPeriods) {
                val cell = courseTable.scheduleData[currentPeriod][dayIndex]

                if (cell.isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SLOT_HEIGHT)
                            .border(
                                0.5.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                    )
                    currentPeriod++
                } else {
                    val primaryCourse = cell.primaryCourse
                    val span = parseJcorSpan(primaryCourse?.jcor)
                    val actualSpan = span.coerceAtMost(totalPeriods - currentPeriod)
                    CourseCard(
                        cell = cell,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(SLOT_HEIGHT * actualSpan)
                            .padding(2.dp),
                        onClick = { onCellClick(cell) }
                    )
                    currentPeriod += actualSpan
                }
            }
        }
    }

    /**
     * 顶部星期表头行组件。
     */
    @Composable
    private fun WeekHeaderRow() {
        Row(
            modifier = Modifier
                .height(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            CourseUtils.weekDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    /**
     * 课表主网格视图组件。
     *
     * @param courseTable 课表网格数据
     * @param currentWeek 当前周次
     * @param onCellClick 单元格点击事件
     */
    @Composable
    private fun ScheduleGrid(
        courseTable: CourseTable,
        currentWeek: Int?,
        onCellClick: (CourseCell) -> Unit
    ) {
        Row {
            for (dayIndex in CourseUtils.weekDays.indices) {
                DayColumn(
                    courseTable = courseTable,
                    dayIndex = dayIndex,
                    currentWeek = currentWeek,
                    onCellClick = onCellClick
                )
            }
        }
    }

    /**
     * 左侧节次数值与对应时间范围指示列组件。
     *
     * @param courseTable 课表网格对象
     * @param timeSlots 节次时间段映射表
     */
    @Composable
    private fun LeftPeriodColumn(
        courseTable: CourseTable,
        timeSlots: List<TimeSlot> = CourseUtils.defaultTimeSlots
    ) {
        val actualCount = courseTable.scheduleData.size
        val displayPeriods = CourseUtils.periods.take(actualCount)
        val displayGroups = CourseUtils.periodGroups.take(actualCount)

        Column(
            modifier = Modifier
                .width(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节次",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            displayPeriods.forEachIndexed { index, period ->
                val group = displayGroups.getOrNull(index) ?: ""
                val timeSlot = timeSlots.getOrNull(index)

                val bgColor = when (group) {
                    "上午" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    "下午" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SLOT_HEIGHT)
                        .background(bgColor)
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = period,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                text = timeSlot?.startText ?: "",
                                fontSize = 8.sp,
                                lineHeight = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Text(
                                text = timeSlot?.endText ?: "",
                                fontSize = 8.sp,
                                lineHeight = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据课程名称哈希生成稳定的 Material3 卡片配色体系方案。
     *
     * @param courseName 课程名称
     * @return [CourseColorStyle] 包含背景容器与文本的前景色组
     */
    @Composable
    private fun getSmartCourseColor(courseName: String): CourseColorStyle {
        val scheme = MaterialTheme.colorScheme

        val colorPalette = remember(scheme) {
            listOf(
                CourseColorStyle(scheme.primaryContainer, scheme.onPrimaryContainer),
                CourseColorStyle(scheme.secondaryContainer, scheme.onSecondaryContainer),
                CourseColorStyle(scheme.tertiaryContainer, scheme.onTertiaryContainer),
                CourseColorStyle(scheme.surfaceVariant, scheme.onSurfaceVariant)
            )
        }

        if (courseName.isEmpty()) {
            return CourseColorStyle(Color.Unspecified, scheme.onSurface)
        }

        val index = abs(courseName.hashCode()) % colorPalette.size
        return colorPalette[index]
    }

    /**
     * 解析节次字符串跨度节数。
     *
     * @param jcor 节次字符串（例如 "1-2"）
     * @return 跨越节数，解析失败返回 1
     */
    private fun parseJcorSpan(jcor: String?): Int {
        if (jcor.isNullOrBlank()) return 1
        val cleanJcor = jcor.replace(Regex("[^0-9-]"), "")
        val parts = cleanJcor.split("-")
        return if (parts.size == 2) {
            val start = parts[0].toIntOrNull()
            val end = parts[1].toIntOrNull()
            if (start != null && end != null && end >= start) {
                end - start + 1
            } else 1
        } else {
            1
        }
    }
}
