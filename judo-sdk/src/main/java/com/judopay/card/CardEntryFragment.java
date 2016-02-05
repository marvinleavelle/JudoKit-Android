package com.judopay.card;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;

import com.judopay.CardNumberValidation;
import com.judopay.CountryAndPostcodeValidation;
import com.judopay.Judo;
import com.judopay.JudoOptions;
import com.judopay.PaymentForm;
import com.judopay.PaymentFormValidation;
import com.judopay.R;
import com.judopay.StartDateAndIssueNumberValidation;
import com.judopay.model.Address;
import com.judopay.model.Card;
import com.judopay.model.CardToken;
import com.judopay.model.CardType;
import com.judopay.model.Country;
import com.judopay.view.CardNumberEntryView;
import com.judopay.view.CountrySpinner;
import com.judopay.view.CvvEntryView;
import com.judopay.view.ExpiryDateEntryView;
import com.judopay.view.IssueNumberEntryView;
import com.judopay.view.PostcodeEntryView;
import com.judopay.view.ScrollOnFocusChangeListener;
import com.judopay.view.SimpleTextWatcher;
import com.judopay.view.SingleClickOnClickListener;
import com.judopay.view.StartDateEntryView;

import static com.judopay.Judo.isAvsEnabled;

/**
 * A Fragment that allows for card details to be entered by the user, with validation checks
 * on input data.
 * Configuration options can be provided by passing a {@link JudoOptions} instance in the fragment
 * arguments, identified using the {@link Judo#JUDO_OPTIONS} as a key, e.g.
 * <code>
 * CardEntryFragment fragment = new CardEntryFragment();
 * Bundle args = new Bundle();
 *
 * args.putParcelable(Judo.JUDO_OPTIONS, new JudoOptions.Builder()
 *      .setJudoId("123456")
 *      .setAmount("1.99")
 *      .setCurrency(Currency.USD)
 *      .setButtonLabel("Perform payment")
 *      .setSecureServerMessageShown(true)
 *      .build());
 *
 * fragment.setArguments(args);
 * </code>
 */
public final class CardEntryFragment extends Fragment {

    private Button paymentButton;
    private CountrySpinner countrySpinner;
    private CvvEntryView cvvEntryView;
    private CardNumberEntryView cardNumberEntryView;

    private View startDateAndIssueNumberContainer;
    private View countryAndPostcodeContainer;
    private ScrollView scrollView;
    private View cardsAcceptedErrorText;
    private IssueNumberEntryView issueNumberEntryView;
    private PostcodeEntryView postcodeEntryView;
    private StartDateEntryView startDateEntryView;

    private View secureServerText;

