package com.judokit.android.api.model.request

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration
import java.util.TimeZone

private const val AVAILABLE = "available"
private const val UNAVAILABLE = "unavailable"

data class DeviceData(
    val available: Map<String, String>,
    val unavailable: Map<String, String>
)

sealed class Identifiers {
    enum class Common {
        //Platform
        C001,

        //Device Model
        C002,

        //OS Name
        C003,

        //OS Version
        C004,

        //Locale
        C005,

        //Time Zone
        C006,

        //Advertising ID
        C007,

        //Screen Resolution
        C008,

        //Device Name
        C009,

        //IP Address
        C010,

        //Latitude
        C011,

        //Longitude
        C012,

        //Application Package Name
        C013,

        //SDK App ID
        C014,

        //SDK Version
        C015;

        companion object {
            fun getParameters(context: Context): MutableMap<String, List<Pair<Common, String>>> {
                val parameters = mutableMapOf<String, List<Pair<Common, String>>>()
                val available = mutableListOf<Pair<Common, String>>()
                val unavailable = mutableListOf<Pair<Common, String>>()
                val location = getLastKnownLocation(context)

                available.add(C001 to "Android")
                available.add(C002 to Build.MODEL)
                available.add(C003 to Build.VERSION.RELEASE)
                available.add(C004 to Build.VERSION.SDK_INT.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    available.add(C005 to context.resources.configuration.locales[0].toString())
                } else {
                    unavailable.add(C005 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                available.add(C006 to TimeZone.getDefault().displayName)
                //TODO: Identifiers.C007
                val metrics = DisplayMetrics()
                available.add(C008 to "${metrics.widthPixels}x${metrics.heightPixels}")

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    available.add(C009 to BluetoothAdapter.getDefaultAdapter().name)
                } else {
                    unavailable.add(C009 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.INTERNET
                    ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getIPAddress(available, unavailable)
                } else {
                    unavailable.add(C010 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                val latitude = location?.latitude
                if (latitude != null) {
                    available.add(C011 to latitude.toString())
                } else {
                    unavailable.add(C011 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                val longitude = location?.longitude
                if (longitude != null) {
                    available.add(C012 to longitude.toString())
                } else {
                    unavailable.add(C012 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                available.add(
                    C013 to context.applicationContext.packageManager.getApplicationInfo(
                        context.packageName,
                        0
                    ).packageName
                )
                //TODO: Identifiers.C014
                //TODO: Identifiers.C015
                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable

                return parameters
            }
        }
    }

    enum class Telephony {
        //DeviceId
        A001,

        //SubscriberId
        A002,

        //IMEI/SV
        A003,

        //Group Identifier Level1
        A004,

        //Line1 Number
        A005,

        //MmsUAProfUrl
        A006,

        //MmsUserAgent
        A007,

        //NetworkCountryIso
        A008,

        //NetworkOperator
        A009,

        //NetworkOperatorName
        A010,

        //NetworkType
        A011,

        //PhoneCount
        A012,

        //PhoneType
        A013,

        //SimCountryIso
        A014,

        //SimOperator
        A015,

        //SimOperatorName
        A016,

        //SimSerialNumber
        A017,

        //SimState
        A018,

        //VoiceMailAlphaTag
        A019,

        //VoiceMailNumber
        A020,

        //hasIccCard
        A021,

        //isHearingAidCompatibilitySupported
        A022,

        //isNetworkRoaming
        A023,

        //isSmsCapable
        A024,

        //isTtyModeSupported
        A025,

        //isVoiceCapable
        A026,

        //isWorldPhone
        A027;

        companion object {
            @SuppressLint("MissingPermission")
            fun getParameters(context: Context): MutableMap<String, List<Pair<Telephony, String>>> {
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                val smsPermission: Boolean
                val readPhoneStatePermission: Boolean
                val readPhoneNumbersPermission: Boolean
                val hasCarrierPrivileges: Boolean =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && telephonyManager.hasCarrierPrivileges()
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    smsPermission = PermissionChecker.checkSelfPermission(
                        context,
                        Manifest.permission.SEND_SMS
                    ) == PermissionChecker.PERMISSION_GRANTED
                    readPhoneStatePermission = PermissionChecker.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PermissionChecker.PERMISSION_GRANTED
                    readPhoneNumbersPermission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            PermissionChecker.checkSelfPermission(
                                context,
                                Manifest.permission.READ_PHONE_NUMBERS
                            ) == PermissionChecker.PERMISSION_GRANTED
                        } else {
                            true
                        }
                } else {
                    smsPermission = true
                    readPhoneStatePermission = true
                    readPhoneNumbersPermission = true
                }

                val parameters = mutableMapOf<String, List<Pair<Telephony, String>>>()
                val available = mutableListOf<Pair<Telephony, String>>()
                val unavailable = mutableListOf<Pair<Telephony, String>>()
                getDeviceId(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                getSubscriberId(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                getDeviceSoftwareInformation(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                getGroupIdLevel1(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                getLine1Number(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    readPhoneNumbersPermission,
                    smsPermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                getMmsUAProfUrl(available, unavailable, telephonyManager)
                getMmsUserAgent(available, unavailable, telephonyManager)
                available.add(A008 to telephonyManager.networkCountryIso)
                available.add(A009 to telephonyManager.networkOperator)
                available.add(A010 to telephonyManager.networkOperatorName)
                available.add(A011 to telephonyManager.networkType.toString())
                getPhoneCount(available, unavailable, telephonyManager)
                available.add(A013 to telephonyManager.phoneType.toString())
                available.add(A014 to telephonyManager.simCountryIso)
                getSimOperator(available, unavailable, telephonyManager)
                getSimOperatorName(available, unavailable, telephonyManager)
                getSimSerialNumber(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                available.add(A018 to telephonyManager.simState.toString())
                if (readPhoneStatePermission || hasCarrierPrivileges) {
                    try {
                        available.add(A019 to telephonyManager.voiceMailAlphaTag)
                    } catch (e: SecurityException) {
                        available.add(A019 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    }
                } else {
                    available.add(A019 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                getVoiceMailNumber(
                    available,
                    unavailable,
                    readPhoneStatePermission,
                    hasCarrierPrivileges,
                    telephonyManager
                )
                available.add(A021 to telephonyManager.hasIccCard().toString())
                isHearingAidCompatibilitySupported(available, unavailable, telephonyManager)
                available.add(A023 to telephonyManager.isNetworkRoaming.toString())
                isSmsCapable(available, unavailable, telephonyManager)
                isTtySupported(available, unavailable, readPhoneStatePermission, context)
                isVoiceCapable(available, unavailable, telephonyManager)
                isWorldPhone(available, unavailable, telephonyManager)

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }
}

//enum class Identifiers {
//Platform
//    C001,
//
//    //Device Model
//    C002,
//
//    //OS Name
//    C003,
//
//    //OS Version
//    C004,
//
//    //Locale
//    C005,
//
//    //Time Zone
//    C006,
//
//    //Advertising ID
////    C007,
//
//    //Screen Resolution
//    C008,
//
//    //Device Name
//    C009,
//
//    //IP Address
//    C010,
//
//    //Latitude
//    C011,
//
//    //Longitude
//    C012,
//
//    //Application Package Name
//    C013,

//SDK App ID
//    C014,

//SDK Version
//    C015,
//    A028,
//    A029,
//    A030,
//    A031,
//    A032,
//    A033,
//    A034,
//    A035,
//    A036,
//    A037,
//    A038,
//    A039,
//    A040,
//    A041,
//    A042,
//    A043,
//    A044,
//    A045,
//    A046,
//    A047,
//    A048,
//    A049,
//    A050,
//    A051,
//    A052,
//    A053,
//    A054,
//    A055,
//    A056,
//    A057,
//    A058,
//    A059,
//    A060,
//    A061,
//    A062,
//    A063,
//    A064,
//    A065,
//    A066,
//    A067,
//    A068,
//    A069,
//    A070,
//    A071,
//    A072,
//    A073,
//    A074,
//    A075,
//    A076,
//    A077,
//    A078,
//    A079,
//    A080,
//    A081,
//    A082,
//    A083,
//    A084,
//    A085,
//    A086,
//    A087,
//    A088,
//    A089,
//    A090,
//    A091,
//    A092,
//    A093,
//    A094,
//    A095,
//    A096,
//    A097,
//    A098,
//    A099,
//    A100,
//    A101,
//    A102,
//    A103,
//    A104,
//    A105,
//    A106,
//    A107,
//    A108,
//    A109,
//    A110,
//    A111,
//    A112,
//    A113,
//    A114,
//    A115,
//    A116,
//    A117,
//    A118,
//    A119,
//    A120,
//    A121,
//    A122,
//    A123,
//    A124,
//    A125,
//    A126,
//    A127,
//    A128,
//    A129,
//    A130,
//    A131,
//    A132,
//    A133,
//    A134,
//    A135,
//    A136
//}
//
//@SuppressLint("MissingPermission")
//fun Identifiers.getParameter(
//    context: Context,
//    smsPermission: Boolean,
//    readPhoneStatePermission: Boolean,
//    readPhoneNumbersPermission: Boolean,
//    hasCarrierPrivileges: Boolean,
//    telephonyManager: TelephonyManager,
//    location: Location?
//): Pair<String, Pair<String, String>> {
//    return when (this) {
//        Identifiers.C001 -> AVAILABLE to (name to "Android")
//        Identifiers.C002 -> AVAILABLE to (name to Build.MODEL)
//        Identifiers.C003 -> AVAILABLE to (name to Build.VERSION.RELEASE)
//        Identifiers.C004 -> AVAILABLE to (name to Build.VERSION.SDK_INT.toString())
//        Identifiers.C005 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            AVAILABLE to (name to context.resources.configuration.locales[0].toString())
//        } else {
//            UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//        }
//        Identifiers.C006 -> AVAILABLE to (name to TimeZone.getDefault().displayName)
////        Identifiers.C007 -> TODO()
//        Identifiers.C008 -> {
//            val metrics = DisplayMetrics()
//            AVAILABLE to (name to "${metrics.widthPixels}x${metrics.heightPixels}")
//        }
//        Identifiers.C009 -> if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            AVAILABLE to (name to BluetoothAdapter.getDefaultAdapter().name)
//        } else {
//            UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//        }
//        Identifiers.C010 -> if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.INTERNET
//            ) == PackageManager.PERMISSION_GRANTED
//            && ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_NETWORK_STATE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            getIPAddress(name)
//        } else {
//            UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//        }
//        Identifiers.C011 -> {
//            val latitude = location?.latitude
//            if (latitude != null) {
//                AVAILABLE to (name to latitude.toString())
//            } else {
//                UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//            }
//        }
//        Identifiers.C012 -> {
//            val longitude = location?.longitude
//            if (longitude != null) {
//                AVAILABLE to (name to longitude.toString())
//            } else {
//                UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//            }
//        }
//        Identifiers.C013 -> AVAILABLE to (name to context.applicationContext.packageManager.getApplicationInfo(
//            context.packageName,
//            0
//        ).packageName)
////        Identifiers.C014 -> TODO()
////        Identifiers.C015 -> TODO()
//        Identifiers.A001 -> getDeviceId(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A002 -> getSubscriberId(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A003 -> getDeviceSoftwareInformation(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A004 -> getGroupIdLevel1(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A005 -> getLine1Number(
//            name,
//            readPhoneStatePermission,
//            readPhoneNumbersPermission,
//            smsPermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A006 -> getMmsUAProfUrl(name, telephonyManager)
//        Identifiers.A007 -> getMmsUserAgent(name, telephonyManager)
//        Identifiers.A008 -> AVAILABLE to (name to telephonyManager.networkCountryIso)
//        Identifiers.A009 -> AVAILABLE to (name to telephonyManager.networkOperator)
//        Identifiers.A010 -> AVAILABLE to (name to telephonyManager.networkOperatorName)
//        Identifiers.A011 -> AVAILABLE to (name to telephonyManager.networkType.toString())
//        Identifiers.A012 -> getPhoneCount(name, telephonyManager)
//        Identifiers.A013 -> AVAILABLE to (name to telephonyManager.phoneType.toString())
//        Identifiers.A014 -> AVAILABLE to (name to telephonyManager.simCountryIso)
//        Identifiers.A015 -> getSimOperator(name, telephonyManager)
//        Identifiers.A016 -> getSimOperatorName(name, telephonyManager)
//        Identifiers.A017 -> getSimSerialNumber(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A018 -> AVAILABLE to (name to telephonyManager.simState.toString())
//        Identifiers.A019 -> if (readPhoneStatePermission || hasCarrierPrivileges) {
//            try {
//                AVAILABLE to (name to telephonyManager.voiceMailAlphaTag)
//            } catch (e: SecurityException) {
//                UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//            }
//        } else {
//            UNAVAILABLE to (name to DeviceParameterUnavailabilityReasonCodes.RE03.name)
//        }
//        Identifiers.A020 -> getVoiceMailNumber(
//            name,
//            readPhoneStatePermission,
//            hasCarrierPrivileges,
//            telephonyManager
//        )
//        Identifiers.A021 -> AVAILABLE to (name to telephonyManager.hasIccCard().toString())
//        Identifiers.A022 -> isHearingAidCompatibilitySupported(name, telephonyManager)
//        Identifiers.A023 -> AVAILABLE to (name to telephonyManager.isNetworkRoaming.toString())
//        Identifiers.A024 -> isSmsCapable(name, telephonyManager)
//        Identifiers.A025 -> isTtySupported(name, readPhoneStatePermission, context)
//        Identifiers.A026 -> isVoiceCapable(name, telephonyManager)
//        Identifiers.A027 -> isWorldPhone(name, telephonyManager)
//    }
//}

@SuppressLint("MissingPermission")
fun getDeviceData(context: Context): DeviceData {
    val available = mutableMapOf<String, String>()
    val unavailable = mutableMapOf<String, String>()
    val commonAvailableParameters =
        Identifiers.Common.getParameters(context).filterKeys { it == AVAILABLE }.values
    val commonUnavailableParameters =
        Identifiers.Common.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val telephonyAvailableParameters =
        Identifiers.Telephony.getParameters(context).filterKeys { it == AVAILABLE }.values
    val telephonyUnavailableParameters =
        Identifiers.Telephony.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    commonAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Common, String> ->
            available[pair.first.name] = pair.second
        }
    }
    commonUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Common, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    telephonyAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Telephony, String> ->
            available[pair.first.name] = pair.second
        }
    }
    telephonyUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Telephony, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }

//    val location = getLastKnownLocation(context)
//
//    val telephonyManager =
//        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//
//    val smsPermission: Boolean
//    val readPhoneStatePermission: Boolean
//    val readPhoneNumbersPermission: Boolean
//    val hasCarrierPrivileges: Boolean =
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && telephonyManager.hasCarrierPrivileges()
//    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
//        smsPermission = PermissionChecker.checkSelfPermission(
//            context,
//            Manifest.permission.SEND_SMS
//        ) == PermissionChecker.PERMISSION_GRANTED
//        readPhoneStatePermission = PermissionChecker.checkSelfPermission(
//            context,
//            Manifest.permission.READ_PHONE_STATE
//        ) == PermissionChecker.PERMISSION_GRANTED
//        readPhoneNumbersPermission =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                PermissionChecker.checkSelfPermission(
//                    context,
//                    Manifest.permission.READ_PHONE_NUMBERS
//                ) == PermissionChecker.PERMISSION_GRANTED
//            } else {
//                true
//            }
//    } else {
//        smsPermission = true
//        readPhoneStatePermission = true
//        readPhoneNumbersPermission = true
//    }
//
//
//    val available = mutableMapOf<String, String>()
//    val unavailable = mutableMapOf<String, String>()
//    Identifiers.values().forEach { identifiers ->
//        val availablePair =
//            identifiers.getParameter(
//                context,
//                smsPermission,
//                readPhoneStatePermission,
//                readPhoneNumbersPermission,
//                hasCarrierPrivileges,
//                telephonyManager,
//                location
//            ).takeIf { it.first == AVAILABLE }?.second
//        val unavailablePair =
//            identifiers.getParameter(
//                context,
//                smsPermission,
//                readPhoneStatePermission,
//                readPhoneNumbersPermission,
//                hasCarrierPrivileges,
//                telephonyManager,
//                location
//            ).takeIf { it.first == UNAVAILABLE }?.second
//
//        if (availablePair != null) {
//            val availableKey = availablePair.first
//            val availableValue = availablePair.second
//            available[availableKey] = availableValue
//        } else if (unavailablePair != null) {
//            val unavailableKey = unavailablePair.first
//            val unavailableValue = unavailablePair.second
//            unavailable[unavailableKey] = unavailableValue
//        }
//    }

    return DeviceData(available, unavailable)
}

private fun isWorldPhone(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            available.add(Identifiers.Telephony.A027 to telephonyManager.isWorldPhone.toString())
        } catch (e: SecurityException) {
            unavailable.add(Identifiers.Telephony.A027 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
        }
    } else {
        unavailable.add(Identifiers.Telephony.A027 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}


private fun isVoiceCapable(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
    available.add(Identifiers.Telephony.A026 to telephonyManager.isVoiceCapable.toString())
} else {
    unavailable.add(Identifiers.Telephony.A026 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
}


@SuppressLint("MissingPermission")
private fun isTtySupported(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    context: Context
) = if (readPhoneStatePermission) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val telecomManager =
            context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        available.add(Identifiers.Telephony.A025 to telecomManager.isTtySupported.toString())
    } else {
        unavailable.add(Identifiers.Telephony.A025 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
} else {
    unavailable.add(Identifiers.Telephony.A025 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
}


private fun isSmsCapable(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    available.add(Identifiers.Telephony.A024 to telephonyManager.isSmsCapable.toString())
} else {
    unavailable.add(Identifiers.Telephony.A024 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
}


private fun isHearingAidCompatibilitySupported(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        available.add(Identifiers.Telephony.A022 to telephonyManager.isHearingAidCompatibilitySupported.toString())
    } else {
        unavailable.add(Identifiers.Telephony.A022 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}


@SuppressLint("MissingPermission")
private fun getVoiceMailNumber(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) = if (readPhoneStatePermission || hasCarrierPrivileges) {
    val voiceMailNumber = telephonyManager.voiceMailNumber
    if (voiceMailNumber != null) {
        available.add(Identifiers.Telephony.A020 to voiceMailNumber)
    } else {
        unavailable.add(Identifiers.Telephony.A020 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
    }
} else {
    unavailable.add(Identifiers.Telephony.A020 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
}


@SuppressLint("MissingPermission")
private fun getSimSerialNumber(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) = if (readPhoneStatePermission || hasCarrierPrivileges) {
    try {
        val simSerialNumber = telephonyManager.simSerialNumber
        if (simSerialNumber != null) {
            available.add(Identifiers.Telephony.A017 to simSerialNumber)
        } else {
            unavailable.add(Identifiers.Telephony.A017 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
        }
    } catch (e: SecurityException) {
        unavailable.add(Identifiers.Telephony.A017 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
    }
} else {
    unavailable.add(Identifiers.Telephony.A017 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
}

private fun getSimOperator(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) = if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
    available.add(Identifiers.Telephony.A015 to telephonyManager.simOperator)
} else {
    unavailable.add(Identifiers.Telephony.A015 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
}

private fun getSimOperatorName(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) = if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
    available.add(Identifiers.Telephony.A016 to telephonyManager.simOperatorName)
} else {
    available.add(Identifiers.Telephony.A016 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
}


private fun getPhoneCount(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        available.add(Identifiers.Telephony.A012 to telephonyManager.phoneCount.toString())
    } else {
        unavailable.add(Identifiers.Telephony.A012 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
    }
}

private fun getMmsUserAgent(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) {
    val mmsUserAgent = telephonyManager.mmsUserAgent
    if (mmsUserAgent != null) {
        available.add(Identifiers.Telephony.A007 to mmsUserAgent)
    } else {
        unavailable.add(Identifiers.Telephony.A007 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
    }
}

private fun getMmsUAProfUrl(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    telephonyManager: TelephonyManager
) {
    val mmsUAProfUrl = telephonyManager.mmsUAProfUrl
    if (mmsUAProfUrl != null) {
        available.add(Identifiers.Telephony.A006 to mmsUAProfUrl)
    } else {
        unavailable.add(Identifiers.Telephony.A006 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
    }
}

@SuppressLint("MissingPermission")
private fun getLine1Number(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    readPhoneNumbersPermission: Boolean,
    smsPermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) {
    if ((readPhoneStatePermission && readPhoneNumbersPermission && smsPermission) || hasCarrierPrivileges) {
        val line1Number = telephonyManager.line1Number
        if (line1Number != null) {
            available.add(Identifiers.Telephony.A005 to line1Number)
        } else {
            unavailable.add(Identifiers.Telephony.A005 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
        }
    } else {
        unavailable.add(Identifiers.Telephony.A005 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}


@SuppressLint("MissingPermission")
private fun getGroupIdLevel1(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) {
    if (readPhoneStatePermission || hasCarrierPrivileges) {
        val groupIdLevel1 = telephonyManager.groupIdLevel1
        if (groupIdLevel1 != null) {
            available.add(Identifiers.Telephony.A004 to groupIdLevel1)
        } else {
            unavailable.add(Identifiers.Telephony.A004 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
        }
    } else {
        unavailable.add(Identifiers.Telephony.A004 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}


@SuppressLint("MissingPermission")
private fun getDeviceSoftwareInformation(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) = if (readPhoneStatePermission || hasCarrierPrivileges) {
    val deviceSoftwareVersion = telephonyManager.deviceSoftwareVersion
    if (deviceSoftwareVersion != null) {
        available.add(Identifiers.Telephony.A003 to deviceSoftwareVersion)
    } else {
        unavailable.add(Identifiers.Telephony.A003 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
    }
} else {
    unavailable.add(Identifiers.Telephony.A003 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
}


@SuppressLint("MissingPermission")
private fun getSubscriberId(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) {
    if (readPhoneStatePermission || hasCarrierPrivileges) {
        try {
            val subscriberId = telephonyManager.subscriberId
            if (subscriberId != null) {
                available.add(Identifiers.Telephony.A002 to subscriberId)
            } else {
                unavailable.add(Identifiers.Telephony.A002 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
            }
        } catch (e: SecurityException) {
            unavailable.add(Identifiers.Telephony.A002 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
        }
    } else {
        unavailable.add(Identifiers.Telephony.A002 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}

@SuppressLint("MissingPermission")
private fun getDeviceId(
    available: MutableList<Pair<Identifiers.Telephony, String>>,
    unavailable: MutableList<Pair<Identifiers.Telephony, String>>,
    readPhoneStatePermission: Boolean,
    hasCarrierPrivileges: Boolean,
    telephonyManager: TelephonyManager
) {
    if (readPhoneStatePermission || hasCarrierPrivileges) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    val meid = telephonyManager.meid
                    if (meid != null) {
                        available.add(Identifiers.Telephony.A001 to meid)
                    } else {
                        unavailable.add(Identifiers.Telephony.A001 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                } else {
                    val imei = telephonyManager.imei
                    if (imei != null) {
                        available.add(Identifiers.Telephony.A001 to imei)
                    } else {
                        unavailable.add(Identifiers.Telephony.A001 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                }
            } catch (e: SecurityException) {
                unavailable.add(Identifiers.Telephony.A001 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
            }
        } else {
            unavailable.add(Identifiers.Telephony.A001 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
        }
    } else {
        unavailable.add(Identifiers.Telephony.A001 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
    }
}

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(context: Context): Location? {
    val manager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = manager.getProviders(true)
    var lastKnownLocation: Location? = null
    for (provider in providers) {
        val accessFineLocation = PermissionChecker.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
        val accessCoarseLocation = PermissionChecker.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

        if (accessCoarseLocation || accessFineLocation) {
            if (lastKnownLocation == null) {
                lastKnownLocation = manager.getLastKnownLocation(provider)
            } else {
                val location =
                    manager.getLastKnownLocation(provider)
                if (location != null && location.time > lastKnownLocation.time) {
                    lastKnownLocation = location
                }
            }
        }
    }
    return lastKnownLocation
}

private fun getIPAddress(
    available: MutableList<Pair<Identifiers.Common, String>>,
    unavailable: MutableList<Pair<Identifiers.Common, String>>
) {
    try {
        val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val networkInterface: NetworkInterface = en.nextElement()
            val enumIpAddr: Enumeration<InetAddress> = networkInterface.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress: InetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    available.add(Identifiers.Common.C010 to inetAddress.getHostAddress())
                }
            }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }
    unavailable.add(Identifiers.Common.C010 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
}

enum class DeviceParameterUnavailabilityReasonCodes {
    //Market or regional restriction on the parameter.
    RE01,

    //Platform version does not support the parameter or the parameter has been deprecated.
    RE02,

    //Parameter collection not possible without prompting the user for permission.
    RE03,

    //Parameter value returned is null or blank.
    RE04

}
