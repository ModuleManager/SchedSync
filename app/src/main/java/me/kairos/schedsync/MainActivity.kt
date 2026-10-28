package me.kairos.schedsync

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.kairos.schedsync.setting.SettingsViewModel
import me.kairos.schedsync.ui.HomeTab
import me.kairos.schedsync.ui.InfoTab
import me.kairos.schedsync.ui.SettingTab
import me.kairos.schedsync.ui.theme.MyApplicationTheme
import me.kairos.schedsync.utils.NotificationUtils

/**
 * 应用程序主 Activity 入口。
 *
 * 初始化全屏沉浸式边缘到边缘 (Edge-to-Edge) 布局，并绑定 [SettingsViewModel] 提供全局的主题模式切换与界面渲染。
 */
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val displayMode by settingsViewModel.displayMode.collectAsStateWithLifecycle()
            val notificationAllow by settingsViewModel.notificationAllow.collectAsStateWithLifecycle()

            LaunchedEffect(notificationAllow) {
                NotificationUtils.checkAndStartServiceIfAllowed(this@MainActivity, notificationAllow)
            }

            MyApplicationTheme(displayMode = displayMode) {
                Screen(settingsViewModel = settingsViewModel)
            }
        }
    }
}

/**
 * 主界面容器 Composable。
 *
 * 包含顶部状态栏留白、Tab 选项卡平滑水平滑动切换动画、动态滚动检测以及底部浮动导航栏。
 *
 * @param settingsViewModel 设置 ViewModel 实例
 */
@Composable
private fun Screen(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackBarHostState = remember { SnackbarHostState() }

    val homeScrollState = rememberScrollState()
    val infoScrollState = rememberScrollState()
    val settingScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val isVisible by remember(selectedTab) {
        derivedStateOf {
            val scrollState = when (selectedTab) {
                0 -> homeScrollState
                1 -> infoScrollState
                else -> settingScrollState
            }
            !(scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "TabAnimation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeTab.HomeTabContent(settingsViewModel = settingsViewModel, scrollState = homeScrollState)
                    1 -> InfoTab.InfoTabContent(
                        settingsViewModel = settingsViewModel,
                        scrollState = infoScrollState
                    )
                    else -> SettingTab.SettingsTabContent(settingsViewModel = settingsViewModel, scrollState = settingScrollState)
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 5.dp)
                        .navigationBarsPadding(),
                    shape = CircleShape,
                    color = Color.Transparent,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomNavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { NavIcon(R.drawable.baseline_house_24, "首页") },
                            label = "首页",
                            modifier = Modifier.weight(1f)
                        )
                        CustomNavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { NavIcon(R.drawable.baseline_info_24, "信息") },
                            label = "信息",
                            modifier = Modifier.weight(1f)
                        )
                        CustomNavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { NavIcon(R.drawable.baseline_settings_24, "设置") },
                            label = "设置",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

/**
 * 自定义带圆角动画与颜色渐变的底部导航项控件。
 *
 * @param selected 是否处于选中状态
 * @param onClick 点击事件回调
 * @param icon 图标 Composable
 * @param label 导航文本
 * @param modifier 修饰符
 */
@Composable
private fun CustomNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 28.dp else 12.dp,
        animationSpec = tween(durationMillis = 400),
        label = "cornerRadius"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        },
        animationSpec = tween(durationMillis = 400),
        label = "containerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "contentColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 导航栏图标组件辅助函数。
 *
 * @param resId 矢量图标资源 ID
 * @param contentDesc 无障碍描述文本
 */
@Composable
private fun NavIcon(@DrawableRes resId: Int, contentDesc: String) {
    Icon(
        imageVector = ImageVector.vectorResource(id = resId),
        contentDescription = contentDesc
    )
}
