# 移除系统动态色彩管理，使用自定义色彩体系

本计划旨在将软件的色彩管理逻辑与系统动态色彩（Material You）彻底解耦，确保应用始终使用开发者定义的色彩体系。

## 用户审核要求

- **禁用动态色彩**：不再根据系统壁纸自动生成配色方案。
- **强制自定义配色**：始终应用 `Color.kt` 中定义的亮色与暗色方案。
- **代码清理**：移除主题管理中关于 `dynamicColor` 的冗余逻辑。

## 拟议更改

### 1. 主题配置重构 (Theme.kt)

#### [MODIFY] [Theme.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/schedsync/ui/theme/Theme.kt)
- 移除 `MyApplicationTheme` 中的 `dynamicColor` 参数及相关逻辑。
- 将 `colorScheme` 的计算逻辑简化为仅根据 `darkTheme` 状态切换 `DarkColorScheme` 和 `LightColorScheme`。

### 2. 移除调用处依赖 (MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/schedsync/MainActivity.kt)
- 更新 `MyApplicationTheme` 的调用方式，移除不再需要的参数（如果 `Theme.kt` 中已移除）。

## 验证计划

### 手动验证
- **色彩一致性检查**：在 Android 12+ 设备上更改系统壁纸，验证应用配色是否保持不变。
- **深浅模式切换**：验证在“设置”中切换显示模式时，应用能否正确加载自定义的深色/浅色配色。
