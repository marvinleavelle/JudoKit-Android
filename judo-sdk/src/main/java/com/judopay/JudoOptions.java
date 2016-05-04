package com.judopay;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.judopay.error.JudoIdInvalidError;
import com.judopay.model.CardToken;
import com.judopay.model.CustomLayout;

import java.util.HashMap;
import java.util.Map;

import static com.judopay.arch.TextUtil.isEmpty;
import static com.judopay.model.LuhnCheck.isValid;

/**
 * The wrapper for providing data to Activity and Fragments classes in the SDK (e.g. PaymentActivity).
 * This is preferable to using the individual Extras names defined in {@link Judo} as it provides
 * type safety.
 *
 * Use the {@link JudoOptions.Builder class for constructing} an instance of {@link JudoOptions}.
 *
 * When calling an Activity with an Intent extra or a Fragment using an arguments Bundle,
 * use {@link Judo#JUDO_OPTIONS} as the extra or argument name.
 */
public class JudoOptions implements Parcelable {

    private String judoId;
    private String amount;
    private String currency;
    private String consumerRef;
    private Bundle metaData;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String buttonLabel;
    private String activityTitle;
    private CardToken cardToken;
    private String emailAddress;
    private String mobileNumber;
    private boolean secureServerMessageShown;
    private PendingIntent cardScanningIntent;
    private CustomLayout customLayout;

    private JudoOptions() { }

    public String getAmount() {
        return amount;
    }

    public String getJudoId() {
        return judoId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getConsumerRef() {
        return consumerRef;
    }

    public Bundle getMetaData() {
        return metaData;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public CardToken getCardToken() {
        return cardToken;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public PendingIntent getCardScanningIntent() {
        return cardScanningIntent;
    }

    public boolean isSecureServerMessageShown() {
        return secureServerMessageShown;
    }

    public CustomLayout getCustomLayout() {
        return customLayout;
    }

    public Map<String, String> getMetaDataMap() {
        Map<String, String> map = new HashMap<>();

        if (metaData != null) {
            for (String key : metaData.keySet()) {
                String value = metaData.getString(key);
                map.put(key, value);
            }
        }
        return map;
    }

    public static class Builder {

        private String buttonLabel;
        private CardToken cardToken;
        private String cardNumber;
        private String expiryMonth;
        private String expiryYear;
        private String amount;
        private String judoId;
        private String currency;
        private String consumerRef;
        private Bundle metaData;
        private String activityTitle;
        private String emailAddress;
        private String mobileNumber;
        private PendingIntent cardScanningIntent;
        private boolean secureServerMessageShown;
        private CustomLayout customLayout;

        public Builder setActivityTitle(String activityTitle) {
            this.activityTitle = activityTitle;
            return this;
        }

        public Builder setSecureServerMessageShown(boolean secureServerMessageShown) {
            this.secureServerMessageShown = secureServerMessageShown;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setJudoId(String judoId) {
            this.judoId = judoId.replaceAll("-", "");
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setConsumerRef(String consumerRef) {
            this.consumerRef = consumerRef;
            return this;
        }

        public Builder setMetaData(Bundle metaData) {
            this.metaData = metaData;
            return this;
        }

        public Builder setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder setExpiryMonth(String expiryMonth) {
            this.expiryMonth = expiryMonth;
            return this;
        }

        public Builder setExpiryYear(String expiryYear) {
            this.expiryYear = expiryYear;
            return this;
        }

        public Builder setButtonLabel(String buttonLabel) {
            this.buttonLabel = buttonLabel;
            return this;
        }

        public Builder setCardToken(CardToken cardToken) {
            this.cardToken = cardToken;
            return this;
        }

        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder setCustomLayout(CustomLayout customLayout) {
            this.customLayout = customLayout;
            return this;
        }

        public Builder setCardScanningIntent(PendingIntent cardScanningIntent) {
            this.cardScanningIntent = cardScanningIntent;
            return this;
        }

        public JudoOptions build() {
            if(isEmpty(judoId) || !isValid(judoId)) {
                throw new JudoIdInvalidError();
            }

            JudoOptions options = new JudoOptions();

            options.cardToken = cardToken;
            options.cardNumber = cardNumber;
            options.expiryMonth = expiryMonth;
            options.expiryYear = expiryYear;
            options.amount = amount;
            options.judoId = judoId;
            options.currency = currency;
            options.consumerRef = consumerRef;
            options.metaData = metaData;

            options.buttonLabel = buttonLabel;
            options.secureServerMessageShown = secureServerMessageShown;
            options.activityTitle = activityTitle;

            options.emailAddress = emailAddress;
            options.mobileNumber = mobileNumber;

            options.customLayout = customLayout;
            options.cardScanningIntent = cardScanningIntent;

            return options;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.judoId);
        dest.writeString(this.amount);
        dest.writeString(this.currency);
        dest.writeString(this.consumerRef);
        dest.writeBundle(metaData);
        dest.writeString(this.cardNumber);
        dest.writeString(this.expiryMonth);
        dest.writeString(this.expiryYear);
        dest.writeString(this.buttonLabel);
        dest.writeString(this.activityTitle);
        dest.writeParcelable(this.cardToken, flags);
        dest.writeString(this.emailAddress);
        dest.writeString(this.mobileNumber);
        dest.writeByte(secureServerMessageShown ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.cardScanningIntent, flags);
        dest.writeParcelable(this.customLayout, flags);
    }

    protected JudoOptions(Parcel in) {
        this.judoId = in.readString();
        this.amount = in.readString();
        this.currency = in.readString();
        this.consumerRef = in.readString();
        metaData = in.readBundle();
        this.cardNumber = in.readString();
        this.expiryMonth = in.readString();
        this.expiryYear = in.readString();
        this.buttonLabel = in.readString();
        this.activityTitle = in.readString();
        this.cardToken = in.readParcelable(CardToken.class.getClassLoader());
        this.emailAddress = in.readString();
        this.mobileNumber = in.readString();
        this.secureServerMessageShown = in.readByte() != 0;
        this.cardScanningIntent = in.readParcelable(PendingIntent.class.getClassLoader());
        this.customLayout = in.readParcelable(CustomLayout.class.getClassLoader());
    }

    public static final Parcelable.Creator<JudoOptions> CREATOR = new Parcelable.Creator<JudoOptions>() {
        @Override
        public JudoOptions createFromParcel(Parcel source) {
            return new JudoOptions(source);
        }

        @Override
        public JudoOptions[] newArray(int size) {
            return new JudoOptions[size];
        }
    };
}