 package me.kairos.schedsync.utils

import android.os.Build
import me.kairos.schedsync.data.type.DeviceBrand

/**
 * 设备品牌识别工具类。
 */
object DeviceUtils {
    /**
     * 获取当前系统设备的品牌枚举类型。
     *
     * @return [DeviceBrand] 设备品牌
     */
    fun current(): DeviceBrand {
        return when (Build.MANUFACTURER.lowercase()) {
            "samsung" -> DeviceBrand.SAMSUNG
            "xiaomi" -> DeviceBrand.XIAOMI
            "huawei" -> DeviceBrand.HUAWEI
            "oppo" -> DeviceBrand.OPPO
            "vivo" -> DeviceBrand.VIVO
            "google" -> DeviceBrand.GOOGLE
            "oneplus" -> DeviceBrand.ONEPLUS
            "iqoo" -> DeviceBrand.IQOO
            else -> DeviceBrand.OTHER
        }
    }
}
