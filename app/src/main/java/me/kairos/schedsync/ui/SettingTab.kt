package me.kairos.schedsync.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.kairos.schedsync.R
import me.kairos.schedsync.data.json.class_json.ClassInfo
import me.kairos.schedsync.data.type.DisplayMode
import me.kairos.schedsync.data.type.ScheduleType
import me.kairos.schedsync.setting.SettingsViewModel
import me.kairos.schedsync.utils.NotificationUtils

/**
 * 设置选项卡视图单例组件。
 *
 * 包含教务网账号绑定管理、学年学期设置、课表类型动画切换滑块、主题暗亮模式列表及软件关于信息弹窗。
 */
object SettingTab {
    /**
     * 设置选项卡界面内容入口。
     *
     * @param settingsViewModel 设置 ViewModel
     * @param scrollState 滚动状态对象
     */
    @Composable
    fun SettingsTabContent(settingsViewModel: SettingsViewModel, scrollState: ScrollState) {
        val context: Context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "设置选项",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text(
                text = "账号管理",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            AccountManagementSection(settingsViewModel)
            AcademicConfigSection(settingsViewModel)
            ScheduleTypeSection(settingsViewModel)

            Text(
                text = "通知设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            NotificationSetting(viewModel = settingsViewModel, context = context)

            Text(
                text = "用户界面",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            SingleSelectList(viewModel = settingsViewModel)

            Text(
                text = "软件信息",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            AboutSection()
        }
    }

    /**
     * 账号绑定与多步骤弹窗组件。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun AccountManagementSection(settingsViewModel: SettingsViewModel) {
        val accountInfo by settingsViewModel.accountState.collectAsState()

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_account_circle_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    headlineContent = {
                        Text(
                            text = if (accountInfo.userId.isEmpty()) "未绑定首义教务账号" else "学号：${accountInfo.userId}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    supportingContent = {
                        val subtitle = when {
                            accountInfo.userId.isEmpty() -> "绑定后可同步课表并选择默认班级"
                            settingsViewModel.selectedClass != null -> "绑定班级：${settingsViewModel.selectedClass!!.bj}"
                            else -> "账号已绑定（未选择默认班级）"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        TextButton(onClick = { settingsViewModel.openAccountSetup() }) {
                            Text(if (accountInfo.userId.isEmpty()) "绑定账号" else "修改账号")
                        }
                    }
                )
            }
        }

        if (settingsViewModel.currentDialogStep == 1) {
            var inputUsername by remember { mutableStateOf(accountInfo.userId) }
            var inputPassword by remember { mutableStateOf(accountInfo.password) }
            var passwordVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    if (!settingsViewModel.isLoggingIn) settingsViewModel.dismissDialog()
                },
                title = {
                    Text(text = if (accountInfo.userId.isEmpty()) "绑定教务账号" else "修改教务账号")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputUsername,
                            onValueChange = { inputUsername = it },
                            label = { Text("学号") },
                            singleLine = true,
                            enabled = !settingsViewModel.isLoggingIn,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = { Text("密码") },
                            singleLine = true,
                            enabled = !settingsViewModel.isLoggingIn,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val image = if (passwordVisible)
                                    ImageVector.vectorResource(R.drawable.baseline_visibility_24)
                                else
                                    ImageVector.vectorResource(R.drawable.baseline_visibility_off_24)

                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    enabled = !settingsViewModel.isLoggingIn
                                ) {
                                    Icon(imageVector = image, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        settingsViewModel.loginErrorMessage?.let { errorMsg ->
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            settingsViewModel.verifyAndSaveAccount(inputUsername, inputPassword)
                        },
                        enabled = inputUsername.isNotBlank() && inputPassword.isNotBlank() && !settingsViewModel.isLoggingIn
                    ) {
                        if (settingsViewModel.isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("登录并继续")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { settingsViewModel.dismissDialog() },
                        enabled = !settingsViewModel.isLoggingIn
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        if (settingsViewModel.currentDialogStep == 2) {
            ClassSelectDialog(
                viewModel = settingsViewModel,
                onDismiss = {
                    settingsViewModel.dismissDialog()
                },
                onConfirm = { classInfo ->
                    settingsViewModel.saveSelectedClass(classInfo)
                    settingsViewModel.dismissDialog()
                }
            )
        }

        if (settingsViewModel.isShowingCaptchaDialog) {
            CaptchaDialog(
                viewModel = settingsViewModel,
                onDismiss = { settingsViewModel.isShowingCaptchaDialog = false },
                onConfirm = { settingsViewModel.submitCaptcha() }
            )
        }
    }

    /**
     * 图形验证码校验对话框组件。
     *
     * @param viewModel 设置 ViewModel
     * @param onDismiss 取消/关闭回调
     * @param onConfirm 提交验证码回调
     */
    @Composable
    private fun CaptchaDialog(
        viewModel: SettingsViewModel,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        val captchaBitmap = remember(viewModel.captchaImageBytes) {
            viewModel.captchaImageBytes?.let {
                try {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "安全校验",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "检测到登录环境异常，请输入验证码继续",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier
                            .height(60.dp)
                            .width(150.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { viewModel.refreshCaptcha() },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        if (captchaBitmap != null) {
                            Image(
                                bitmap = captchaBitmap,
                                contentDescription = "验证码图片",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    Text(
                        text = "看不清？点击图片刷新",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = viewModel.captchaInputText,
                        onValueChange = { viewModel.captchaInputText = it },
                        label = { Text("验证码") },
                        singleLine = true,
                        enabled = !viewModel.isLoggingIn,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    )

                    viewModel.loginErrorMessage?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = viewModel.captchaInputText.isNotBlank() && !viewModel.isLoggingIn
                ) {
                    if (viewModel.isLoggingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("验证并登录")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !viewModel.isLoggingIn
                ) {
                    Text("取消")
                }
            }
        )
    }

    /**
     * 学年与学期配置项组件。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @Composable
    private fun AcademicConfigSection(settingsViewModel: SettingsViewModel) {
        val accountInfo by settingsViewModel.accountState.collectAsState()
        var showDialog by remember { mutableStateOf(false) }

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_date_range_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                headlineContent = {
                    Text(
                        text = "当前学期：\n${accountInfo.academicYear} 学年 第 ${accountInfo.semester} 学期",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                supportingContent = {
                    Text(
                        text = "过滤且提取对应的课表数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    TextButton(onClick = { showDialog = true }) {
                        Text("修改学期")
                    }
                }
            )
        }

        if (showDialog) {
            var inputYear by remember { mutableStateOf(accountInfo.academicYear) }
            var inputSemester by remember { mutableStateOf(accountInfo.semester) }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "设置学年与学期") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputYear,
                            onValueChange = { text ->
                                if (text.isEmpty() || (text.all { it.isDigit() } && text.length <= 4)) {
                                    inputYear = text
                                }
                            },
                            label = { Text("学年 (如: 2026)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = inputSemester,
                            onValueChange = { text ->
                                if (text.isEmpty() || (text.all { it.isDigit() } && text.length <= 1)) {
                                    inputSemester = text
                                }
                            },
                            label = { Text("学期 (如: 1 或 2)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            settingsViewModel.saveAcademicConfig(inputYear, inputSemester)
                            showDialog = false
                        },
                        enabled = inputYear.length == 4 && inputSemester.isNotEmpty()
                    ) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }

    /**
     * 课表类型（个人课表 / 班级课表）分段滑块切换组件。
     *
     * @param settingsViewModel 设置 ViewModel
     */
    @SuppressLint("UseOfNonLambdaOffsetOverload")
    @Composable
    private fun ScheduleTypeSection(settingsViewModel: SettingsViewModel) {
        val scheduleType by settingsViewModel.scheduleType.collectAsState()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_compare_arrows_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "课表类型",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "选择课表类型",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .width(160.dp)
                        .height(40.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val maxWidth = maxWidth
                    val itemWidth = maxWidth / 2
                    val selectedIndex = ScheduleType.entries.indexOf(scheduleType)
                    val indicatorOffset by animateDpAsState(
                        targetValue = itemWidth * selectedIndex,
                        animationSpec = tween(durationMillis = 300),
                        label = "indicatorOffset"
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScheduleType.entries.forEach { type ->
                            val isSelected = scheduleType == type
                            val animatedContentColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(durationMillis = 300),
                                label = "contentColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable { settingsViewModel.setScheduleType(type) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = animatedContentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 实时通知提醒开关与提前时间配置组件。
     *
     * @param viewModel 设置 ViewModel
     * @param context 应用程序 Context
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NotificationSetting(
        viewModel: SettingsViewModel,
        context: Context
    ) {
        val isNotificationAllow by viewModel.notificationAllow.collectAsStateWithLifecycle()
        val notificationAdvanceMinutes by viewModel.notificationAdvanceMinutes.collectAsStateWithLifecycle()
        val lifecycleOwner = LocalLifecycleOwner.current
        var showPermissionBottomSheet by remember { mutableStateOf(false) }

        var advanceInputText by remember(notificationAdvanceMinutes) {
            mutableStateOf(notificationAdvanceMinutes.toString())
        }
        var isAdvanceInputError by remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.setNotificationAllow(true)
                showPermissionBottomSheet = false
            } else {
                showPermissionBottomSheet = true
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val hasPermission = NotificationUtils.hasNotificationPermission(context)
                    if (!hasPermission && isNotificationAllow) {
                        viewModel.setNotificationAllow(false)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val handleEnableNotification = {
            val hasPermission = NotificationUtils.hasNotificationPermission(context)
            if (hasPermission) {
                viewModel.setNotificationAllow(true)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    showPermissionBottomSheet = true
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (isNotificationAllow) {
                                viewModel.setNotificationAllow(false)
                            } else {
                                handleEnableNotification()
                            }
                        }
                        .padding(
                            vertical = 4.dp,
                            horizontal = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                R.drawable.baseline_notifications_active_24
                            ),
                            contentDescription = "通知提醒",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Column {
                            Text(
                                text = "实时通知",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isNotificationAllow) "实时通知已开启" else "开启实时通知提醒",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isNotificationAllow,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                viewModel.setNotificationAllow(false)
                            } else {
                                handleEnableNotification()
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isNotificationAllow,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Text(
                            text = "课程提前提醒时间",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = advanceInputText,
                            onValueChange = { text ->
                                if (text.length <= 3 && (text.isEmpty() || text.all { it.isDigit() })) {
                                    advanceInputText = text
                                    val num = text.toIntOrNull()
                                    if (num != null && num in 10..180) {
                                        isAdvanceInputError = false
                                        viewModel.setNotificationAdvanceMinutes(num)
                                    } else {
                                        isAdvanceInputError = true
                                    }
                                }
                            },
                            label = { Text("提前提醒时间 (10-180)") },
                            suffix = { Text("分钟") },
                            singleLine = true,
                            isError = isAdvanceInputError,
                            supportingText = {
                                if (isAdvanceInputError) {
                                    Text(
                                        text = "请输入 10 至 180 之间的分钟数",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    Text(
                                        text = "开课前 $notificationAdvanceMinutes 分钟内弹出/更新实时通知",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showPermissionBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPermissionBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "开启通知权限",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "开启通知权限后，应用可以在后台为你提供实时课程状态更新和重要卡片提醒。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showPermissionBottomSheet = false
                            NotificationUtils.openNotificationSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("前往系统设置开启")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showPermissionBottomSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("暂不开启", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    /**
     * 暗亮显示模式模式切换列表组件。
     *
     * @param viewModel 设置 ViewModel
     */
    @Composable
    private fun SingleSelectList(viewModel: SettingsViewModel) {
        val selectedMode by viewModel.displayMode.collectAsStateWithLifecycle()
        val isAutoFollow = selectedMode == DisplayMode.FOLLOW

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            if (isAutoFollow) {
                                viewModel.setDisplayMode(DisplayMode.LIGHT)
                            } else {
                                viewModel.setDisplayMode(DisplayMode.FOLLOW)
                            }
                        }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_brightness_auto_24),
                            contentDescription = "暗亮模式",
                            tint = if (isAutoFollow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Column {
                            Text(
                                text = "暗亮模式",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isAutoFollow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAutoFollow) "自动切换到和系统统一" else "自定义亮暗模式",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isAutoFollow,
                        onCheckedChange = { checked ->
                            if (checked) {
                                viewModel.setDisplayMode(DisplayMode.FOLLOW)
                            } else {
                                viewModel.setDisplayMode(DisplayMode.LIGHT)
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = !isAutoFollow,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        val manualOptions = listOf(DisplayMode.LIGHT, DisplayMode.DARK)
                        manualOptions.forEach { mode ->
                            val isSelected = selectedMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .selectable(
                                        selected = isSelected,
                                        role = Role.RadioButton,
                                        onClick = { viewModel.setDisplayMode(mode) }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null
                                )
                                Text(
                                    text = when (mode) {
                                        DisplayMode.DARK -> "暗色模式"
                                        else -> "亮色模式"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = when (mode) {
                                        DisplayMode.DARK -> ImageVector.vectorResource(R.drawable.baseline_dark_mode_24)
                                        else -> ImageVector.vectorResource(R.drawable.baseline_light_mode_24)
                                    },
                                    contentDescription = when (mode) {
                                        DisplayMode.DARK -> "暗色模式"
                                        else -> "亮色模式"
                                    },
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 搜索并选择关联绑定班级对话框组件。
     *
     * @param viewModel 设置 ViewModel
     * @param onDismiss 取消/关闭事件
     * @param onConfirm 确认班级保存事件
     */
    @Composable
    private fun ClassSelectDialog(
        viewModel: SettingsViewModel,
        onDismiss: () -> Unit,
        onConfirm: (ClassInfo) -> Unit
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var tempSelectedClass by remember { mutableStateOf(viewModel.selectedClass) }

        val filteredList = remember(searchQuery, viewModel.classListState) {
            if (searchQuery.isBlank()) {
                viewModel.classListState
            } else {
                viewModel.classListState.filter {
                    it.bj.contains(searchQuery, ignoreCase = true) || it.zymc.contains(
                        searchQuery,
                        ignoreCase = true
                    ) || it.bh.contains(searchQuery)
                }
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "选择绑定班级") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索班级、专业...") },
                        leadingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_search_24),
                                contentDescription = "搜索"
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (viewModel.isLoadingClassList) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (viewModel.classListState.isEmpty()) "暂无班级数据" else "未找到匹配班级",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(filteredList, key = { it.bhId }) { item ->
                                val isSelected = tempSelectedClass?.bhId == item.bhId

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempSelectedClass = item }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { tempSelectedClass = item }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = item.bj,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = item.zymc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempSelectedClass?.let { onConfirm(it) }
                    },
                    enabled = tempSelectedClass != null
                ) {
                    Text("保存班级")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("暂不保存")
                }
            }
        )
    }

    /**
     * 关于软件信息与开源致谢对话框组件。
     */
    @Composable
    private fun AboutSection() {
        var showAboutDialog by remember { mutableStateOf(false) }
        val uriHandler = LocalUriHandler.current
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SoftwareInfo(
                onClick = { showAboutDialog = true }
            )
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.kairos127),
                            contentDescription = "作者头像",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    },
                    title = {
                        Text(
                            text = "关于这个应用程序",
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "版本：1.0.0 (Build 2026)\n" +
                                        "这里是开发者Kairos127！\n" +
                                        "这是一个基于 Jetpack Compose 的软件。\n" +
                                        "非常感谢你的下载使用！\n" +
                                        "万分感谢啦ヾ(≧▽xl)o",
                                textAlign = TextAlign.Left
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            Text(
                                text = "衷心感谢以下开源项目:",
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        uriHandler.openUri("https://github.com/DsukU/zhengfang")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_github_black),
                                        contentDescription = "GitHub Icon",
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DsukU/zhengfang",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.baseline_link_24),
                                    contentDescription = "打开链接",
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }

    /**
     * 软件信息入口列表项。
     *
     * @param onClick 点击入口回调
     */
    @Composable
    private fun SoftwareInfo(
        onClick: () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.baseline_info_24),
                        contentDescription = "关于软件",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "关于软件",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "你真的不想点进来看看吗？QwQ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
