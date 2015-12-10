package com.judopay;

import android.os.Bundle;

import com.google.gson.Gson;
import com.judopay.model.Card;
import com.judopay.model.CardToken;
import com.judopay.model.Location;
import com.judopay.model.TokenTransaction;

import static com.judopay.BundleUtil.toMap;

class TokenPaymentPresenter extends BasePresenter {

    public TokenPaymentPresenter(PaymentFormView view, JudoApiService judoApiService, Scheduler scheduler, Gson gson) {
        super(view, judoApiService, scheduler, gson);
    }

    public void performTokenPayment(Card card, CardToken cardToken, String consumer, String judoId, String amount, String currency, Bundle metaData, boolean threeDSecureEnabled) {
        this.loading = true;
        paymentFormView.showLoading();

        TokenTransaction tokenTransaction = new TokenTransaction.Builder()
                .setAmount(amount)
                .setCardAddress(card.getCardAddress())
                .setConsumerLocation(new Location())
                .setCurrency(currency)
                .setJudoId(Long.valueOf(judoId))
                .setYourConsumerReference(consumer)
                .setCv2(card.getCv2())
                .setMetaData(toMap(metaData))
                .setEndDate(cardToken.getEndDate())
                .setLastFour(cardToken.getLastFour())
                .setToken(cardToken.getToken())
                .setType(cardToken.getType())
                .build();

        apiService.tokenPayment(tokenTransaction)
                .subscribeOn(scheduler.backgroundThread())
                .observeOn(scheduler.mainThread())
                .subscribe(callback(threeDSecureEnabled), error());
    }

}