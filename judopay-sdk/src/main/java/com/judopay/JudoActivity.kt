package com.judopay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.judopay.api.factory.JudoApiServiceFactory
import com.judopay.model.JudoPaymentResult
import com.judopay.model.code
import com.judopay.model.googlepay.GooglePayEnvironment
import com.judopay.model.isGooglePayWidget
import com.judopay.model.navigationGraphId
import com.judopay.model.toIntent
import com.judopay.service.JudoGooglePayService
import com.judopay.service.LOAD_GPAY_PAYMENT_DATA_REQUEST_CODE

class JudoActivity : AppCompatActivity() {

    private lateinit var viewModel: JudoSharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.judopay_activity)

        // Treat the content of the window as secure, preventing it from appearing in screenshots
        // or from being viewed on non-secure displays.
        val secureFlag = WindowManager.LayoutParams.FLAG_SECURE
        window.setFlags(secureFlag, secureFlag)

        // setup shared view-model & response callbacks
        val judoApiService = JudoApiServiceFactory.createApiService(applicationContext, judo)
        val factory = JudoSharedViewModelFactory(judo, buildJudoGooglePayService(), judoApiService)

        viewModel = ViewModelProvider(this, factory).get(JudoSharedViewModel::class.java)
        viewModel.paymentResult.observe(this, Observer { dispatchPaymentResult(it) })
        viewModel.threeDSecureResult.observe(this, Observer { dispatchPaymentResult(it) })

        if (judo.paymentWidgetType.isGooglePayWidget) {
            viewModel.send(JudoSharedAction.LoadGPayPaymentData)
            return
        }

        // setup navigation graph
        val graphId = judo.paymentWidgetType.navigationGraphId
        val navigationHost = NavHostFragment.create(graphId)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, navigationHost)
            .setPrimaryNavigationFragment(navigationHost)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOAD_GPAY_PAYMENT_DATA_REQUEST_CODE) dispatchGPayResult(resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dispatchGPayResult(resultCode: Int, data: Intent?) {
        val action = when (resultCode) {
            Activity.RESULT_OK -> {
                val paymentData = if (data != null) PaymentData.getFromIntent(data) else null

                if (paymentData != null) {
                    JudoSharedAction.LoadGPayPaymentDataSuccess(paymentData)
                } else {
                    JudoSharedAction.LoadGPayPaymentDataError("Null response data")
                }
            }

            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(data)
                val message = status?.statusMessage ?: "Unknown error"
                JudoSharedAction.LoadGPayPaymentDataError(message)
            }

            Activity.RESULT_CANCELED -> JudoSharedAction.LoadGPayPaymentDataUserCancelled

            else -> {
                JudoSharedAction.LoadGPayPaymentDataError("Unknown error")
            }
        }

        viewModel.send(action)
    }

    private fun dispatchPaymentResult(result: JudoPaymentResult) {
        setResult(result.code, result.toIntent())
        finish()
    }

    private fun buildJudoGooglePayService(): JudoGooglePayService {
        val environment = when (judo.googlePayConfiguration?.environment) {
            GooglePayEnvironment.PRODUCTION -> WalletConstants.ENVIRONMENT_PRODUCTION
            else -> WalletConstants.ENVIRONMENT_TEST
        }

        val walletOptions = Wallet.WalletOptions.Builder().setEnvironment(environment).build()
        val client = Wallet.getPaymentsClient(this, walletOptions)
        return JudoGooglePayService(client, this, judo)
    }
}