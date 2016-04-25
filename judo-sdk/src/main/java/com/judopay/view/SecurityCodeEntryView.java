package com.judopay.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.judopay.R;
import com.judopay.model.CardNetwork;
import com.judopay.validation.Validation;

/**
 * A view that allows for the security code of a card (CV2, CID) to be input and an image displayed to
 * indicate where on the payment card the security code can be located.
 */
public class SecurityCodeEntryView extends RelativeLayout {

    private EditText editText;
    private CardSecurityCodeView imageView;
    private TextInputLayout inputLayout;
    private HintFocusListener hintFocusListener;

    public SecurityCodeEntryView(Context context) {
        super(context);
        initialize(context);
    }

    public SecurityCodeEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public SecurityCodeEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_card_security_code, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        editText = (EditText) findViewById(R.id.security_code_edit_text);
        inputLayout = (TextInputLayout) findViewById(R.id.security_code_input_layout);
        imageView = (CardSecurityCodeView) findViewById(R.id.security_code_image_view);
        View helperText = findViewById(R.id.security_code_helper_text);

        hintFocusListener = new HintFocusListener(editText, "CVV");

        editText.setOnFocusChangeListener(new CompositeOnFocusChangeListener(
                new EmptyTextHintOnFocusChangeListener(helperText),
                new ViewAlphaChangingTextWatcher(editText, imageView),
                hintFocusListener
        ));
        editText.addTextChangedListener(new HidingViewTextWatcher(helperText));
    }

    public void setText(CharSequence text) {
        editText.setText(text);
    }

    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void setCardType(int cardType, boolean animate) {
        imageView.setCardType(cardType, animate);

        setAlternateHint(CardNetwork.securityCodeHint(cardType));

        inputLayout.setHint(CardNetwork.securityCode(cardType));
    }

    public void setMaxLength(int length) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
    }

    public void setHint(String hint) {
        inputLayout.setHint(hint);
    }

    public void setAlternateHint(String hint) {
        hintFocusListener.setHint(hint);
    }

    public String getText() {
        return editText.getText().toString().trim();
    }

    public EditText getEditText() {
        return editText;
    }

    public void setValidation(Validation validation) {
        inputLayout.setErrorEnabled(validation.isShowError());

        if (validation.isShowError()) {
            inputLayout.setError(getResources().getString(validation.getError()));
        } else {
            inputLayout.setError("");
        }
    }
}