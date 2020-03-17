package com.judopay.model.googlepay

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GooglePayIsReadyToPayRequest(
    val apiVersion: Number,
    val apiVersionMinor: Number,
    val allowedPaymentMethods: Array<GooglePayPaymentMethod>,
    val existingPaymentMethodRequired: Boolean?
) : Parcelable
