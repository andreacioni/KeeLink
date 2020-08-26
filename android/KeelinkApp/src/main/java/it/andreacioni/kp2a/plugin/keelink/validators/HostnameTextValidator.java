package it.andreacioni.kp2a.plugin.keelink.validators;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import it.andreacioni.kp2a.plugin.keelink.keelink.KeelinkDefs;

public abstract class HostnameTextValidator implements TextWatcher {
    private static  final String HTTP_HOST_PORT_REGEX = "^https?://([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9][a-zA-Z0-9-_]+\\.[a-zA-Z]{2,11}?(:[0-9]{1,5})?$";

    private final EditText editText;

    public HostnameTextValidator(EditText editText) {
        this.editText = editText;
    }

    public void validate(EditText editText, String text) {
        if(!text.matches(HTTP_HOST_PORT_REGEX)) {
            editText.setError("http[s]://<host>[:<port>]");
            onValidationResultChange(false);
        } else {
            onValidationResultChange(true);
        }
    }

    public abstract void onValidationResultChange(boolean isValid);

    @Override
    public void afterTextChanged(Editable s) {
        String text = editText.getText().toString();
        validate(editText, text);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }
}
