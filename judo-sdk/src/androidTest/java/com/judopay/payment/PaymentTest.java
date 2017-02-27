package com.judopay.payment;

import android.support.test.runner.AndroidJUnit4;

import com.judopay.DeviceDna;
import com.judopay.Judo;
import com.judopay.JudoApiService;
import com.judopay.devicedna.Credentials;
import com.judopay.model.Currency;
import com.judopay.model.PaymentRequest;
import com.judopay.model.Receipt;

import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Single;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

import static android.support.test.InstrumentationRegistry.getContext;
import static com.judopay.TestUtil.API_SECRET;
import static com.judopay.TestUtil.API_TOKEN;
import static com.judopay.TestUtil.JUDO_ID;
import static com.judopay.TestUtil.getJudo;
import static com.judopay.api.JudoApiServiceFactory.createApiService;
import static java.util.UUID.randomUUID;

@RunWith(AndroidJUnit4.class)
public class PaymentTest {

    @Test
    public void shouldPerformPaymentWithNewDeviceSignals() {
        final JudoApiService apiService = createApiService(getContext(), Judo.UI_CLIENT_MODE_JUDO_SDK, getJudo());
        DeviceDna deviceDna = new DeviceDna(getContext(), new Credentials(API_TOKEN, API_SECRET));

        final PaymentRequest paymentRequest = new PaymentRequest.Builder()
                .setJudoId(JUDO_ID)
                .setAmount("0.01")
                .setCardNumber("4976000000003436")
                .setCv2("452")
                .setExpiryDate("12/20")
                .setCurrency(Currency.GBP)
                .setConsumerReference(randomUUID().toString())
                .build();

        TestSubscriber<Receipt> subscriber = new TestSubscriber<>();

        deviceDna.send(null)
                .flatMap(new Func1<String, Single<Receipt>>() {
                    @Override
                    public Single<Receipt> call(String deviceId) {
                        return apiService.payment(paymentRequest);
                    }
                }).subscribe(subscriber);

        subscriber.assertNoErrors();
    }

}