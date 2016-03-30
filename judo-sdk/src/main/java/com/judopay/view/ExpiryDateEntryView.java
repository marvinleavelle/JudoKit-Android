package com.judopay.view;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.judopay.R;

/**
 * A view that allows for a card expiry date to be entered or for a tokenized expiry number to be shown.
 */
public class ExpiryDateEntryView extends LinearLayout {

    private EditText expiryDateEditText;
    private TextInputLayout expiryDateInputLayout;

    public ExpiryDateEntryView(Context context) {
        super(context);
        initialize(context);
    }

    public ExpiryDateEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ExpiryDateEntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_expiry_date_entry, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        expiryDateEditText = (EditText) findViewById(R.id.expiry_date_edit_text);
        expiryDateInputLayout = (TextInputLayout) findViewById(R.id.expiry_date_input_layout);

        HintFocusListener hintFocusListener = new HintFocusListener(expiryDateEditText, R.string.date_hint);
        expiryDateEditText.setOnFocusChangeListener(hintFocusListener);

        String dateFormat = getResources().getString(R.string.date_format);
        NumberFormatTextWatcher numberFormatTextWatcher = new NumberFormatTextWatcher(expiryDateEditText, dateFormat, true);
        expiryDateEditText.addTextChangedListener(numberFormatTextWatcher);
    }

    public void setText(CharSequence text) {
        expiryDateEditText.setText(text);
    }

    public void addTextChangedListener(SimpleTextWatcher watcher) {
        expiryDateEditText.addTextChangedListener(watcher);
    }

    public void setExpiryDate(String expiryDate) {
        expiryDateEditText.setText(expiryDate);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        expiryDateEditText.setEnabled(false);
    }

    public String getText() {
        return expiryDateEditText.getText().toString().trim();
    }

    public void setError(int error, boolean show) {
        expiryDateInputLayout.setErrorEnabled(show);

        if (show) {
            expiryDateInputLayout.setError(getResources().getString(error));
        } else {
            expiryDateInputLayout.setError("");
        }
    }

}