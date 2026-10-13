package me.kairos.schedsync.api

/**
 * 教务系统登录结果密封接口。
 *
 * 表达正方教务系统登录过程中的三种可能结果状态。
 */
sealed interface LoginResult {
    /**
     * 登录成功。
     */
    data object Success : LoginResult

    /**
     * 需要输入图形验证码。
     *
     * @property captchaBytes 验证码图片的字节数组
     */
    data class CaptchaRequired(val captchaBytes: ByteArray) : LoginResult {
        /**
         * 比较验证码字节内容是否完全一致。
         *
         * @param other 待比较的对象
         * @return 字节数组内容相同则返回 true，否则返回 false
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CaptchaRequired

            return captchaBytes.contentEquals(other.captchaBytes)
        }

        /**
         * 计算基于验证码字节内容的哈希值。
         *
         * @return 哈希码数值
         */
        override fun hashCode(): Int {
            return captchaBytes.contentHashCode()
        }
    }

    /**
     * 登录失败。
     *
     * @property message 登录失败原因提示信息
     */
    data class Failure(val message: String) : LoginResult
}
