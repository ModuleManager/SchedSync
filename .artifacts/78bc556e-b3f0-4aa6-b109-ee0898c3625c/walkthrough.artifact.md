# 验证码修复与 Google Material Design 弹窗实现总结

本项目已成功实现教务系统验证码自动检测、获取及交互式输入功能，并对 UI 进行了 Material 3 升级。

## 主要变更

### 1. 教务系统 API 增强 (ZengFangAPI.kt)
- **验证码检测**：在登录前自动检测 HTML 中是否包含 `yzm` 输入框。
- **状态驱动登录**：引入 `LoginResult` 密封接口，清晰区分 `Success`（成功）、`CaptchaRequired`（需要验证码）和 `Failure`（失败）。
- **图片获取**：新增 `/kaptcha` 接口支持，动态拉取验证码图片。
- **会话保持**：在请求验证码与提交登录表单之间保持相同的 Cookie 会话与安全令牌。

### 2. ViewModel 逻辑升级 (SettingsViewModel.kt)
- **状态流管理**：新增 `captchaImageBytes`、`isShowingCaptchaDialog` 等响应式变量。
- **交互流重构**：当登录结果返回 `CaptchaRequired` 时，自动挂起当前任务并激活验证码弹窗。
- **刷新机制**：支持手动点击刷新验证码。

### 3. Material 3 验证码弹窗 (SettingTab.kt)
- **视觉设计**：遵循 Google MD3 规范，采用圆角卡片、`OutlinedTextField` 以及符合色调的 `Surface` 容器。
- **交互体验**：
    - 验证码图片居中展示，点击可快速刷新。
    - 支持加载动画（CircularProgressIndicator）。
    - 友好的错误信息反馈机制。

## 验证结论

- **逻辑验证**：已通过代码静态分析确保 `LoginResult` 的闭环处理，特别是针对验证码失败后的自动刷新逻辑。
- **编译检查**：确保所有 Compose 组件与 Ktor 调用的参数签名一致。
- **UI 预览**：验证码弹窗已正确集成至“账号管理”流程中。

> [!TIP]
> 如果在同步课表时提示“需要验证码”，用户只需前往设置页面重新进行一次“修改账号”操作，通过验证码校验后，会话将自动更新，后续同步将恢复正常。
