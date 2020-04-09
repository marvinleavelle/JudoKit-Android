package com.judopay.model

import android.os.Parcelable
import com.judopay.R
import com.judopay.model.CardNetwork.Companion.DEFAULT_CARD_NUMBER_MASK
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class CardNetwork : Parcelable {
    VISA,
    MASTERCARD,
    MAESTRO,
    AMEX,
    CHINA_UNION_PAY,
    JCB,
    DISCOVER,
    DINERS_CLUB,
    OTHER;

    companion object {
        const val DEFAULT_CARD_NUMBER_MASK = "#### #### #### ####"

        private val REGEX_VISA = "^4[0-9]{3}.*?".toRegex()
        private val REGEX_MASTERCARD = "^5[1-5][0-9]{2}.*?".toRegex()
        private val REGEX_MAESTRO =
            "^(5018|5020|5038|6304|6759|6761|6763|6334|6767|4903|4905|4911|4936|5641 82|6331 10|6333|5600|5602|5603|5610|5611|5656|6700|6706|6775|6709|6771|6773).*?".toRegex()
        private val REGEX_AMEX = "^3[47][0-9]{2}.*?".toRegex()
        private val REGEX_DISCOVER =
            "^65.*?|64[4-9].*?|6011.*?|(622(1 2[6-9].*?|1 [3-9][0-9].*?|[2-8] [0-9][0-9].*?|9 [01][0-9].*?|9 2[0-5].*?).*?)".toRegex()
        private val REGEX_DINERS_CLUB = "^(30[0-5]|309|36|38|39).*?".toRegex()
        private val REGEX_JCB = "^(35[2-8][0-9]).*?".toRegex()
        private val AMEX_PREFIXES = arrayOf("34", "37")
        private val VISA_PREFIXES = arrayOf("4")
        private val MASTERCARD_PREFIXES = arrayOf("50", "51", "52", "53", "54", "55")
        private val CHINA_UNION_PAY_PREFIXES = arrayOf("62")

        fun ofNumber(number: String): CardNetwork {
            return when {
                number.hasOneOfPrefixes(VISA_PREFIXES) || number.matches(REGEX_VISA) -> {
                    VISA
                }

                number.hasOneOfPrefixes(MASTERCARD_PREFIXES) || number.matches(REGEX_MASTERCARD) -> {
                    MASTERCARD
                }

                number.matches(REGEX_MAESTRO) -> {
                    MAESTRO
                }

                number.hasOneOfPrefixes(AMEX_PREFIXES) || number.matches(REGEX_AMEX) -> {
                    AMEX
                }

                number.matches(REGEX_DISCOVER) -> {
                    DISCOVER
                }

                number.matches(REGEX_DINERS_CLUB) -> {
                    DINERS_CLUB
                }

                number.matches(REGEX_JCB) -> {
                    JCB
                }

                number.hasOneOfPrefixes(CHINA_UNION_PAY_PREFIXES) -> {
                    CHINA_UNION_PAY
                }

                else -> {
                    OTHER
                }
            }
        }

        private fun String.hasOneOfPrefixes(prefixes: Array<String>): Boolean {
            for (prefix in prefixes) {
                if (startsWith(prefix)) {
                    return true
                }
            }
            return false
        }

        fun withIdentifier(id: Int): CardNetwork = when (id) {
            1 /*VISA*/,
            3 /*VISA_ELECTRON*/,
            11 /*VISA_DEBIT*/ -> VISA
            2 -> MASTERCARD
            10 -> MAESTRO
            8 -> AMEX
            7 -> CHINA_UNION_PAY
            9 -> JCB
            12 -> DISCOVER
            13 -> DINERS_CLUB
            else -> OTHER
        }
    }
}

val CardNetwork.cardNumberMask: String
    get() = when (this) {
        CardNetwork.AMEX -> "#### ###### #####"
        CardNetwork.DINERS_CLUB -> "#### ###### ####"
        else -> DEFAULT_CARD_NUMBER_MASK
    }

val CardNetwork.securityCodeNumberMask: String
    get() = if (this == CardNetwork.AMEX) "####" else "###"

val CardNetwork.securityCodeLength: Int
    get() = if (this == CardNetwork.AMEX) 4 else 3

