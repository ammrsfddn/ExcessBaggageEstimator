package com.ammar.excessbaggage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ammar.excessbaggage.logic.BaggageCalculator;
import com.ammar.excessbaggage.model.BaggageResult;
import com.ammar.excessbaggage.model.TicketClass;
import com.ammar.excessbaggage.util.ShareHelper;
import com.ammar.excessbaggage.util.SimpleTextWatcher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Main screen: 3 inputs -> 3 outputs. */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BaggageFee";
    private static final int MAX_WEIGHT_KG = 200;

    private TextInputLayout tilWeight, tilRate;
    private TextInputEditText etWeight, etRate;
    private RadioGroup rgTicketClass;
    private TextView tvAllowanceNote, tvLiveNotice;

    private View cardResult;
    private TextView tvExcessWeight, tvExcessCharge, tvChargeFormula, tvTotalPayable, tvTaxNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        setupListeners();
        updateAllowanceNote();
    }

    private void bindViews() {
        tilWeight = findViewById(R.id.tilWeight);
        tilRate = findViewById(R.id.tilRate);
        etWeight = findViewById(R.id.etWeight);
        etRate = findViewById(R.id.etRate);
        rgTicketClass = findViewById(R.id.rgTicketClass);
        tvAllowanceNote = findViewById(R.id.tvAllowanceNote);
        tvLiveNotice = findViewById(R.id.tvLiveNotice);

        cardResult = findViewById(R.id.cardResult);
        tvExcessWeight = findViewById(R.id.tvExcessWeight);
        tvExcessCharge = findViewById(R.id.tvExcessCharge);
        tvChargeFormula = findViewById(R.id.tvChargeFormula);
        tvTotalPayable = findViewById(R.id.tvTotalPayable);
        tvTaxNote = findViewById(R.id.tvTaxNote);
    }

    private void setupListeners() {
        findViewById(R.id.btnCalculate).setOnClickListener(v -> onCalculateClicked());
        findViewById(R.id.btnReset).setOnClickListener(v -> resetForm());

        rgTicketClass.setOnCheckedChangeListener((group, checkedId) -> {
            updateAllowanceNote();
            updateLiveNotice();
        });

        etWeight.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                tilWeight.setError(null);
                updateLiveNotice();
            }
        });
        etRate.addTextChangedListener(new SimpleTextWatcher() {
            @Override public void afterTextChanged(Editable s) { tilRate.setError(null); }
        });

        etRate.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onCalculateClicked();
                return true;
            }
            return false;
        });
    }

    private void onCalculateClicked() {
        hideKeyboard();

        Double weight = readPositiveNumber(etWeight, tilWeight,
                R.string.error_weight_empty, R.string.error_weight_invalid);
        Double rate = readPositiveNumber(etRate, tilRate,
                R.string.error_rate_empty, R.string.error_rate_invalid);

        if (weight != null && weight > MAX_WEIGHT_KG) {
            tilWeight.setError(getString(R.string.error_weight_too_high, MAX_WEIGHT_KG));
            weight = null;
        }

        if (weight == null || rate == null) {
            cardResult.setVisibility(View.GONE);
            Snackbar.make(cardResult, R.string.msg_fix_errors, Snackbar.LENGTH_SHORT).show();
            return;
        }

        TicketClass ticketClass = getSelectedTicketClass();
        BaggageResult result = BaggageCalculator.calculate(weight, ticketClass, rate);

        Log.d(TAG, "weight=" + weight + " class=" + ticketClass + " rate=" + rate
                + " -> excess=" + result.getExcessWeightKg()
                + " charge=" + result.getExcessCharge()
                + " total=" + result.getTotalPayable());

        showResult(result);

        if (result.getExcessWeightKg() == 0) {
            int allowance = (int) ticketClass.getFreeAllowanceKg();
            Snackbar.make(cardResult, getString(R.string.msg_no_excess, allowance),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void showResult(BaggageResult r) {
        tvExcessWeight.setText(getString(R.string.value_kg, r.getExcessWeightKg()));
        tvExcessCharge.setText(getString(R.string.value_rm, r.getExcessCharge()));
        tvChargeFormula.setText(getString(R.string.formula_charge,
                r.getExcessWeightKg(), r.getRatePerKg()));
        tvTotalPayable.setText(getString(R.string.value_rm, r.getTotalPayable()));
        tvTaxNote.setText(getString(R.string.tax_note, r.getServiceTax()));

        cardResult.setVisibility(View.VISIBLE);
    }

    /** Live notice: tells the user straight away if the bag is over or under the allowance. */
    private void updateLiveNotice() {
        String text = etWeight.getText() == null ? "" : etWeight.getText().toString().trim();
        if (text.isEmpty()) {
            tvLiveNotice.setVisibility(View.GONE);
            return;
        }
        try {
            double weight = Double.parseDouble(text);
            if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0) {
                tvLiveNotice.setVisibility(View.GONE);
                return;
            }
            int allowance = (int) getSelectedTicketClass().getFreeAllowanceKg();
            double diff = weight - allowance;

            if (diff > 0) {
                tvLiveNotice.setText(getString(R.string.notice_over, diff, allowance));
            } else {
                tvLiveNotice.setText(getString(R.string.notice_ok, allowance, -diff));
            }
            tvLiveNotice.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            tvLiveNotice.setVisibility(View.GONE);
        }
    }

    private Double readPositiveNumber(TextInputEditText field, TextInputLayout layout,
                                      int emptyMsg, int invalidMsg) {
        String text = field.getText() == null ? "" : field.getText().toString().trim();

        if (text.isEmpty()) {
            layout.setError(getString(emptyMsg));
            return null;
        }
        try {
            double value = Double.parseDouble(text);
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                layout.setError(getString(invalidMsg));
                return null;
            }
            layout.setError(null);
            return value;
        } catch (NumberFormatException e) {
            Log.w(TAG, "Bad number: " + text, e);
            layout.setError(getString(invalidMsg));
            return null;
        }
    }

    private TicketClass getSelectedTicketClass() {
        return rgTicketClass.getCheckedRadioButtonId() == R.id.rbBusiness
                ? TicketClass.BUSINESS : TicketClass.ECONOMY;
    }

    private void updateAllowanceNote() {
        int allowance = (int) getSelectedTicketClass().getFreeAllowanceKg();
        tvAllowanceNote.setText(getString(R.string.allowance_note, allowance));
    }

    private void resetForm() {
        etWeight.setText("");
        etRate.setText("");
        tilWeight.setError(null);
        tilRate.setError(null);
        rgTicketClass.check(R.id.rbEconomy);
        cardResult.setVisibility(View.GONE);
        tvLiveNotice.setVisibility(View.GONE);
        etWeight.requestFocus();
    }

    private void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            ShareHelper.shareApp(this);
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}