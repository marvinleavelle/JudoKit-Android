package com.judopay.error;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.judopay.Judo;
import com.judopay.JudoOptions;
import com.judopay.PaymentActivity;
import com.judopay.R;
import com.judopay.model.Currency;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PaymentFormErrorMessageTest {

    @Rule
    public ActivityTestRule<PaymentActivity> activityTestRule = new ActivityTestRule<>(PaymentActivity.class, false, false);

    @Test
    public void shouldDisplayErrorMessageWhenInvalidCardNumberEntered() {
        activityTestRule.launchActivity(getIntent());

        onView(ViewMatchers.withId(R.id.card_number_edit_text))
                .perform(typeText("1234000000001234"));

        onView(withText(R.string.check_card_number))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayErrorMessageWhenPastExpiryDateEntered() {
        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.expiry_date_edit_text))
                .perform(typeText("0101"));

        onView(withText(R.string.check_expiry_date))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayErrorWhenExpiryDateMoreThanTenYearsInFuture() {
        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.expiry_date_edit_text))
                .perform(typeText("1230"));

        onView(withText(R.string.check_expiry_date))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayErrorMessageWhenFutureStartDateEntered() {
        Judo.setMaestroEnabled(true);

        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("6759000000005462"));

        onView(withId(R.id.start_date_edit_text))
                .perform(typeText("0125"));

        onView(withText(R.string.check_start_date))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayErrorMessageWhenStartDateOlderThanTenYearsEntered() {
        Judo.setMaestroEnabled(true);

        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("6759000000005462"));

        onView(withId(R.id.start_date_edit_text))
                .perform(typeText("1205"));

        onView(withText(R.string.check_start_date))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayAmexNotSupportedWhenAmexEnteredAndNotEnabled() {
        Judo.setAmexEnabled(false);

        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("3400"));

        onView(withText(R.string.error_amex_not_supported))
                .check(matches(isDisplayed()));
    }

    @Test
    public void shouldDisplayMaestroNotSupportedWhenMaestroEnteredAndNotEnabled() {
        Judo.setMaestroEnabled(false);

        activityTestRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("6759"));

        onView(withText(R.string.error_maestro_not_supported))
                .check(matches(isDisplayed()));
    }

    protected Intent getIntent() {
        Intent intent = new Intent();

        intent.putExtra(Judo.JUDO_OPTIONS, new JudoOptions.Builder()
                .setJudoId("100915867")
                .setAmount("0.99")
                .setCurrency(Currency.GBP)
                .setConsumerRef(UUID.randomUUID().toString())
                .build());
        return intent;
    }

}