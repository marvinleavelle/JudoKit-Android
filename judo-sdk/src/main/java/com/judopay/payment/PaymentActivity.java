package com.judopay.payment;

import android.content.Intent;
import android.os.Bundle;

import com.judopay.JudoActivity;
import com.judopay.JudoPay;
import com.judopay.R;

import static com.judopay.JudoPay.EXTRA_PAYMENT;

public class PaymentActivity extends JudoActivity implements PaymentListener {

    private PaymentFragment paymentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra(EXTRA_PAYMENT)) {
            throw new IllegalArgumentException("payment must be provided to PaymentActivity");
        }

        setTitle(R.string.payment);

        if (savedInstanceState == null) {
            Payment payment = getIntent().getParcelableExtra(EXTRA_PAYMENT);
            paymentFragment = PaymentFragment.newInstance(payment, this);

            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, paymentFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (paymentFragment == null || !paymentFragment.isPaymentInProgress()) {
            super.onBackPressed();
            setResult(JudoPay.RESULT_CANCELED);
        }
    }

    @Override
    public void onPaymentSuccess(Receipt receipt) {
        Intent intent = new Intent();
        intent.putExtra(JudoPay.JUDO_RECEIPT, receipt);

        setResult(JudoPay.RESULT_PAYMENT_SUCCESS, intent);

        finish();
    }

    @Override
    public void onPaymentDeclined(Receipt receipt) {
        Intent intent = new Intent();
        intent.putExtra(JudoPay.JUDO_RECEIPT, receipt);

        setResult(JudoPay.RESULT_PAYMENT_DECLINED, intent);

        finish();
    }

    @Override
    public void onError() {
        setResult(JudoPay.RESULT_ERROR);
        finish();
    }

}