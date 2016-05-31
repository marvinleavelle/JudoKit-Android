package com.judopay.customlayout;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.judopay.Judo;
import com.judopay.JudoOptions;
import com.judopay.PaymentActivity;
import com.judopay.R;
import com.judopay.model.Currency;
import com.judopay.model.CustomLayout;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CustomLayoutAutoAdvanceFieldsTest {

    @Rule
    public ActivityTestRule<PaymentActivity> testRule = new ActivityTestRule<>(PaymentActivity.class, false, false);

    @Before
    public void setupJudoSdk() {
        Judo.setEnvironment(Judo.UAT);
    }

    @Test
    public void shouldAutoAdvanceFields() {
        Judo.setAvsEnabled(true);
        testRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("4976000000003436"));

        onView(withId(R.id.expiry_date_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("1220"));

        onView(withId(R.id.security_code_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("452"));

        onView(withId(R.id.post_code_edit_text))
                .check(matches(hasFocus()));
    }

    @Test
    public void shouldAutoAdvanceMaestroFields() {
        Judo.setAvsEnabled(false);
        testRule.launchActivity(getIntent());

        onView(withId(R.id.card_number_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("6759000000005462"));

        onView(withId(R.id.start_date_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("0107"));

        onView(withId(R.id.issue_number_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("01"));

        onView(withId(R.id.expiry_date_edit_text))
                .perform(typeText("1220"));

        onView(withId(R.id.security_code_edit_text))
                .check(matches(hasFocus()))
                .perform(typeText("789"));
    }

    private Intent getIntent() {
        Intent intent = new Intent();

        intent.putExtra(Judo.JUDO_OPTIONS, new JudoOptions.Builder()
                .setJudoId("100915867")
                .setAmount("0.99")
                .setCustomLayout(new CustomLayout.Builder()
                        .cardNumberInput(R.id.card_number_input_layout)
                        .expiryDateInput(R.id.expiry_date_input_layout)
                        .securityCodeInput(R.id.security_code_input_layout)
                        .issueNumberInput(R.id.issue_number_input_layout)
                        .startDateInput(R.id.start_date_input_layout)
                        .countrySpinner(R.id.country_spinner)
                        .postcodeInput(R.id.post_code_input_layout)
                        .submitButton(R.id.payment_button)
                        .build(R.layout.custom_layout))
                .setCurrency(Currency.GBP)
                .setConsumerRef(UUID.randomUUID().toString())
                .build());

        return intent;
    }
}