# 补全 CourseInfoCard 课程进度卡片与波浪进度条

为 `InfoTab.kt` 补全 `CourseInfoCard` 组件，该卡片能够根据当前日期和时间，智能过滤并计算今日课程的完成进度，并采用 Google Material Design 设计风格的动态波浪进度条进行精美展示。

## 核心设计与计算逻辑

1. **样式一致性**：使用 `MaterialTheme.shapes.medium` 形状和 `MaterialTheme.colorScheme.surfaceContainerLow` 颜色，与 `UserCard`、`SyncInfoCard`、`WeekDayCard` 保持完全一致的 Surface 容器风格。
2. **今日课程筛选**：
   - 获取当前星期几（1-7）。
   - 通过 `isWeekInZcd` 辅助函数验证课程在当前周数（`courseWeek`）是否处于活跃状态。
3. **完成度计算**：
   - 遍历今日所有活跃课程，解析其结束节次对应的标准时间。
   - 对比系统当前时间，只有当当前时间超出课程结束时间后，该课程才被归为“已过去课程”。
   - 百分比公式：`已结束课程数 / 今日总课程数`（若今日无课，显示为 100% 并展示贴心提示）。
4. **谷歌 MD 波浪进度条（Wavy Progress Indicator）**：
   - 采用高效的自定义 `Canvas` 绘制，通过 `rememberInfiniteTransition` 驱动正弦波相位变化，呈现平滑连续的科技感波浪流动效果。

## 拟更改文件

### Component: UI Layer

#### [MODIFY] [InfoTab.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/myapplication/ui/InfoTab.kt)
- 在 `InfoTabContent` 布局中添加 `CourseInfoCard(settingsViewModel = settingsViewModel)`。
- 实现 `CourseInfoCard` 组合函数，包括今日课程过滤、状态收集与时间对比逻辑。
- 添加基于 Canvas 绘制的动态波浪进度条组件 `WavyProgressIndicator`。
- 引入周数有效性判断函数 `isWeekInZcd`。

## 验证计划

### 自动化与编译检查
- 运行 Gradle Build 验证新增 Compose 代码与 Canvas 动画绘制完全无编译期或运行期错误。

### 手动检查
- 检查 `InfoTab` 页面下的卡片排版和整体视觉风格。
- 模拟有课和无课状态，确保百分比分子分母计算正确，波浪动画流畅，无内存泄漏。
