# 验证码修复与 Google Material Design 弹窗设计实现方案

针对当前正方教务系统登录可能遇到的验证码限制问题，我们将参考提供的 Python 实现思路，对 Android 客户端的登录逻辑进行升级。同时，使用 Jetpack Compose (Material 3) 设计并实现一个优雅的验证码输入弹窗，使其完全符合 Google MD 设计规范。

## 需求分析与解决思路

根据 `mian.py` 的处理逻辑，正方教务系统的验证码流如下：
1. **首次请求登录页**：获取 `csrftoken`，并检查页面中是否存在 `input#yzm` 验证码输入框。
2. **判断是否需要验证码**：
   - 若不存在：则按原逻辑直接加密密码并发送登录请求。
   - 若存在：则请求 `/kaptcha` 接口下载验证码图片数据，并向用户展示。
3. **二次提交请求**：在维持相同 Cookie 会话与 `csrftoken` 的前提下，将用户输入的验证码作为 `yzm` 参数随表单一同提交。

## 拟更改的内容

### 1. [API 层] `ZengFangAPI.kt`
- 定义 `LoginResult` 密封接口（Success, CaptchaRequired, Failure），更清晰地表达登录状态。
- 改造 `login` 函数，每次请求时实时检测是否包含验证码输入框，若包含则下载验证码图片字节流并返回 `LoginResult.CaptchaRequired`。
- 支持在传入 `captcha` 参数时直接进行带验证码的表单提交，避免二次刷新页面导致会话和 Token 失效。

### 2. [ViewModel 层] `SettingsViewModel.kt`
- 增加控制验证码弹窗显示、验证码图片字节流、当前输入的验证码文本等状态变量。
- 重构 `verifyAndSaveAccount` 和 `fetchAndSaveSchedule` 逻辑，处理 `LoginResult`。当收到 `CaptchaRequired` 时，触发验证码弹窗状态。

### 3. [UI 层] `SettingTab.kt`
- 遵循 Material 3 设计规范，编写一个美观、规范的验证码对话框。
- 对话框包含：标题、清晰的验证码图片展示（带点击刷新功能）、圆角 `OutlinedTextField` 输入框、符合 MD 规范的取消/确认按钮以及错误信息提示。

---

## Proposed Changes

### me.kairos.myapplication.api

#### [MODIFY] [ZengFangAPI.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/myapplication/api/ZengFangAPI.kt)
- 引入 `LoginResult` 密封接口。
- 更新 `login` 核心函数，支持验证码检测、下载与携带提交。

### me.kairos.myapplication.setting

#### [MODIFY] [SettingsViewModel.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/myapplication/setting/SettingsViewModel.kt)
- 引入验证码相关状态变量：`captchaImageBytes`, `isShowingCaptchaDialog` 等。
- 升级账号校验与课表同步函数以适配新的 `LoginResult`。

### me.kairos.myapplication.ui

#### [MODIFY] [SettingTab.kt](file:///E:/Project/MyApplication/app/src/main/java/me/kairos/myapplication/ui/SettingTab.kt)
- 在 `AccountManagementSection` 中集成 Material 3 风格的验证码弹窗。

---

## Verification Plan

### 自动化与编译检查
- 运行 Gradle 编译确保新增的密封接口与 Compose 弹窗无任何语法或构筑错误。

### 手动验证
1. 打开设置页，点击“绑定账号”或“修改账号”。
2. 输入正确的学号与密码，触发教务系统的登录逻辑。
3. 若教务系统触发验证码保护，检查是否弹出了符合 Material 3 设计规范的验证码输入框。
4. 验证输入验证码后能否正常登录成功，并进入下一步班级选择。
