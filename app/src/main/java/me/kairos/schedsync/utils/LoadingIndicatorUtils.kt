package me.kairos.schedsync.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * Material Expressive 风格指示器与波浪进度组件工具。
 */
object LoadingIndicatorUtils {
    /**
     * M3 Expressive 风格 Wavy Progress Indicator (波浪进度条)。
     *
     * @param progress 进度比例 (0.0 ~ 1.0)
     * @param modifier Modifier 修饰符
     * @param color 进度主体前景色
     * @param trackColor 未激活轨道底色
     * @param strokeWidth 线条宽度
     * @param wavelength 波浪波长
     * @param gapSize 进度头与未激活轨道的间隔宽度
     */
    @Composable
    fun WavyProgress(
        progress: Float,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        trackColor: Color = color.copy(alpha = 0.25f),
        strokeWidth: Dp = 4.dp,
        wavelength: Dp = 24.dp,
        gapSize: Dp = 8.dp
    ) {
        val density = LocalDensity.current
        val wavelengthPx = with(density) {
            wavelength.toPx()
        }
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(
            label = "wavy_progress"
        )

        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000, easing = LinearEasing
                ), repeatMode = RepeatMode.Restart
            ),
            label = "wave_phase"
        )

        val targetAmplitude = when {
            progress <= 0.1f -> 0f
            progress >= 0.95f -> 0f
            else -> 1f
        }

        val amplitude by animateFloatAsState(
            targetValue = targetAmplitude, animationSpec = tween(
                durationMillis = 250, easing = FastOutSlowInEasing
            ), label = "wave_amplitude"
        )

        Canvas(
            modifier = modifier
        ) {
            drawWavyProgress(
                progress = progress,
                phase = phase,
                amplitude = amplitude,
                wavelengthPx = wavelengthPx,
                color = color,
                trackColor = trackColor,
                strokeWidth = strokeWidth.toPx(),
                gapSize = gapSize.toPx()
            )
        }
    }

    /**
     * M3 Expressive 风格 Morph Loading Indicator (多边形渐变形变加载动画)。
     *
     * @param modifier Modifier 修饰符
     * @param color 图形前景色
     * @param size 控件尺寸
     */
    @Composable
    fun MorphLoading(
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        size: Dp = 48.dp
    ) {
        val shapes = remember {
            createMorphShapes()
        }
        val morphs = remember(shapes) {
            List(shapes.size) { index ->
                Morph(
                    start = shapes[index], end = shapes[(index + 1) % shapes.size]
                )
            }
        }
        val morphProgress = remember {
            Animatable(0f)
        }
        val globalRotation = remember {
            Animatable(0f)
        }
        var morphIndex by remember {
            mutableIntStateOf(0)
        }
        var morphRotation by remember {
            mutableFloatStateOf(0f)
        }

        LaunchedEffect(morphs) {
            launch {
                val springSpec = spring<Float>(
                    dampingRatio = 0.6f, stiffness = 200f, visibilityThreshold = 0.1f
                )
                while (true) {
                    val result = morphProgress.animateTo(targetValue = 1f, animationSpec = springSpec)
                    if (result.endReason == AnimationEndReason.Finished) {
                        morphIndex = (morphIndex + 1) % morphs.size
                        morphProgress.snapTo(0f)
                        morphRotation = (morphRotation + 90f) % 360f
                    }

                    delay(650L.milliseconds)
                }
            }
            launch {
                globalRotation.animateTo(
                    targetValue = 360f, animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 4666, easing = LinearEasing
                        ), repeatMode = RepeatMode.Restart
                    )
                )
            }
        }

        val androidPath = remember {
            android.graphics.Path()
        }

        Canvas(
            modifier = modifier.size(size)
        ) {
            androidPath.reset()

            val morph = morphs[morphIndex]
            morph.toPath(progress = morphProgress.value, path = androidPath)

            val scale = min(this.size.width, this.size.height) / 2f
            val androidMatrix = android.graphics.Matrix()
            androidMatrix.setScale(scale, scale)
            androidPath.transform(androidMatrix)

            val bounds = android.graphics.RectF()
            androidPath.computeBounds(bounds, true)

            val dx = this.size.width / 2f - bounds.centerX()
            val dy = this.size.height / 2f - bounds.centerY()
            androidMatrix.setTranslate(dx, dy)
            androidPath.transform(androidMatrix)

            val composePath = androidPath.asComposePath()
            rotate(
                degrees = morphRotation + morphProgress.value * 90f + globalRotation.value,
                pivot = center
            ) {
                drawPath(path = composePath, color = color)
            }
        }
    }

    /**
     * 在 Canvas 上实际绘制波浪进度条逻辑。
     */
    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWavyProgress(
        progress: Float,
        phase: Float,
        amplitude: Float,
        wavelengthPx: Float,
        color: Color,
        trackColor: Color,
        strokeWidth: Float,
        gapSize: Float
    ) {
        val safeProgress = progress.coerceIn(0f, 1f)
        val centerY = size.height / 2f
        val progressX = size.width * safeProgress
        val activeEnd = (progressX - gapSize / 2f).coerceAtLeast(0f)
        val trackStart = (progressX + gapSize / 2f).coerceAtMost(size.width)

        if (trackStart < size.width) {
            drawLine(
                color = trackColor, start = Offset(trackStart, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        if (activeEnd <= 0.01f) {
            return
        }

        val path = Path()
        path.moveTo(0f, centerY)

        val baseAmplitude = min(size.height * 0.35f, 5.dp.toPx())
        val finalAmplitude = baseAmplitude * amplitude
        val step = max(0.5.dp.toPx(), size.width / 300f)
        var x = 0f

        while (x <= activeEnd) {
            val normalized = x / activeEnd
            val envelope = when {
                normalized < 0.08f -> {
                    val t = normalized / 0.08f
                    t * t * (3f - 2f * t)
                }
                normalized > 0.92f -> {
                    val t = (1f - normalized) / 0.08f
                    t * t * (3f - 2f * t)
                }
                else -> 1f
            }
            val y = centerY + finalAmplitude * envelope * sin(2.0 * PI * x / wavelengthPx + phase).toFloat()
            path.lineTo(x, y)
            x += step
        }

        path.lineTo(activeEnd, centerY)
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    /**
     * 创建用于 Morph 形变动画的多边形列表。
     */
    private fun createMorphShapes(): List<RoundedPolygon> {
        return listOf(
            RoundedPolygon.star(
                numVerticesPerRadius = 8, innerRadius = 0.72f, rounding = CornerRounding(0.32f)
            ).normalized(),
            RoundedPolygon.star(
                numVerticesPerRadius = 9, innerRadius = 0.82f, rounding = CornerRounding(0.48f)
            ).normalized(),
            RoundedPolygon.star(
                numVerticesPerRadius = 5, innerRadius = 0.90f, rounding = CornerRounding(0.18f)
            ).normalized(),
            RoundedPolygon.star(
                numVerticesPerRadius = 2, innerRadius = 0.92f, rounding = CornerRounding(0.65f)
            ).normalized(),
            RoundedPolygon.star(
                numVerticesPerRadius = 7, innerRadius = 0.66f, rounding = CornerRounding(0.35f)
            ).normalized(),
            RoundedPolygon.star(
                numVerticesPerRadius = 4, innerRadius = 0.82f, rounding = CornerRounding(0.45f)
            ).normalized(),
            RoundedPolygon.circle(
                numVertices = 32
            ).normalized()
        )
    }
}
