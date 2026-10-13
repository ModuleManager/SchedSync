# SchedSync

![Android API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-orange.svg)

SchedSync 是一个基于 Android 平台开发的课表同步与管理应用，主要面向高校学生使用。

项目通过解析高校教务系统数据，实现课程信息同步、课表查看以及课程提醒等功能。

目前项目主要适配正方教务系统，使用 Kotlin 开发，并采用 Jetpack Compose 构建用户界面。

本项目主要用于学习和实践 Android 开发中的网络请求、数据解析、UI 架构以及本地数据管理等内容。

---

## 功能

### 教务系统登录与同步

- 支持正方教务系统登录
- 处理登录过程中的验证码、Token、Cookie 等信息
- 获取并解析课程数据
- 本地保存账号相关信息

### 课表管理

- 查看个人课表
- 查看课程详细信息
- 展示课程时间、地点、教师等信息
- 支持课程状态显示

### 界面与交互

- 使用 Jetpack Compose 构建 UI
- 使用 Material 3 组件
- 支持深色模式
- 支持 Edge-to-Edge 布局
- 提供基础动画和状态切换效果

### 课程提醒

- 使用 Android 前台服务实现课程状态更新
- 通过系统通知展示当前课程和下一节课程信息

### 数据安全

- 使用 AndroidX Security Crypto
- 使用 EncryptedSharedPreferences 保存敏感数据

---

## 技术栈

### 开发语言

- Kotlin 2.0+

### UI

- Jetpack Compose
- Material 3

### 架构

- MVVM
- ViewModel
- Kotlin Coroutines
- Flow / StateFlow

### 网络与数据解析

- Ktor Client
- Jsoup
- Gson

### Android 技术

- AndroidX Security Crypto
- Foreground Service
- Notification API

### 开发环境

- Minimum SDK: API 29 (Android 10)
- Compile SDK: API 37
- JDK 11+

---

## 项目结构

```
me.kairos.schedsync/

├── api/
│   └── 教务系统网络请求、登录及数据获取

├── data/
│   ├── json/
│   │   └── 数据解析模型
│   ├── source/
│   │   └── 数据来源管理
│   └── type/
│       └── 枚举及数据类型定义

├── notification/
│   └── 通知管理相关代码

├── service/
│   └── 前台服务及后台更新逻辑

├── setting/
│   └── 设置与配置管理

├── ui/
│   ├── 页面组件
│   └── theme/
│       └── Material 3 主题配置

└── utils/
    └── 通用工具类
```

---

## 开始使用

### 环境要求

- Android Studio Ladybug / Jellyfish 或更高版本
- JDK 11+
- Android SDK 29+

### 构建项目

1. 克隆项目

```bash
git clone https://github.com/your-username/SchedSync.git
```

1. 使用 Android Studio 打开项目目录。

2. 等待 Gradle Sync 完成。

3. 连接 Android 设备或启动模拟器运行项目。

---

## 注意事项

由于不同高校使用的教务系统可能存在差异，目前项目主要针对正方教务系统进行开发。

如果教务系统接口或页面结构发生变化，相关解析逻辑可能需要调整。

---

## 免责声明

本项目仅用于学习、研究和技术交流。

请遵守学校相关规定以及网络安全要求。

开发者不对因使用本项目造成的数据异常、账号问题等情况承担责任。

---

## License

This project is licensed under the MIT License.