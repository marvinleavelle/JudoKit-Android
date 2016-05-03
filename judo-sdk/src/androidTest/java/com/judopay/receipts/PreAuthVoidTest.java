package com.judopay.receipts;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import com.judopay.Judo;
import com.judopay.JudoApiService;
import com.judopay.model.Currency;
import com.judopay.model.PaymentRequest;
import com.judopay.model.Receipt;
import com.judopay.model.VoidRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.functions.Func1;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class PreAuthVoidTest {

    @Before
    public void setEnvironment() {
        Judo.setEnvironment(Judo.UAT);
    }

    @Test
    public void shouldPreAuthAndVoidTransaction() {
        Context context = InstrumentationRegistry.getContext();
        final JudoApiService apiService = Judo.getApiService(context);

        PaymentRequest paymentRequest = new PaymentRequest.Builder()
                .setJudoId("100915867")
                .setAmount("0.01")
                .setCardNumber("4976000000003436")
                .setCv2("452")
                .setExpiryDate("12/20")
                .setCurrency(Currency.GBP)
                .setYourConsumerReference("PreAuthVoidTest")
                .build();

        apiService.preAuth(paymentRequest)
                .compose(RxHelpers.<Receipt>schedulers())
                .flatMap(new Func1<Receipt, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(Receipt receipt) {
                        VoidRequest voidTransaction = new VoidRequest(
                                receipt.getReceiptId(),
                                receipt.getAmount());

                        return apiService.voidPreAuth(voidTransaction);
                    }
                })
                .subscribe(RxHelpers.assertTransactionSuccessful(), RxHelpers.failOnError());
    }

}