val CardNetwork.securityCodeName: String
    get() = when (this) {
        CardNetwork.AMEX -> "CID"
        CardNetwork.VISA -> "CVV2"
        CardNetwork.MASTERCARD -> "CVC2"
        CardNetwork.CHINA_UNION_PAY -> "CVN2"
        CardNetwork.JCB -> "CAV2"
        else -> "CVV"
    }

val CardNetwork.displayName: String
    get() = when (this) {
        CardNetwork.VISA -> "Visa"
        CardNetwork.MASTERCARD -> "Master Card"
        CardNetwork.MAESTRO -> "Maestro"
        CardNetwork.AMEX -> "AmEx"
        CardNetwork.CHINA_UNION_PAY -> "China UnionPay"
        CardNetwork.JCB -> "JCB"
        CardNetwork.DISCOVER -> "Discover"
        CardNetwork.DINERS_CLUB -> "Diners Club"
        CardNetwork.OTHER -> "Unknown Card Network"
    }

val CardNetwork.iconImageResId: Int
    get() = when (this) {
        CardNetwork.AMEX -> R.drawable.ic_card_amex
        CardNetwork.MASTERCARD -> R.drawable.ic_card_mastercard
        CardNetwork.MAESTRO -> R.drawable.ic_card_maestro
        CardNetwork.VISA -> R.drawable.ic_card_visa
        CardNetwork.DISCOVER -> R.drawable.ic_discover
        CardNetwork.DINERS_CLUB -> R.drawable.ic_diners_club
        CardNetwork.JCB -> R.drawable.ic_jcb
        else -> 0
    }

val CardNetwork.lightIconImageResId: Int
    get() = when (this) {
        CardNetwork.AMEX -> R.drawable.ic_card_amex_light
        CardNetwork.VISA -> R.drawable.ic_card_visa_light
        else -> this.iconImageResId
    }

val CardNetwork.cardNumberMaxLength: Int
    get() = when (this) {
        CardNetwork.AMEX -> 15
        CardNetwork.DINERS_CLUB -> 14
        else -> 16
    }

val CardNetwork.notSupportedErrorMessageResId: Int
    get() = when (this) {
        CardNetwork.VISA -> R.string.error_visa_not_supported
        CardNetwork.MASTERCARD -> R.string.error_mastercard_not_supported
        CardNetwork.MAESTRO -> R.string.error_maestro_not_supported
        CardNetwork.AMEX -> R.string.error_amex_not_supported
        CardNetwork.DISCOVER -> R.string.error_discover_not_supported
        CardNetwork.CHINA_UNION_PAY -> R.string.error_union_pay_not_supported
        CardNetwork.JCB -> R.string.error_jcb_not_supported
        CardNetwork.DINERS_CLUB -> R.string.error_diners_club_not_supported
        else -> R.string.empty
    }

val CardNetwork.defaultCardNameResId: Int
    get() = when (this) {
        CardNetwork.AMEX -> R.string.default_amex_card_title
        CardNetwork.MASTERCARD -> R.string.default_mastercard_card_title
        CardNetwork.MAESTRO -> R.string.default_maestro_card_title
        CardNetwork.VISA -> R.string.default_visa_card_title
        CardNetwork.DISCOVER -> R.string.default_discover_card_title
        CardNetwork.DINERS_CLUB -> R.string.default_dinnersclub_card_title
        CardNetwork.JCB -> R.string.default_jcb_card_title
        CardNetwork.CHINA_UNION_PAY -> R.string.default_chinaunionpay_card_title
        else -> R.string.empty
    }

val CardNetwork.typeId: Int
    get() = when (this) {
        CardNetwork.VISA -> 1
        CardNetwork.MASTERCARD -> 2
        CardNetwork.MAESTRO -> 10
        CardNetwork.AMEX -> 8
        CardNetwork.CHINA_UNION_PAY -> 7
        CardNetwork.JCB -> 9
        CardNetwork.DISCOVER -> 12
        CardNetwork.DINERS_CLUB -> 13
        else -> -1
    }

val CardNetwork.isSupportedByGooglePay: Boolean
    get() = when (this) {
        CardNetwork.VISA,
        CardNetwork.MASTERCARD,
        CardNetwork.AMEX,
        CardNetwork.DISCOVER,
        CardNetwork.MAESTRO,
        CardNetwork.JCB -> true
        else -> false
    }