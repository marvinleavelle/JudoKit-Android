package com.judokit.android.api.model.request

import android.content.Context
import com.google.gson.annotations.SerializedName

data class ThreeDSDeviceData(
    @SerializedName("DV")
    val deviceVersion: String = "1.4",
    @SerializedName("DD")
    var deviceData: Map<String, String>,
    @SerializedName("DPNA")
    var deviceParameterUnavailabilityReason: Map<String, String>,
    @SerializedName("SW")
    var securityWarnings: List<String>
) {
    class Builder {
        fun build(context: Context): ThreeDSDeviceData {
            val data = getDeviceData(context)
            val available = data.available
            val unavailable = data.unavailable
            val securityWarnings = data.securityWarnings

            return ThreeDSDeviceData(
                deviceData = available,
                deviceParameterUnavailabilityReason = unavailable,
                securityWarnings = securityWarnings
            )
        }
    }
}