    private JudoOptions judoOptions;
    private CardEntryListener cardEntryListener;
    private ExpiryDateEntryView expiryDateEntryView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_entry, container, false);

        paymentButton = (Button) view.findViewById(R.id.payment_button);

        cvvEntryView = (CvvEntryView) view.findViewById(R.id.cvv_entry_view);
        cardNumberEntryView = (CardNumberEntryView) view.findViewById(R.id.card_number_entry_view);
        expiryDateEntryView = (ExpiryDateEntryView) view.findViewById(R.id.expiry_date_entry_view);

        postcodeEntryView = (PostcodeEntryView) view.findViewById(R.id.postcode_entry_view);

        countrySpinner = (CountrySpinner) view.findViewById(R.id.country_spinner);
        startDateEntryView = (StartDateEntryView) view.findViewById(R.id.start_date_entry_view);

        issueNumberEntryView = (IssueNumberEntryView) view.findViewById(R.id.issue_number_entry_view);

        startDateAndIssueNumberContainer = view.findViewById(R.id.start_date_issue_number_container);
        countryAndPostcodeContainer = view.findViewById(R.id.country_postcode_container);
        scrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        cardsAcceptedErrorText = view.findViewById(R.id.cards_accepted_error_text);
        secureServerText = view.findViewById(R.id.secure_server_text);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SimpleTextWatcher validationWatcher = new SimpleTextWatcher() {
            @Override
            protected void onTextChanged() {
                updateFormView();
            }
        };

        if (getArguments() != null && getArguments().containsKey(Judo.JUDO_OPTIONS)) {
            this.judoOptions = getArguments().getParcelable(Judo.JUDO_OPTIONS);

            if (judoOptions != null) {
                if (judoOptions.getButtonLabel() != null) {
                    this.paymentButton.setText(judoOptions.getButtonLabel());
                }

                CardToken cardToken = judoOptions.getCardToken();
                if (cardToken != null) {
                    cardNumberEntryView.setCardType(cardToken.getType());
                    cvvEntryView.setCardType(cardToken.getType());
                    cvvEntryView.requestFocus();
                }

                if (judoOptions.getCardNumber() != null) {
                    cardNumberEntryView.setText(judoOptions.getCardNumber());

                    int cardType = CardType.fromCardNumber(judoOptions.getCardNumber());
                    cardNumberEntryView.setCardType(cardType);
                    expiryDateEntryView.requestFocus();
                }

                if (judoOptions.getExpiryYear() != null && judoOptions.getExpiryMonth() != null) {
                    expiryDateEntryView.setText(getString(R.string.expiry_date_format, judoOptions.getExpiryMonth(), judoOptions.getExpiryYear()));
                    cvvEntryView.requestFocus();
                }

                if (judoOptions.isSecureServerMessageShown()) {
                    secureServerText.setVisibility(View.VISIBLE);
                } else {
                    secureServerText.setVisibility(View.GONE);
                }

            }
        }

        initialiseCardNumber(validationWatcher);
        initialiseExpiryDate(validationWatcher);

        cvvEntryView.addTextChangedListener(validationWatcher);
        startDateEntryView.addTextChangedListener(validationWatcher);
        issueNumberEntryView.addTextChangedListener(validationWatcher);

        initialisePostcode(validationWatcher);
        initialiseCountry();

        initialisePayButton();
    }

    private void initialisePayButton() {
        paymentButton.setOnClickListener(new SingleClickOnClickListener() {
            @Override
            public void doClick() {
                hideKeyboard();
                submitForm();
            }
        });
    }

    private void initialiseCountry() {
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFormView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initialisePostcode(SimpleTextWatcher formValidator) {
        postcodeEntryView.addTextChangedListener(formValidator);
        postcodeEntryView.addOnFocusChangeListener(new ScrollOnFocusChangeListener(scrollView));
    }

    private void initialiseExpiryDate(SimpleTextWatcher formValidator) {
        if (judoOptions.getCardToken() == null) {
            expiryDateEntryView.addTextChangedListener(formValidator);
        } else {
            expiryDateEntryView.setTokenized(true);
        }
    }

    private void initialiseCardNumber(SimpleTextWatcher formValidator) {
        if (judoOptions.getCardToken() == null) {
            cardNumberEntryView.addTextChangedListener(formValidator);
        } else {
            cardNumberEntryView.setTokenizedNumber(judoOptions.getCardToken().getLastFour());
        }
    }

    private void hideKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateFormView() {
        PaymentForm.Builder builder = new PaymentForm.Builder()
                .setCardNumber(cardNumberEntryView.getText())
                .setCvv(cvvEntryView.getText())
                .setCountry(getCountry())
                .setPostcode(postcodeEntryView.getText())
                .setIssueNumber(issueNumberEntryView.getText())
                .setExpiryDate(expiryDateEntryView.getText())
                .setStartDate(startDateEntryView.getText())
                .setAddressRequired(Judo.isAvsEnabled())
                .setAmexSupported(Judo.isAmexEnabled())
                .setMaestroSupported(Judo.isMaestroEnabled());

        CardToken cardToken = judoOptions.getCardToken();

        if (cardToken != null) {
            builder.setTokenCard(true)
                    .setCardType(cardToken.getType());
        }

        PaymentFormValidation formView = new PaymentFormValidation.Builder()
                .build(builder.build());

        if (cardToken == null) {
            cardNumberEntryView.setCardType(formView.getCardType());
        }

        updateFormErrors(formView);
        moveFieldFocus(formView);
    }

    private void updateFormErrors(PaymentFormValidation formView) {
        showCardNumberErrors(formView.getCardNumberValidation());

        showExpiryDateErrors(formView);

        showStartDateAndIssueNumberErrors(formView.getStartDateAndIssueNumberState());

        updateCvvErrors(formView);

        updateCountryAndPostcode(formView.getCountryAndPostcodeValidation());

        paymentButton.setVisibility(formView.isPaymentButtonEnabled() ? View.VISIBLE : View.GONE);
    }

    private void updateCvvErrors(PaymentFormValidation formView) {
        cvvEntryView.setHint(formView.getCvvLabel());
        cvvEntryView.setAlternateHint(formView.getCvvHint());

        cvvEntryView.setMaxLength(formView.getCvvLength());
        cvvEntryView.setCardType(formView.getCardType());
    }

    private void showStartDateAndIssueNumberErrors(StartDateAndIssueNumberValidation startDateAndIssueNumberValidation) {
        startDateEntryView.setError(startDateAndIssueNumberValidation.getStartDateError(),
                startDateAndIssueNumberValidation.isShowStartDateError());

        startDateAndIssueNumberContainer.setVisibility(startDateAndIssueNumberValidation.isShowIssueNumberAndStartDate()
                ? View.VISIBLE : View.GONE);
    }

    private void updateCountryAndPostcode(CountryAndPostcodeValidation validation) {
        countryAndPostcodeContainer.setVisibility(validation.isShowCountryAndPostcode() ? View.VISIBLE : View.GONE);

        postcodeEntryView.setHint(validation.getPostcodeLabel());
        postcodeEntryView.setError(validation.getPostcodeError(), validation.isShowPostcodeError());

        postcodeEntryView.setNumericInput(validation.isPostcodeNumeric());
        postcodeEntryView.setSelectionEnd();

        cardsAcceptedErrorText.setVisibility(validation.isCountryValid() ? View.GONE : View.VISIBLE);
        postcodeEntryView.setVisibility(validation.isCountryValid() ? View.VISIBLE : View.INVISIBLE);
    }

    private void showExpiryDateErrors(PaymentFormValidation formView) {
        expiryDateEntryView.setError(formView.getExpiryDateError(), formView.isShowExpiryDateError());
    }

    private void showCardNumberErrors(CardNumberValidation cardNumberValidation) {
        cardNumberEntryView.setError(cardNumberValidation.getError(), cardNumberValidation.isShowError());
        cardNumberEntryView.setMaxLength(cardNumberValidation.getMaxLength());
    }

    private void moveFieldFocus(PaymentFormValidation formView) {
        if (cardNumberEntryView.hasFocus() && formView.getCardNumberValidation().isEntryComplete() && !formView.getCardNumberValidation().isShowError()) {
            if (startDateAndIssueNumberContainer.getVisibility() == View.VISIBLE) {
                startDateEntryView.requestFocus();
            } else {
                expiryDateEntryView.requestFocus();
            }
        } else if (expiryDateEntryView.hasFocus() && formView.isExpiryDateEntryComplete() && !formView.isShowExpiryDateError()) {
            cvvEntryView.requestFocus();
        } else if (cvvEntryView.hasFocus() && formView.isCvvValid()) {
            if (countryAndPostcodeContainer.getVisibility() == View.VISIBLE) {
                postcodeEntryView.requestFocus();
            }
        } else if (startDateEntryView.hasFocus()
                && formView.getStartDateAndIssueNumberState().isStartDateEntryComplete()
                && !formView.getStartDateAndIssueNumberState().isShowStartDateError()) {
            issueNumberEntryView.requestFocus();
        }
    }

    private Country getCountry() {
        return countrySpinner.getSelectedCountry();
    }

    private void submitForm() {
        Card.Builder cardBuilder = new Card.Builder()
                .setCardNumber(cardNumberEntryView.getText())
                .setExpiryDate(expiryDateEntryView.getText())
                .setCvv(cvvEntryView.getText());

        Address.Builder addressBuilder = new Address.Builder()
                .setPostCode(postcodeEntryView.getText());

        if (isAvsEnabled()) {
            addressBuilder.setCountryCode(countrySpinner.getSelectedCountry().getCode());
        }

        cardBuilder.setCardAddress(addressBuilder.build());

        if (cardNumberEntryView.getCardType() == CardType.MAESTRO) {
            cardBuilder.setIssueNumber(issueNumberEntryView.getText())
                    .setStartDate(startDateEntryView.getText());
        }

        if (cardEntryListener != null) {
            cardEntryListener.onSubmit(cardBuilder.build());
        }
    }

    public static CardEntryFragment newInstance(JudoOptions judoOptions, CardEntryListener listener) {
        CardEntryFragment cardEntryFragment = new CardEntryFragment();
        cardEntryFragment.setCardEntryListener(listener);

        Bundle arguments = new Bundle();
        arguments.putParcelable(Judo.JUDO_OPTIONS, judoOptions);
        cardEntryFragment.setArguments(arguments);

        return cardEntryFragment;
    }

    public static CardEntryFragment newInstance(CardEntryListener paymentListener) {
        CardEntryFragment cardEntryFragment = new CardEntryFragment();
        cardEntryFragment.setCardEntryListener(paymentListener);

        return cardEntryFragment;
    }

    public void setCardEntryListener(CardEntryListener cardEntryListener) {
        this.cardEntryListener = cardEntryListener;
    }

}