package me.kairos.schedsync.data.json.class_schedule_json

import com.google.gson.annotations.SerializedName

/**
 * 接口返回的用户模型。
 *
 * @property monitor 是否为班长/班委
 * @property roleCount 角色数量
 * @property roleKeys 角色键
 * @property roleValues 角色值
 * @property status 状态码
 * @property usable 是否可用
 */
data class UserModel(
    @SerializedName("monitor") val monitor: Boolean? = false,
    @SerializedName("roleCount") val roleCount: Int? = 0,
    @SerializedName("roleKeys") val roleKeys: String? = null,
    @SerializedName("roleValues") val roleValues: String? = null,
    @SerializedName("status") val status: Int? = 0,
    @SerializedName("usable") val usable: Boolean? = false
)
