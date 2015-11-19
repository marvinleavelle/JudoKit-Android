package com.judopay;

import com.judopay.payment.Collection;
import com.judopay.payment.PaymentTransaction;
import com.judopay.payment.Receipt;
import com.judopay.payment.Refund;
import com.judopay.payment.ThreeDSecureInfo;
import com.judopay.payment.TokenTransaction;
import com.judopay.register.RegisterTransaction;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.PUT;
import rx.Observable;

public interface JudoApiService {

    @POST("transactions/payments")
    Observable<Receipt> payment(@Body PaymentTransaction paymentTransaction);

    @POST("transactions/payments")
    Observable<Receipt> tokenPayment(@Body TokenTransaction transaction);

    @PUT("transactions/{receiptId}")
    Observable<Receipt> threeDSecurePayment(String receiptId, @Body ThreeDSecureInfo threeDSecureInfo);

    @POST("transactions/collections")
    Observable<Receipt> collection(@Body Collection collection);

    @POST("transactions/refunds")
    Observable<Receipt> refund(@Body Refund refund);

    @POST("transactions/preauths")
    Observable<Receipt> preAuth(@Body PaymentTransaction paymentTransaction);

    @POST("transactions/registercard")
    Observable<Receipt> registerCard(@Body RegisterTransaction transaction);

}