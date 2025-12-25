package com.dandomi.pufas;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.dandomi.pufas.pufas.AppState;
import com.sjapps.jsonlist.R;

public class SettingsActivity extends AppCompatActivity {

    MaterialSwitch CheckForUpdateSw;
    MaterialSwitch disableMIMEFilterSw;
    MaterialSwitch syntaxHighlightingSw;
    MaterialSwitch dymamicColorSw;
    Spinner ThemeSpinner;
    ArrayAdapter<CharSequence> Themes;
    AppState state;
    TextView btnLoadDb;
    TextView btnViewDb;

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
        //dymamicColorSw.setChecked(state.isChangeDynamicColor());


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
            //state.setChangeDynamicColor(isChecked);
            SaveData();
        });


        btnLoadDb.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImportDatabaseActivity.class);
            startActivity(intent);
        });



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

    }

    public void goBack(View view) {
        finish();
    }

}