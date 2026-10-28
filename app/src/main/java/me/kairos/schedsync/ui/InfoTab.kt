package me.kairos.schedsync.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.kairos.schedsync.R
import me.kairos.schedsync.data.ProgressResult
import me.kairos.schedsync.data.TimeSegment
import me.kairos.schedsync.setting.SettingsViewModel
import me.kairos.schedsync.utils.CourseUtils
import me.kairos.schedsync.utils.LoadingIndicatorUtils
import kotlin.time.Duration.Companion.milliseconds

/**
 * 信息与进度统计选项卡视图单例对象。
 *
 * 展示用户绑定的专业班级信息、今日课程实时进度、自定义 Canvas 波浪指示器、
 * 手动同步控制及教学周数调节卡片。
 */
object InfoTab {
    /**
     * 信息选项卡界面内容入口。
     *
     * @param settingsViewModel 设置 ViewModel
     * @param scrollState 滚动状态
     */
    @Composable
    fun InfoTabContent(
        settingsViewModel: SettingsViewModel,
        scrollState: ScrollState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserCard(settingsViewModel = settingsViewModel)
            CourseInfoCard(settingsViewModel = settingsViewModel)
            SyncInfoCard(settingsViewModel = settingsViewModel)
            WeekDayCard(settingsViewModel = settingsViewModel)
        }
    }

    /**
     * 用户基本信息卡片。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun UserCard(settingsViewModel: SettingsViewModel) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_id_card_24),
                    contentDescription = "用户",
                    modifier = Modifier.size(64.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = settingsViewModel.selectedClass?.jgmc ?: "未知学院",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = settingsViewModel.selectedClass?.bj ?: "未绑定班级",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    /**
     * 今日课程进度动画展示卡片。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun CourseInfoCard(settingsViewModel: SettingsViewModel) {
        val scheduleData by settingsViewModel.scheduleState.collectAsState()
        val currentWeek by settingsViewModel.courseWeek.collectAsState()

        var tick by remember { mutableLongStateOf(0L) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000.milliseconds)
                tick++
            }
        }

        val result = remember(scheduleData, currentWeek, tick) {
            calculateCourseProgress(scheduleData?.kbList, currentWeek)
        }
        val targetProgress = result.progress
        val animatedProgress by animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            label = "progressAnimation"
        )
        val totalCourses = result.totalCourses
        val finishedCount = result.finishedCount

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "今日课程进度",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (totalCourses == 0) "今日无课，放松一下" else "已完成 $finishedCount / $totalCourses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LoadingIndicatorUtils.WavyProgress(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    gapSize = 8.dp,
                    wavelength = 24.dp
                )
            }
        }
    }

    /**
     * 课表同步控制卡片。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun SyncInfoCard(settingsViewModel: SettingsViewModel) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        settingsViewModel.syncSchedule(settingsViewModel.selectedClass)
                    },
                    enabled = !settingsViewModel.isSyncing,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_sync_24),
                            contentDescription = "同步",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "同步课表",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "同步最新课表到应用程序",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = settingsViewModel.isSyncing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LoadingIndicatorUtils.MorphLoading(
                        modifier = Modifier.size(52.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    /**
     * 教学周数调节卡片。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun WeekDayCard(settingsViewModel: SettingsViewModel) {
        val currentWeek by settingsViewModel.courseWeek.collectAsState()
        var showWeekInputDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showWeekInputDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "${currentWeek ?: 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "课程周数",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "调整课表周数筛选",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { settingsViewModel.decreaseWeek() },
                        enabled = (currentWeek ?: 1) > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_24),
                            contentDescription = "减少周数",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { settingsViewModel.increaseWeek() },
                        enabled = (currentWeek ?: 1) < 30,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_add_24),
                            contentDescription = "增加周数",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        if (showWeekInputDialog) {
            var inputText by remember { mutableStateOf((currentWeek ?: 1).toString()) }

            AlertDialog(
                onDismissRequest = { showWeekInputDialog = false },
                title = { Text(text = "设置课程周数") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "请输入当前课程周数 (1 - 30)：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { newValue ->
                                val digitsOnly = newValue.filter { it.isDigit() }
                                if (digitsOnly.length <= 2) {
                                    inputText = digitsOnly
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    val inputInt = inputText.toIntOrNull()
                    val isValid = inputInt != null && inputInt in 1..30
                    Button(
                        onClick = {
                            if (inputInt != null) {
                                settingsViewModel.setCourseWeek(inputInt)
                                showWeekInputDialog = false
                            }
                        },
                        enabled = isValid
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWeekInputDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }

    /**
     * 计算今日课程进度与完成状态。
     *
     * 过滤出属于今天且在当前周生效的课程，计算已过课程的累积时长占比。
     *
     * @param kbList 课程列表
     * @param currentWeek 当前教学周数
     * @return [ProgressResult] 进度统计结果
     */
    private fun calculateCourseProgress(
        kbList: List<me.kairos.schedsync.data.json.class_schedule_json.CourseItem>?,
        currentWeek: Int?
    ): ProgressResult {
        if (kbList.isNullOrEmpty()) return ProgressResult(1f, 0, 0)

        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = (calendar.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7 + 1
        val nowMin =
            calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)

        val todaySegments = kbList
            .filter { item ->
                item.xqj?.toIntOrNull() == dayOfWeek && CourseUtils.isWeekInZcd(
                    currentWeek,
                    item.zcd
                )
            }
            .distinctBy { "${it.kcmc}-${it.xqj}-${it.jc}" }
            .mapNotNull { item ->
                val jcStr = item.jc ?: ""
                val cleanJc = jcStr.replace(Regex("[^0-9-]"), "")
                val parts = cleanJc.split("-")
                val startJc = parts.firstOrNull()?.toIntOrNull() ?: return@mapNotNull null
                val endJc = parts.lastOrNull()?.toIntOrNull() ?: startJc

                val startTimeStr = CourseUtils.defaultTimeSlots.getOrNull(startJc - 1)?.startText
                    ?: return@mapNotNull null
                val endTimeStr = CourseUtils.defaultTimeSlots.getOrNull(endJc - 1)?.endText
                    ?: return@mapNotNull null

                TimeSegment(
                    startMin = timeToMinutes(startTimeStr),
                    endMin = timeToMinutes(endTimeStr),
                    duration = timeToMinutes(endTimeStr) - timeToMinutes(startTimeStr)
                )
            }
            .sortedBy { it.startMin }

        if (todaySegments.isEmpty()) return ProgressResult(1f, 0, 0)

        val totalDuration = todaySegments.sumOf { it.duration }
        var completedDuration = 0f
        var finishedCount = 0

        for (segment in todaySegments) {
            when {
                nowMin >= segment.endMin -> {
                    completedDuration += segment.duration
                    finishedCount++
                }

                nowMin >= segment.startMin -> {
                    completedDuration += (nowMin - segment.startMin)
                }

                else -> {
                }
            }
        }
        return ProgressResult(
            progress = if (totalDuration > 0) completedDuration / totalDuration else 1f,
            totalCourses = todaySegments.size,
            finishedCount = finishedCount
        )
    }

    /**
     * 将 "HH:mm" 格式时间文本转换为当天自 00:00 算起的分钟数值。
     *
     * @param time 时间字符串 (如 "08:30")
     * @return 分钟数值
     */
    private fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (_: Exception) {
            0
        }
    }
}
