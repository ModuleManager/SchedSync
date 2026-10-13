package me.kairos.schedsync.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import me.kairos.schedsync.data.type.DisplayMode

/**
 * 深色暗彩方案。
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = DarkOnPrimary,
    secondary = PurpleGrey80,
    onSecondary = DarkOnSecondary,
    tertiary = Pink80,

    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceContainer = DarkSurfaceContainer,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

/**
 * 亮色日彩方案。
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = LightOnPrimary,
    secondary = PurpleGrey40,
    onSecondary = LightOnSecondary,
    tertiary = Pink40,

    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

/**
 * 应用程序 Material 3 全局主题配置 Composable。
 *
 * 根据 [displayMode] 选项动态在暗色模式、亮色模式及跟随系统之间动态切换。
 *
 * @param displayMode 界面暗亮模式模式类型 ([DisplayMode])
 * @param content 子 UI 内容块
 */
@Composable
fun MyApplicationTheme(
    displayMode: DisplayMode = DisplayMode.FOLLOW,
    content: @Composable () -> Unit
) {
    val darkTheme = when (displayMode) {
        DisplayMode.FOLLOW -> isSystemInDarkTheme()
        DisplayMode.DARK -> true
        DisplayMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
