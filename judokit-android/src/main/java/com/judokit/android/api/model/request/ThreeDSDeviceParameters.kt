package com.judokit.android.api.model.request

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import android.provider.Settings.Global
import android.provider.Settings.Secure
import android.provider.Settings.System
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.judokit.android.BuildConfig
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration
import java.util.Locale
import java.util.TimeZone

private const val AVAILABLE = "available"
private const val UNAVAILABLE = "unavailable"

data class DeviceData(
    val available: Map<String, String>,
    val unavailable: Map<String, String>,
    val securityWarnings: List<String>
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
                //TODO: C007
                unavailable.add(C007 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                val wm =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)
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
                //TODO: C014
                available.add(C015 to BuildConfig.VERSION_NAME)

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

    enum class Wifi {
        //Wifi - Mac Address
        A028,

        //BSSID
        A029,

        //SSID
        A030,

        //Network ID
        A031,

        //is5GHzBandSupported
        A032,

        //isDeviceToApRttSupported
        A033,

        //isEnhancedPowerReportingSupported
        A034,

        //isP2pSupported
        A035,

        //isPreferredNetworkOffloadSupported
        A036,

        //isScanAlwaysAvailable
        A037,

        //isTdlsSupported
        A038;

        companion object {
            @SuppressLint("MissingPermission")
            fun getParameters(context: Context): MutableMap<String, List<Pair<Wifi, String>>> {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiPermission = PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_WIFI_STATE
                ) == PermissionChecker.PERMISSION_GRANTED

                val parameters = mutableMapOf<String, List<Pair<Wifi, String>>>()
                val available = mutableListOf<Pair<Wifi, String>>()
                val unavailable = mutableListOf<Pair<Wifi, String>>()

                if (wifiPermission) {
                    try {
                        val connectionInfo = wifiManager.connectionInfo
                        available.add(A028 to connectionInfo.macAddress)
                        val bssid = connectionInfo.bssid
                        if (bssid != null) {
                            available.add(A029 to bssid)
                        } else {
                            unavailable.add(A029 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                        }
                        available.add(A030 to connectionInfo.ssid)
                        available.add(A031 to connectionInfo.networkId.toString())
                    } catch (e: Exception) {
                        unavailable.add(A028 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                        unavailable.add(A029 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                        unavailable.add(A030 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                        unavailable.add(A031 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        available.add(A032 to wifiManager.is5GHzBandSupported.toString())
                    } else {
                        unavailable.add(A032 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        available.add(
                            A033 to context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)
                                .toString()
                        )
                    } else {
                        unavailable.add(A033 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        available.add(A034 to wifiManager.isEnhancedPowerReportingSupported.toString())
                    } else {
                        unavailable.add(A034 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        available.add(A035 to wifiManager.isP2pSupported.toString())
                    } else {
                        unavailable.add(A035 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        available.add(A036 to wifiManager.isPreferredNetworkOffloadSupported.toString())
                    } else {
                        unavailable.add(A036 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    }
                    unavailable.add(A037 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        available.add(A038 to wifiManager.isTdlsSupported.toString())
                    } else {
                        unavailable.add(A038 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                } else {
                    unavailable.add(A028 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A029 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A030 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A031 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A032 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A033 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A034 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A035 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A036 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A037 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A038 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class Bluetooth {
        //Address
        A039,

        //BondedDevices
        A040,

        //isEnabled
        A041;

        companion object {
            @SuppressLint("MissingPermission")
            fun getParameters(context: Context): MutableMap<String, List<Pair<Bluetooth, String>>> {
                val bluetoothManager =
                    context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothPermission = PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) == PermissionChecker.PERMISSION_GRANTED

                val parameters = mutableMapOf<String, List<Pair<Bluetooth, String>>>()
                val available = mutableListOf<Pair<Bluetooth, String>>()
                val unavailable = mutableListOf<Pair<Bluetooth, String>>()

                if (bluetoothPermission) {
                    val adapter = bluetoothManager.adapter
                    available.add(A039 to adapter.address)
                    available.add(A040 to adapter.bondedDevices.toString())
                    available.add(A041 to adapter.isEnabled.toString())
                } else {
                    unavailable.add(A039 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A040 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    unavailable.add(A041 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class PhoneBuild {
        //BOARD
        A042,

        //BOOTLOADER
        A043,

        //BRAND
        A044,

        //DEVICE
        A045,

        //DISPLAY
        A046,

        //FINGERPRINT
        A047,

        //HARDWARE
        A048,

        //ID
        A049,

        //MANUFACTURER
        A050,

        //PRODUCT
        A051,

        //RADIO
        A052,

        //SERIAL
        A053,

        //SUPPORTED_32_BIT_ABIS
        A054,

        //SUPPORTED_64_BIT_ABIS
        A055,

        //TAGS
        A056,

        //TIME
        A057,

        //TYPE
        A058,

        //USER
        A059,

        //CODENAME
        A060,

        //INCREMENTAL
        A061,

        //PREVIEW_SDK_INT
        A062,

        //SDK_INT
        A063,

        //SECURITY_PATCH
        A064;

        companion object {
            @SuppressLint("MissingPermission")
            fun getParameters(context: Context): MutableMap<String, List<Pair<PhoneBuild, String>>> {
                val telephonyManager =
                    context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val hasCarrierPrivileges: Boolean =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && telephonyManager.hasCarrierPrivileges()
                val readPhoneStatePermission = PermissionChecker.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PermissionChecker.PERMISSION_GRANTED

                val parameters = mutableMapOf<String, List<Pair<PhoneBuild, String>>>()
                val available = mutableListOf<Pair<PhoneBuild, String>>()
                val unavailable = mutableListOf<Pair<PhoneBuild, String>>()

                available.add(A042 to Build.BOARD)
                available.add(A043 to Build.BOOTLOADER)
                available.add(A044 to Build.BRAND)
                available.add(A045 to Build.DEVICE)
                available.add(A046 to Build.DISPLAY)
                available.add(A047 to Build.FINGERPRINT)
                available.add(A048 to Build.HARDWARE)
                available.add(A049 to Build.ID)
                available.add(A050 to Build.MANUFACTURER)
                available.add(A051 to Build.PRODUCT)
                val radioVersion = Build.getRadioVersion()
                if (radioVersion != null) {
                    available.add(A052 to radioVersion)
                } else {
                    unavailable.add(A052 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (hasCarrierPrivileges || readPhoneStatePermission)) {
                        available.add(A053 to Build.getSerial())
                    } else {
                        unavailable.add(A053 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                } catch (e: Exception) {
                    unavailable.add(A053 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val supported32BitAbis = Build.SUPPORTED_32_BIT_ABIS
                    val supported64BitAbis = Build.SUPPORTED_32_BIT_ABIS
                    if (supported32BitAbis.isNotEmpty()) {
                        available.add(A054 to supported32BitAbis[0])
                    } else {
                        unavailable.add(A054 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                    if (supported64BitAbis.isNotEmpty()) {
                        available.add(A055 to supported64BitAbis[0])
                    } else {
                        unavailable.add(A055 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                }
                available.add(A056 to Build.TAGS)
                available.add(A057 to Build.TIME.toString())
                available.add(A058 to Build.TYPE)
                available.add(A059 to Build.USER)
                available.add(A060 to Build.VERSION.CODENAME)
                available.add(A061 to Build.VERSION.INCREMENTAL)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    available.add(A062 to Build.VERSION.PREVIEW_SDK_INT.toString())
                } else {
                    unavailable.add(A062 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                available.add(A063 to Build.VERSION.SDK_INT.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    available.add(A064 to Build.VERSION.SECURITY_PATCH)
                } else {
                    unavailable.add(A064 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class SettingsSecure {
        //ACCESSIBILITY_DISPLAY_INVERSION_ENABLED
        A065,

        //ACCESSIBILITY_ENABLED
        A066,

        //ACCESSIBILITY_SPEAK_PASSWORD
        A067,

        //ALLOWED_GEOLOCATION_ORIGINS
        A068,

        //ANDROID_ID
        A069,

        //DEFAULT_INPUT_METHOD
        A071,

        //ENABLED_ACCESSIBILITY_SERVICES
        A073,

        //ENABLED_INPUT_METHODS
        A074,

        //INPUT_METHOD_SELECTOR_VISIBILITY
        A075,

        //INSTALL_NON_MARKET_APPS
        A076,

        //LOCATION_MODE
        A077,

        //SKIP_FIRST_USE_HINTS
        A078,

        //SYS_PROP_SETTING_VERSION
        A079,

        //TTS_DEFAULT_PITCH
        A080,

        //TTS_DEFAULT_RATE
        A081,

        //TTS_DEFAULT_SYNTH
        A082,

        //TTS_ENABLED_PLUGINS
        A083;

        companion object {
            fun getParameters(context: Context): MutableMap<String, List<Pair<SettingsSecure, String>>> {

                val installPackagesPermission =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PermissionChecker.checkSelfPermission(
                        context,
                        Manifest.permission.REQUEST_INSTALL_PACKAGES
                    ) == PermissionChecker.PERMISSION_GRANTED

                val parameters = mutableMapOf<String, List<Pair<SettingsSecure, String>>>()
                val available = mutableListOf<Pair<SettingsSecure, String>>()
                val unavailable = mutableListOf<Pair<SettingsSecure, String>>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val accessibilityDisplayInversionEnabled = Secure.getString(
                        context.contentResolver,
                        Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED
                    )
                    if (accessibilityDisplayInversionEnabled != null) {
                        available.add(A065 to accessibilityDisplayInversionEnabled)
                    } else {
                        unavailable.add(A065 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                } else {
                    unavailable.add(A065 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val accessibilityEnabled = Secure.getString(
                    context.contentResolver,
                    Secure.ACCESSIBILITY_ENABLED
                )
                if (accessibilityEnabled != null) {
                    available.add(A066 to accessibilityEnabled)
                } else {
                    unavailable.add(A066 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                unavailable.add(A067 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                val allowedGeolocationOrigins =
                    Secure.getString(context.contentResolver, Secure.ALLOWED_GEOLOCATION_ORIGINS)
                if (allowedGeolocationOrigins != null) {
                    available.add(A068 to allowedGeolocationOrigins)
                } else {
                    unavailable.add(A068 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val androidId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
                if (androidId != null) {
                    available.add(A069 to androidId)
                } else {
                    unavailable.add(A069 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val defaultInputMethod =
                    Secure.getString(context.contentResolver, Secure.DEFAULT_INPUT_METHOD)
                if (defaultInputMethod != null) {
                    available.add(A071 to defaultInputMethod)
                } else {
                    unavailable.add(A071 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val enabledAccessibilityServices =
                    Secure.getString(context.contentResolver, Secure.ENABLED_ACCESSIBILITY_SERVICES)
                if (enabledAccessibilityServices != null) {
                    available.add(A073 to enabledAccessibilityServices)
                } else {
                    unavailable.add(A073 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val enabledInputMethods =
                    Secure.getString(context.contentResolver, Secure.ENABLED_INPUT_METHODS)
                if (enabledInputMethods != null) {
                    available.add(A074 to enabledInputMethods)
                } else {
                    unavailable.add(A074 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val inputMethodSelectorVisibility = Secure.getString(
                    context.contentResolver,
                    Secure.INPUT_METHOD_SELECTOR_VISIBILITY
                )
                if (inputMethodSelectorVisibility != null) {
                    available.add(A075 to inputMethodSelectorVisibility)
                } else {
                    unavailable.add(A075 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (installPackagesPermission) {
                        available.add(
                            A076 to context.applicationContext.packageManager.canRequestPackageInstalls()
                                .toString()
                        )
                    } else {
                        unavailable.add(A076 to DeviceParameterUnavailabilityReasonCodes.RE03.name)
                    }
                } else {
                    unavailable.add(A076 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val locationManager =
                    context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    available.add(A077 to locationManager.isLocationEnabled.toString())
                } else {
                    unavailable.add(A077 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val skipFirstUseHints =
                        Secure.getString(context.contentResolver, Secure.SKIP_FIRST_USE_HINTS)
                    if (skipFirstUseHints != null) {
                        available.add(A078 to skipFirstUseHints)
                    } else {
                        unavailable.add(A078 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                    }
                } else {
                    unavailable.add(A078 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                unavailable.add(A079 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                val ttsDefaultPitch =
                    Secure.getString(context.contentResolver, Secure.TTS_DEFAULT_PITCH)
                if (ttsDefaultPitch != null) {
                    available.add(A080 to ttsDefaultPitch)
                } else {
                    unavailable.add(A080 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val ttsDefaultRate =
                    Secure.getString(context.contentResolver, Secure.TTS_DEFAULT_RATE)
                if (ttsDefaultRate != null) {
                    available.add(A081 to ttsDefaultRate)
                } else {
                    unavailable.add(A081 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val ttsDefaultSynth =
                    Secure.getString(context.contentResolver, Secure.TTS_DEFAULT_SYNTH)
                if (ttsDefaultSynth != null) {
                    available.add(A082 to ttsDefaultSynth)
                } else {
                    unavailable.add(A082 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val ttsEnabledPlugins =
                    Secure.getString(context.contentResolver, Secure.TTS_ENABLED_PLUGINS)
                if (ttsEnabledPlugins != null) {
                    available.add(A083 to ttsEnabledPlugins)
                } else {
                    unavailable.add(A083 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class SettingsGlobal {
        //ADB_ENABLED
        A084,

        //AIRPLANE_MODE_RADIOS
        A085,

        //ALWAYS_FINISH_ACTIVITIES
        A086,

        //ANIMATOR_DURATION_SCALE
        A087,

        //AUTO_TIME
        A088,

        //AUTO_TIME_ZONE
        A089,

        //DATA_ROAMING
        A070,

        //DEVELOPMENT_SETTINGS_ENABLED
        A090,

        //DEVICE_PROVISIONED
        A072,

        //HTTP_PROXY
        A091,

        //NETWORK_PREFERENCE
        A092,

        //STAY_ON_WHILE_PLUGGED_IN
        A093,

        //TRANSITION_ANIMATION_SCALE
        A094,

        //USB_MASS_STORAGE_ENABLED
        A095,

        //USE_GOOGLE_MAIL
        A096,

        //WAIT_FOR_DEBUGGER
        A097,

        //WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON
        A098;

        companion object {
            fun getParameters(context: Context): MutableMap<String, List<Pair<SettingsGlobal, String>>> {

                val parameters = mutableMapOf<String, List<Pair<SettingsGlobal, String>>>()
                val available = mutableListOf<Pair<SettingsGlobal, String>>()
                val unavailable = mutableListOf<Pair<SettingsGlobal, String>>()

                val adbEnabled = Global.getString(
                    context.contentResolver,
                    Global.ADB_ENABLED
                )
                if (adbEnabled != null) {
                    available.add(A084 to adbEnabled)
                } else {
                    unavailable.add(A084 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val airplaneModeRadios = Global.getString(
                    context.contentResolver,
                    Global.AIRPLANE_MODE_RADIOS
                )
                if (airplaneModeRadios != null) {
                    available.add(A085 to airplaneModeRadios)
                } else {
                    unavailable.add(A085 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val alwaysFinishActivities = Global.getString(
                    context.contentResolver,
                    Global.ALWAYS_FINISH_ACTIVITIES
                )
                if (alwaysFinishActivities != null) {
                    available.add(A086 to alwaysFinishActivities)
                } else {
                    unavailable.add(A086 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val animatorDurationScale = Global.getString(
                    context.contentResolver,
                    Global.ANIMATOR_DURATION_SCALE
                )
                if (animatorDurationScale != null) {
                    available.add(A087 to animatorDurationScale)
                } else {
                    unavailable.add(A087 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val autoTime = Global.getString(
                    context.contentResolver,
                    Global.AUTO_TIME
                )
                if (autoTime != null) {
                    available.add(A088 to autoTime)
                } else {
                    unavailable.add(A088 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val autoTimeZone = Global.getString(
                    context.contentResolver,
                    Global.AUTO_TIME_ZONE
                )
                if (autoTimeZone != null) {
                    available.add(A089 to autoTimeZone)
                } else {
                    unavailable.add(A089 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val dataRoaming = Global.getString(
                    context.contentResolver,
                    Global.DATA_ROAMING
                )
                if (dataRoaming != null) {
                    available.add(A070 to dataRoaming)
                } else {
                    unavailable.add(A070 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val developmentSettingsEnabled = Global.getString(
                    context.contentResolver,
                    Global.DEVELOPMENT_SETTINGS_ENABLED
                )
                if (developmentSettingsEnabled != null) {
                    available.add(A090 to developmentSettingsEnabled)
                } else {
                    unavailable.add(A090 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val deviceProvisioned = Global.getString(
                    context.contentResolver,
                    Global.DEVICE_PROVISIONED
                )
                if (deviceProvisioned != null) {
                    available.add(A072 to deviceProvisioned)
                } else {
                    unavailable.add(A072 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val httpProxy = Global.getString(
                    context.contentResolver,
                    Global.HTTP_PROXY
                )
                if (httpProxy != null) {
                    available.add(A091 to httpProxy)
                } else {
                    unavailable.add(A091 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val networkPreference = Global.getString(
                    context.contentResolver,
                    Global.NETWORK_PREFERENCE
                )
                if (networkPreference != null) {
                    available.add(A092 to networkPreference)
                } else {
                    unavailable.add(A092 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val stayOnWhilePluggedIn = Global.getString(
                    context.contentResolver,
                    Global.STAY_ON_WHILE_PLUGGED_IN
                )
                if (stayOnWhilePluggedIn != null) {
                    available.add(A093 to stayOnWhilePluggedIn)
                } else {
                    unavailable.add(A093 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val transitionAnimationScale = Global.getString(
                    context.contentResolver,
                    Global.TRANSITION_ANIMATION_SCALE
                )
                if (transitionAnimationScale != null) {
                    available.add(A094 to transitionAnimationScale)
                } else {
                    unavailable.add(A094 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val usbMassStorageEnabled = Global.getString(
                    context.contentResolver,
                    Global.USB_MASS_STORAGE_ENABLED
                )
                if (usbMassStorageEnabled != null) {
                    available.add(A095 to usbMassStorageEnabled)
                } else {
                    unavailable.add(A095 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val useGoogleMail = Global.getString(
                    context.contentResolver,
                    Global.USE_GOOGLE_MAIL
                )
                if (useGoogleMail != null) {
                    available.add(A096 to useGoogleMail)
                } else {
                    unavailable.add(A096 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val waitForDebugger = Global.getString(
                    context.contentResolver,
                    Global.WAIT_FOR_DEBUGGER
                )
                if (waitForDebugger != null) {
                    available.add(A097 to waitForDebugger)
                } else {
                    unavailable.add(A097 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                unavailable.add(A098 to DeviceParameterUnavailabilityReasonCodes.RE02.name)


                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class SettingsSystem {
        //ACCELEROMETER_ROTATION
        A099,

        //BLUETOOTH_DISCOVERABILITY
        A100,

        //BLUETOOTH_DISCOVERABILITY_TIMEOUT
        A101,

        //DATE_FORMAT
        A102,

        //DTMF_TONE_TYPE_WHEN_DIALING
        A103,

        //DTMF_TONE_WHEN_DIALING
        A104,

        //END_BUTTON_BEHAVIOUR
        A105,

        //FONT_SCALE
        A106,

        //HAPTIC_FEEDBACK_ENABLED
        A107,

        //MODE_RINGER_STREAMS_AFFECTED
        A108,

        //NOTIFICATION_SOUND
        A109,

        //MUTE_STREAMS_AFFECTED
        A110,

        //RINGTONE
        A111,

        //SCREEN_BRIGHTNESS
        A112,

        //SCREEN_BRIGHTNESS_MODE
        A113,

        //SCREEN_OFF_TIMEOUT
        A114,

        //SOUND_EFFECTS_ENABLED
        A115,

        //TEXT_AUTO_CAPS
        A116,

        //TEXT_AUTO_PUNCTUATE
        A117,

        //TEXT_AUTO_REPLACE
        A118,

        //TEXT_SHOW_PASSWORD
        A119,

        //TIME_12_24
        A120,

        //USER_ROTATION
        A121,

        //VIBRATE_ON
        A122,

        //VIBRATE_WHEN_RINGING
        A123;

        companion object {
            fun getParameters(context: Context): MutableMap<String, List<Pair<SettingsSystem, String>>> {

                val parameters = mutableMapOf<String, List<Pair<SettingsSystem, String>>>()
                val available = mutableListOf<Pair<SettingsSystem, String>>()
                val unavailable = mutableListOf<Pair<SettingsSystem, String>>()

                val accelerometerRotation = System.getString(
                    context.contentResolver,
                    System.ACCELEROMETER_ROTATION
                )
                if (accelerometerRotation != null) {
                    available.add(A099 to accelerometerRotation)
                } else {
                    unavailable.add(A099 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val bluetoothDiscoverability = System.getString(
                    context.contentResolver,
                    System.BLUETOOTH_DISCOVERABILITY
                )
                if (bluetoothDiscoverability != null) {
                    available.add(A100 to bluetoothDiscoverability)
                } else {
                    unavailable.add(A100 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val bluetoothDiscoverabilityTimeout = System.getString(
                    context.contentResolver,
                    System.BLUETOOTH_DISCOVERABILITY_TIMEOUT
                )
                if (bluetoothDiscoverabilityTimeout != null) {
                    available.add(A101 to bluetoothDiscoverabilityTimeout)
                } else {
                    unavailable.add(A101 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val dateFormat = System.getString(
                    context.contentResolver,
                    System.DATE_FORMAT
                )
                if (dateFormat != null) {
                    available.add(A102 to dateFormat)
                } else {
                    unavailable.add(A102 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val dtmfToneTypeWhenDialing = System.getString(
                        context.contentResolver,
                        System.DTMF_TONE_TYPE_WHEN_DIALING
                    )
                    if (dtmfToneTypeWhenDialing != null) {
                        available.add(A103 to dtmfToneTypeWhenDialing)
                    } else {
                        unavailable.add(A103 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                } else {
                    unavailable.add(A103 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }
                val dtmfToneWhenDialing = System.getString(
                    context.contentResolver,
                    System.DTMF_TONE_WHEN_DIALING
                )
                if (dtmfToneWhenDialing != null) {
                    available.add(A104 to dtmfToneWhenDialing)
                } else {
                    unavailable.add(A104 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val endButtonBehavior = System.getString(
                    context.contentResolver,
                    System.END_BUTTON_BEHAVIOR
                )
                if (endButtonBehavior != null) {
                    available.add(A105 to endButtonBehavior)
                } else {
                    unavailable.add(A105 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val fontScale = System.getString(
                    context.contentResolver,
                    System.FONT_SCALE
                )
                if (fontScale != null) {
                    available.add(A106 to fontScale)
                } else {
                    unavailable.add(A106 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val hapticFeedbackEnabled = System.getString(
                    context.contentResolver,
                    System.HAPTIC_FEEDBACK_ENABLED
                )
                if (hapticFeedbackEnabled != null) {
                    available.add(A107 to hapticFeedbackEnabled)
                } else {
                    unavailable.add(A107 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val modeRingerStreamsAffected = System.getString(
                    context.contentResolver,
                    System.MODE_RINGER_STREAMS_AFFECTED
                )
                if (modeRingerStreamsAffected != null) {
                    available.add(A108 to modeRingerStreamsAffected)
                } else {
                    unavailable.add(A108 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val notificationSound = System.getString(
                    context.contentResolver,
                    System.NOTIFICATION_SOUND
                )
                if (notificationSound != null) {
                    available.add(A109 to notificationSound)
                } else {
                    unavailable.add(A109 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val muteStreamsAffected = System.getString(
                    context.contentResolver,
                    System.MUTE_STREAMS_AFFECTED
                )
                if (muteStreamsAffected != null) {
                    available.add(A110 to muteStreamsAffected)
                } else {
                    unavailable.add(A110 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val ringtone = System.getString(
                    context.contentResolver,
                    System.RINGTONE
                )
                if (ringtone != null) {
                    available.add(A111 to ringtone)
                } else {
                    unavailable.add(A111 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val screenBrightness = System.getString(
                    context.contentResolver,
                    System.SCREEN_BRIGHTNESS
                )
                if (screenBrightness != null) {
                    available.add(A112 to screenBrightness)
                } else {
                    unavailable.add(A112 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val screenBrightnessMode = System.getString(
                    context.contentResolver,
                    System.SCREEN_BRIGHTNESS_MODE
                )
                if (screenBrightnessMode != null) {
                    available.add(A113 to screenBrightnessMode)
                } else {
                    unavailable.add(A113 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val screenOffTimeout = System.getString(
                    context.contentResolver,
                    System.SCREEN_OFF_TIMEOUT
                )
                if (screenOffTimeout != null) {
                    available.add(A114 to screenOffTimeout)
                } else {
                    unavailable.add(A114 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val soundEffectsEnabled = System.getString(
                    context.contentResolver,
                    System.SOUND_EFFECTS_ENABLED
                )
                if (soundEffectsEnabled != null) {
                    available.add(A115 to soundEffectsEnabled)
                } else {
                    unavailable.add(A115 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val textAutoCaps = System.getString(
                    context.contentResolver,
                    System.TEXT_AUTO_CAPS
                )
                if (textAutoCaps != null) {
                    available.add(A116 to textAutoCaps)
                } else {
                    unavailable.add(A116 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val textAutoPunctuate = System.getString(
                    context.contentResolver,
                    System.TEXT_AUTO_PUNCTUATE
                )
                if (textAutoPunctuate != null) {
                    available.add(A117 to textAutoPunctuate)
                } else {
                    unavailable.add(A117 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val textAutoReplace = System.getString(
                    context.contentResolver,
                    System.TEXT_AUTO_REPLACE
                )
                if (textAutoReplace != null) {
                    available.add(A118 to textAutoReplace)
                } else {
                    unavailable.add(A118 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val textShowPassword = System.getString(
                    context.contentResolver,
                    System.TEXT_SHOW_PASSWORD
                )
                if (textShowPassword != null) {
                    available.add(A119 to textShowPassword)
                } else {
                    unavailable.add(A119 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val time1224 = System.getString(
                    context.contentResolver,
                    System.TIME_12_24
                )
                if (time1224 != null) {
                    available.add(A120 to time1224)
                } else {
                    unavailable.add(A120 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val userRotation = System.getString(
                    context.contentResolver,
                    System.USER_ROTATION
                )
                if (userRotation != null) {
                    available.add(A121 to userRotation)
                } else {
                    unavailable.add(A121 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                val vibrateOn = System.getString(
                    context.contentResolver,
                    System.VIBRATE_ON
                )
                if (vibrateOn != null) {
                    available.add(A122 to vibrateOn)
                } else {
                    unavailable.add(A122 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val vibrateWhenRinging = System.getString(
                        context.contentResolver,
                        System.VIBRATE_WHEN_RINGING
                    )
                    if (vibrateWhenRinging != null) {
                        available.add(A123 to vibrateWhenRinging)
                    } else {
                        unavailable.add(A123 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                } else {
                    unavailable.add(A123 to DeviceParameterUnavailabilityReasonCodes.RE02.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }

    enum class PackageManagerKeys {
        //isSafeMode
        A124,

        //getInstalledApplications
        A125,

        //getInstallerPackageName
        A126,

        //getSystemAvailableFeatures
        A127,

        //getSystemSharedLibraryNames
        A128,

        //getExternalStorageState
        A129,

        //getAvailableLocales
        A130,

        //density
        A131,

        //densityDpi
        A132,

        //scaledDensity
        A133,

        //xdpi
        A134,

        //ydpi
        A135,

        //statFs
        A136;

        companion object {
            fun getParameters(context: Context): MutableMap<String, List<Pair<PackageManagerKeys, String>>> {

                val parameters = mutableMapOf<String, List<Pair<PackageManagerKeys, String>>>()
                val available = mutableListOf<Pair<PackageManagerKeys, String>>()
                val unavailable = mutableListOf<Pair<PackageManagerKeys, String>>()

                val packageManager = context.applicationContext.packageManager

                available.add(A124 to packageManager.isSafeMode.toString())
                available.add(
                    A125 to packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                        .toString()
                )
                try {
                    val packageName = packageManager.getApplicationInfo(
                        context.packageName,
                        0
                    ).packageName
                    val installerPackageName =
                        context.packageManager.getInstallerPackageName(packageName)
                    if (installerPackageName != null) {
                        available.add(A126 to installerPackageName)
                    } else {
                        unavailable.add(A126 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                    }
                } catch (e: IllegalArgumentException) {
                    unavailable.add(A126 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                available.add(A127 to packageManager.systemAvailableFeatures.size.toString())
                val systemSharedLibraryNames = packageManager.systemSharedLibraryNames
                if (systemSharedLibraryNames != null) {
                    available.add(A128 to systemSharedLibraryNames.size.toString())
                } else {
                    unavailable.add(A128 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }
                available.add(A129 to Environment.getExternalStorageState())
                available.add(A130 to Locale.getAvailableLocales().size.toString())

                val wm =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val display = wm.defaultDisplay
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)

                available.add(A131 to metrics.density.toString())
                available.add(A132 to metrics.densityDpi.toString())
                available.add(A133 to metrics.scaledDensity.toString())
                available.add(A134 to metrics.xdpi.toString())
                available.add(A135 to metrics.ydpi.toString())

                val path = context.getExternalFilesDir(null)?.path
                if (path != null) {
                    available.add(A136 to StatFs(context.getExternalFilesDir(null)?.path).totalBytes.toString())
                } else {
                    unavailable.add(A136 to DeviceParameterUnavailabilityReasonCodes.RE04.name)
                }

                parameters[AVAILABLE] = available
                parameters[UNAVAILABLE] = unavailable
                return parameters
            }
        }
    }
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
enum class SecurityWarnings{
    //The device is jailbroken.
    SW01,
    //The integrity of the SDK has been tampered.
    SW02,
    //An emulator is being used to run the App.
    SW03,
    //A debugger is attached to the App.
    SW04,
    //The OS or the OS version is not supported.
    SW05
}

@SuppressLint("MissingPermission")
fun getDeviceData(context: Context): DeviceData {
    val available = mutableMapOf<String, String>()
    val unavailable = mutableMapOf<String, String>()
    val securityWarnings = mutableListOf<String>()

    val commonAvailableParameters =
        Identifiers.Common.getParameters(context).filterKeys { it == AVAILABLE }.values
    val commonUnavailableParameters =
        Identifiers.Common.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val telephonyAvailableParameters =
        Identifiers.Telephony.getParameters(context).filterKeys { it == AVAILABLE }.values
    val telephonyUnavailableParameters =
        Identifiers.Telephony.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val wifiAvailableParameters =
        Identifiers.Wifi.getParameters(context).filterKeys { it == AVAILABLE }.values
    val wifiUnavailableParameters =
        Identifiers.Wifi.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val bluetoothAvailableParameters =
        Identifiers.Bluetooth.getParameters(context).filterKeys { it == AVAILABLE }.values
    val bluetoothUnavailableParameters =
        Identifiers.Bluetooth.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val phoneBuildAvailableParameters =
        Identifiers.PhoneBuild.getParameters(context).filterKeys { it == AVAILABLE }.values
    val phoneBuildUnavailableParameters =
        Identifiers.PhoneBuild.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val settingsSecureAvailableParameters =
        Identifiers.SettingsSecure.getParameters(context).filterKeys { it == AVAILABLE }.values
    val settingsSecureUnavailableParameters =
        Identifiers.SettingsSecure.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val settingsGlobalAvailableParameters =
        Identifiers.SettingsGlobal.getParameters(context).filterKeys { it == AVAILABLE }.values
    val settingsGlobalUnavailableParameters =
        Identifiers.SettingsGlobal.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val settingsSystemAvailableParameters =
        Identifiers.SettingsSystem.getParameters(context).filterKeys { it == AVAILABLE }.values
    val settingsSystemUnavailableParameters =
        Identifiers.SettingsSystem.getParameters(context).filterKeys { it == UNAVAILABLE }.values

    val packageManagerKeysAvailableParameters =
        Identifiers.PackageManagerKeys.getParameters(context).filterKeys { it == AVAILABLE }.values
    val packageManagerKeysUnavailableParameters =
        Identifiers.PackageManagerKeys.getParameters(context)
            .filterKeys { it == UNAVAILABLE }.values

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
    wifiAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Wifi, String> ->
            available[pair.first.name] = pair.second
        }
    }
    wifiUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Wifi, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    bluetoothAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Bluetooth, String> ->
            available[pair.first.name] = pair.second
        }
    }
    bluetoothUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.Bluetooth, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    phoneBuildAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.PhoneBuild, String> ->
            available[pair.first.name] = pair.second
        }
    }
    phoneBuildUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.PhoneBuild, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    settingsSecureAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsSecure, String> ->
            available[pair.first.name] = pair.second
        }
    }
    settingsSecureUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsSecure, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    settingsGlobalAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsGlobal, String> ->
            available[pair.first.name] = pair.second
        }
    }
    settingsGlobalUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsGlobal, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    settingsSystemAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsSystem, String> ->
            available[pair.first.name] = pair.second
        }
    }
    settingsSystemUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.SettingsSystem, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }
    packageManagerKeysAvailableParameters.map {
        it.forEach { pair: Pair<Identifiers.PackageManagerKeys, String> ->
            available[pair.first.name] = pair.second
        }
    }
    packageManagerKeysUnavailableParameters.map {
        it.forEach { pair: Pair<Identifiers.PackageManagerKeys, String> ->
            unavailable[pair.first.name] = pair.second
        }
    }

    if(isEmulator()){
        securityWarnings.add(SecurityWarnings.SW03.name)
    }
    if(Debug.isDebuggerConnected()){
        securityWarnings.add(SecurityWarnings.SW04.name)
    }

    return DeviceData(available, unavailable, securityWarnings)
}

private fun isEmulator() = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator")

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
