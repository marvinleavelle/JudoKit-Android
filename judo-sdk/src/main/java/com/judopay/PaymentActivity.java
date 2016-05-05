package com.judopay;

import android.content.Intent;
import android.os.Bundle;

/**
 * Displays a card entry form to the user, allowing for a payment to be made.
 * To launch the PaymentActivity, call {@link android.app.Activity#startActivityForResult(Intent, int)}
 * with an Intent the configuration options:
 *
 * <pre class="prettyprint">
 * Intent intent = new Intent(this, PaymentActivity.class);
 * intent.putExtra(Judo.JUDO_OPTIONS, new JudoOptions.Builder()
 * .setJudoId("1234567")
 * .setCurrency(Currency.GBP)
 * .setAmount("1.99")
 * .setConsumerRef("consumerRef")
 * .build());
 * startActivityForResult(intent, PAYMENT_REQUEST);
 * </pre>
 * See {@link JudoOptions} for the full list of supported options
 */
public final class PaymentActivity extends JudoActivity {

    private PaymentFragment paymentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.payment);

        if (savedInstanceState == null) {
            paymentFragment = new PaymentFragment();
            paymentFragment.setArguments(getIntent().getExtras());

            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, paymentFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (paymentFragment != null && !paymentFragment.isPaymentInProgress()) {
            super.onBackPressed();
        }
    }

}