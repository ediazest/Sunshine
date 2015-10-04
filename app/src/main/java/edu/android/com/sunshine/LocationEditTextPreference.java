package edu.android.com.sunshine;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;

import com.example.android.sunshine.app.R;

/**
 * Created by edu on 04/10/2015.
 */
public class LocationEditTextPreference extends EditTextPreference implements TextWatcher {

    private final static int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.LocationEditTextPreference,
                0, 0);

        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        getEditText().addTextChangedListener(this);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        Dialog d = getDialog();

        if (d instanceof AlertDialog) {
            AlertDialog dialog = (AlertDialog) d;

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (s.length() < mMinLength) {
                positiveButton.setEnabled(false);
            } else {
                positiveButton.setEnabled(true);
            }


        }

    }
}
