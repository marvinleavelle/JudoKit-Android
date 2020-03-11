package com.judopay.api.error

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

const val JUDO_ID_NOT_SUPPLIED = 0
const val JUDO_ID_NOT_SUPPLIED_1 = 1
const val JUDO_ID_NOT_VALID = 2
const val JUDO_ID_NOT_VALID_1 = 3
const val AMOUNT_GREATER_THAN_0 = 4
const val AMOUNT_NOT_VALID = 5
const val AMOUNT_TWO_DECIMAL_PLACES = 6
const val AMOUNT_BETWEEN_0_AND_5000 = 7
const val PARTNER_SERVICE_FEE_NOT_VALID = 8
const val PARTNER_SERVICE_FEE_BETWEEN_0_AND_5000 = 9
const val CONSUMER_REFERENCE_NOT_SUPPLIED = 10
const val CONSUMER_REFERENCE_NOT_SUPPLIED_1 = 11
const val CONSUMER_REFERENCE_LENGTH = 12
const val CONSUMER_REFERENCE_LENGTH_1 = 13
const val CONSUMER_REFERENCE_LENGTH_2 = 14
const val PAYMENT_REFERENCE_NOT_SUPPLIED = 15
const val PAYMENT_REFERENCE_NOT_SUPPLIED_1 = 16
const val PAYMENT_REFERENCE_NOT_SUPPLIED_2 = 17
const val PAYMENT_REFERENCE_NOT_SUPPLIED_3 = 18
const val PAYMENT_REFERENCE_LENGTH = 19
const val PAYMENT_REFERENCE_LENGTH_1 = 20
const val PAYMENT_REFERENCE_LENGTH_2 = 21
const val PAYMENT_REFERENCE_LENGTH_3 = 22
const val PAYMENT_REFERENCE_LENGTH_4 = 23
const val CURRENCY_REQUIRED = 24
const val CURRENCY_LENGTH = 25
const val CURRENCY_NOT_SUPPORTED = 26
const val DEVICE_CATEGORY_UNKNOWN = 27
const val CARD_NUMBER_NOT_SUPPLIED = 28
const val TEST_CARDS_ONLY_IN_SANDBOX = 29
const val CARD_NUMBER_INVALID = 30
const val THREE_DIGIT_CV2_NOT_SUPPLIED = 31
const val FOUR_DIGIT_CV2_NOT_SUPPLIED = 32
const val CV2_NOT_VALID = 33
const val CV2_NOT_VALID_1 = 34
const val START_DATE_OR_ISSUE_NUMBER_MUST_BE_SUPPLIED = 35
const val START_DATE_NOT_SUPPLIED = 36
const val START_DATE_WRONG_LENGTH = 37
const val START_DATE_NOT_VALID = 38
const val START_DATE_NOT_VALID_FORMAT = 39
const val START_DATE_TOO_FAR_IN_PAST = 40
const val START_DATE_MONTH_OUTSIDE_EXPECTED_RANGE = 41
const val ISSUE_NUMBER_OUTSIDE_EXPECTED_RANGE = 42
const val EXPIRY_DATE_NOT_SUPPLIED = 43
const val EXPIRY_DATE_WRONG_LENGTH = 44
const val EXPIRY_DATE_NOT_VALID = 45
const val EXPIRY_DATE_IN_PAST = 46
const val EXPIRY_DATE_TOO_FAR_IN_FUTURE = 47
const val EXPIRY_DATE_MONTH_OUTSIDE_EXPECTED_RANGE = 48
const val POSTCODE_NOT_VALID = 49
const val POSTCODE_NOT_SUPPLIED = 50
const val POSTCODE_IS_INVALID = 51
const val CARD_TOKEN_NOT_SUPPLIED = 52
const val CARD_TOKEN_ORIGINAL_TRANSACTION_FAILED = 53
const val THREE_D_SECURE_PARES_NOT_SUPPLIED = 54
const val RECEIPT_ID_NOT_SUPPLIED = 55
const val RECEIPT_ID_IS_INVALID = 56
const val TRANSACTION_TYPE_IN_URL_INVALID = 57
const val PARTNER_APPLICATION_REFERENCE_NOT_SUPPLIED = 58
const val PARTNER_APPLICATION_REFERENCE_NOT_SUPPLIED_1 = 59
const val TYPE_OF_COMPANY_NOT_SUPPLIED = 60
const val TYPE_OF_COMPANY_UNKNOWN = 61
const val PRINCIPLE_NOT_SUPPLIED = 62
const val PRINCIPLE_SALUTATION_UNKNOWN = 63
const val PRINCIPLE_FIRST_NAME_NOT_SUPPLIED = 64
const val PRINCIPLE_FIRST_NAME_LENGTH = 65
const val PRINCIPLE_FIRST_NAME_NOT_SUPPLIED_1 = 66
const val PRINCIPLE_LAST_NAME_NOT_SUPPLIED = 67
const val PRINCIPLE_LAST_NAME_LENGTH = 68
const val PRINCIPLE_LAST_NAME_NOT_SUPPLIED_1 = 69
const val PRINCIPLE_EMAIL_OR_MOBILE_NOT_SUPPLIED = 70
const val PRINCIPLE_EMAIL_ADDRESS_NOT_SUPPLIED = 71
const val PRINCIPLE_EMAIL_ADDRESS_LENGTH = 72
const val PRINCIPLE_EMAIL_ADDRESS_NOT_VALID = 73
const val PRINCIPLE_EMAIL_ADDRESS_DOMAIN_NOT_VALID = 74
const val PRINCIPLE_MOBILE_OR_EMAIL_NOT_SUPPLIED = 75
const val PRINCIPLE_MOBILE_NUMBER_NOT_VALID = 76
const val PRINCIPLE_MOBILE_NUMBER_NOT_VALID_1 = 77
const val PRINCIPLE_MOBILE_NUMBER_LENGTH = 78
const val PRINCIPLE_HOME_PHONE_NOT_VALID = 79
const val PRINCIPLE_DATE_OF_BIRTH_NOT_SUPPLIED = 80
const val PRINCIPLE_DATE_OF_BIRTH_NOT_VALID = 81
const val PRINCIPLE_DATE_OF_BIRTH_AGE = 82
const val LOCATION_TRADING_NAME_NOT_SUPPLIED = 83
const val LOCATION_PARTNER_REFERENCE_NOT_SUPPLIED = 84
const val LOCATION_PARTNER_REFERENCE_NOT_SUPPLIED_1 = 85
const val LOCATION_PARTNER_REFERENCE_LENGTH = 86
const val FIRST_NAME_NOT_SUPPLIED = 87
const val FIRST_NAME_LENGTH = 88
const val LAST_NAME_NOT_SUPPLIED = 89
const val LAST_NAME_LENGTH = 90
const val EMAIL_ADDRESS_NOT_SUPPLIED = 91
const val EMAIL_ADDRESS_LENGTH = 92
const val EMAIL_ADDRESS_NOT_VALID = 93
const val EMAIL_ADDRESS_DOMAIN_NOT_VALID = 94
const val SCHEDULE_START_DATE_NOT_SUPPLIED = 95
const val SCHEDULE_START_DATE_FORMAT_NOT_VALID = 96
const val SCHEDULE_END_DATE_NOT_SUPPLIED = 97
const val SCHEDULE_END_DATE_FORMAT_NOT_VALID = 98
const val SCHEDULE_END_DATE_MUST_BE_GREATER_THAN_START_DATE = 99
const val SCHEDULE_REPEAT_NOT_SUPPLIED = 100
const val SCHEDULE_REPEAT_MUST_BE_GREATER_THAN_1 = 101
const val SCHEDULE_INTERVAL_NOT_VALID = 102
const val SCHEDULE_INTERVAL_MUST_BE_MINIMUM_5 = 103
const val ITEMS_PER_PAGE_NOT_SUPPLIED = 104
const val ITEMS_PER_PAGE_OUT_OF_RANGE = 105
const val PAGE_NUMBER_NOT_SUPPLIED = 106
const val PAGE_NUMBER_OUT_OF_RANGE = 107
const val LEGAL_NAME_NOT_SUPPLIED = 108
const val COMPANY_NUMBER_NOT_SUPPLIED = 109
const val COMPANY_NUMBER_WRONG_LENGTH = 110
const val CURRENT_ADDRESS_NOT_SUPPLIED = 111
const val BUILDING_NUMBER_OR_NAME_NOT_SUPPLIED = 112
const val BUILDING_NUMBER_OR_NAME_LENGTH = 113
const val ADDRESS_LINE1_NOT_SUPPLIED = 114
const val ADDRESS_LINE1_LENGTH = 115
const val SORTCODE_NOT_SUPPLIED = 116
const val SORTCODE_NOT_VALID = 117
const val ACCOUNT_NUMBER_NOT_SUPPLIED = 118
const val ACCOUNT_NUMBER_NOT_VALID = 119
const val LOCATION_TURNOVER_GREATER_THAN_0 = 120
const val AVERAGE_TRANSACTION_VALUE_NOT_SUPPLIED = 121
const val AVERAGE_TRANSACTION_VALUE_GREATER_THAN_0 = 122
const val AVERAGE_TRANSACTION_VALUE_GREATER_THAN_TURNOVER = 123
const val MCC_CODE_NOT_SUPPLIED = 124
const val MCC_CODE_UNKNOWN = 125
const val GENERIC_IS_INVALID = 200
const val GENERIC_HTML_INVALID = 210

@Parcelize
data class ApiError(
        val code: Int,
        val category: Int,
        val message: String,
        val details: List<ApiErrorDetail>? = emptyList()
) : Parcelable {
    override fun toString(): String {
        return "ApiError(code=$code, category=$category, message='$message', details=$details)"
    }
}

@Parcelize
data class ApiErrorDetail(
        val code: Int,
        val message: String,
        val fieldName: String
) : Parcelable {
    override fun toString(): String {
        return "ApiErrorDetail(code=$code, message='$message', fieldName='$fieldName')"
    }
}