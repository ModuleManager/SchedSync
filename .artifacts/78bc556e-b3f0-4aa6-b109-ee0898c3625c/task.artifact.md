# 验证码修复与 Google Material Design 弹窗任务列表

- [x] `[x]` **第一阶段：API 层重构 (ZengFangAPI.kt)**
    - [x] 定义 `LoginResult` 密封接口 (Success, CaptchaRequired, Failure)
    - [x] 重构 `login` 方法：实现验证码检测与图片字节流获取逻辑
    - [x] 增强 `login` 方法：支持 `yzm` 参数的提交，并复用会话与 Token
- [x] `[x]` **第二阶段：ViewModel 层适配 (SettingsViewModel.kt)**
    - [x] 增加验证码相关的 StateFlow (图片数据、弹窗开关、输入文本)
    - [x] 修改 `verifyAndSaveAccount` 以处理 `CaptchaRequired` 结果并开启弹窗
    - [x] 修改 `fetchAndSaveSchedule` 支持登录过程中的验证码处理
    - [x] 提供 `refreshCaptcha` 和 `submitLoginWithCaptcha` 辅助方法
- [x] `[x]` **第三阶段：UI 层弹窗实现 (SettingTab.kt)**
    - [x] 实现 Material 3 风格的 `CaptchaDialog` 组件
    - [x] 在 `AccountManagementSection` 中集成验证码弹窗的显示逻辑
- [x] `[x]` **第四阶段：验证与测试**
    - [x] 编译并运行项目
    - [x] 模拟触发验证码场景，验证逻辑闭环与 UI 表现
