package com.dandomi.pufas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dandomi.db.DatabaseExportUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.dandomi.pufas.pufas.AppState;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "StepperButtons";
    private static final String KEY_STEP_VALUE = "stepper_step";
    MaterialSwitch CheckForUpdateSw;
    MaterialSwitch disableMIMEFilterSw;
    MaterialSwitch syntaxHighlightingSw;
    MaterialSwitch dymamicColorSw;
    Spinner ThemeSpinner;
    ArrayAdapter<CharSequence> Themes;
    AppState state;
    TextView btnLoadDb;
    TextView btnViewDb;
    TextView btnClearDb;
    TextView btnCleanFrequentlyProducts;
    TextView btnCleanFrequentlyColors;
    TextView btnSetNumberFrequently;
    TextView btnChangeListLiters;
    LinearLayout btnStepSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialize();
        setLayoutBounds();

        LoadData();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        findViewById(R.id.mainSV).startAnimation(animation);

        ThemeSpinner.setSelection(state.getTheme());

        ThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state.setTheme(position);
                switch (position) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 2:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }

                SaveData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CheckForUpdateSw.setChecked(state.isAutoCheckForUpdate());
        disableMIMEFilterSw.setChecked(state.isMIMEFilterDisabled());
        syntaxHighlightingSw.setChecked(state.isSyntaxHighlighting());
        dymamicColorSw.setChecked(state.isChangeDynamicColor());


        CheckForUpdateSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setAutoCheckForUpdate(isChecked);
            SaveData();
        });

        disableMIMEFilterSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setMIMEFilterDisabled(isChecked);
            SaveData();
        });

        syntaxHighlightingSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setSyntaxHighlighting(isChecked);
            SaveData();
        });

        dymamicColorSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setChangeDynamicColor(isChecked);
            SaveData();
        });


        btnLoadDb.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImportDatabaseActivity.class);
            startActivity(intent);
        });

        btnViewDb.setOnClickListener(view -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    File jsonFile = DatabaseExportUtil.exportDatabase(this);

                    Uri uri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".provider",
                            jsonFile
                    );

                    Intent intent = new Intent(this, ImportDatabaseActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/json");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    runOnUiThread(() -> startActivity(intent));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    executor.shutdown();
                }
            });
        });

        btnChangeListLiters.setOnClickListener(view -> {

        });

        btnStepSize.setOnClickListener(v -> showStepSizeDialog());

    }

    private void showStepSizeDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentStep = prefs.getInt(KEY_STEP_VALUE, 1);

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_step_size, null);

        TextView tvValue = dialogView.findViewById(R.id.tv_dialog_value);
        Slider slider = dialogView.findViewById(R.id.slider_step);

        tvValue.setText(String.valueOf(currentStep));
        slider.setValue((float) currentStep);

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            tvValue.setText(String.valueOf((int) value));
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.set_step_liters)
                .setView(dialogView)
                .setIcon(R.drawable.ic_edit)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int newStep = (int) slider.getValue();
                    prefs.edit().putInt(KEY_STEP_VALUE, newStep).apply();
                })
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }

    private void setLayoutBounds() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootView), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets insetsN = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            layoutParams.leftMargin = insets.left + insetsN.left;
            layoutParams.topMargin = insets.top;
            layoutParams.rightMargin = insets.right + insetsN.right;
            layoutParams.bottomMargin = insets.bottom;
            v.setLayoutParams(layoutParams);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void LoadData() {
        state = FileSystem.loadStateData(this);
    }

    private void SaveData() {
        FileSystem.SaveState(this, state);
    }

    private void initialize() {
        CheckForUpdateSw = findViewById(R.id.CheckForUpdateSwitch);
        disableMIMEFilterSw = findViewById(R.id.MIMESwitch);
        syntaxHighlightingSw = findViewById(R.id.sHighlightingSwitch);
        dymamicColorSw = findViewById(R.id.ChangeDynamicColorSwitch);
        ThemeSpinner = findViewById(R.id.theme_spinner);
        Themes = ArrayAdapter.createFromResource(this, R.array.Themes, android.R.layout.simple_spinner_item);
        Themes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ThemeSpinner.setAdapter(Themes);
        btnLoadDb = findViewById(R.id.btnLoadDatabase);
        btnViewDb = findViewById(R.id.btnViewDatabase);
        btnClearDb = findViewById(R.id.btnClearDatabase);
        btnChangeListLiters = findViewById(R.id.btnChangeListLiters);

        btnStepSize= findViewById(R.id.btnStepSize);

    }

    public void goBack(View view) {
        finish();
    }

}