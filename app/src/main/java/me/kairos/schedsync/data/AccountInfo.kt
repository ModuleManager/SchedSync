package me.kairos.schedsync.data

/**
 * 账号与学期基础配置数据模型。
 *
 * @property userId 用户学号
 * @property password 密码（明文形式用于加密传输，本地存储采用加密加密首选项）
 * @property academicYear 目标学年（例如 "2026"）
 * @property semester 目标学期（例如 "1" 或 "2"）
 */
data class AccountInfo(
    val userId: String = "",
    val password: String = "",
    val academicYear: String = "2026",
    val semester: String = "1"
)
