package com.dandomi.pufas;

import static com.dandomi.pufas.MainActivity.DEFAULT_MAX_HISTORY;
import static com.dandomi.pufas.MainActivity.KEY_HISTORY_LIST;
import static com.dandomi.pufas.MainActivity.KEY_MAX_HISTORY;
import static com.dandomi.pufas.MainActivity.KEY_MAX_RECENT;
import static com.dandomi.pufas.MainActivity.KEY_RECENT_COLORS;
import static com.dandomi.pufas.MainActivity.KEY_RECENT_PRODUCTS;
import static com.dandomi.pufas.MainActivity.KEY_STEP_VALUE;
import static com.dandomi.pufas.MainActivity.PREFS;
import static com.dandomi.pufas.MainActivity.PREFS_HISTORY;
import static com.dandomi.pufas.MainActivity.STEPPER_BUTTONS;

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

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dandomi.adapters.HistoryAdapter;
import com.dandomi.db.AppDatabase;
import com.dandomi.db.DatabaseExportUtil;
import com.dandomi.pufas.controllers.SizesEditorFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.dandomi.pufas.pufas.AppState;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    MaterialSwitch CheckForUpdateSw;
    MaterialSwitch disableMIMEFilterSw;
    MaterialSwitch syntaxHighlightingSw;
    MaterialSwitch dymamicColorSw;
    Spinner ThemeSpinner;
    ArrayAdapter<CharSequence> Themes;
    AppState state;
    LinearLayout btnLoadDb;
    LinearLayout btnViewDb;
    LinearLayout btnClearDb;
    LinearLayout btnClearFrequentlyUsedProducts;
    LinearLayout btnClearFrequentlyUsedColors;
    LinearLayout btnSetNumberFrequently;
    LinearLayout btnChangeListLiters;
    LinearLayout btnSetLenghtHistory;
    LinearLayout btnClearHistory;
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

        btnClearDb.setOnClickListener(view -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {
                try {

                    File dbFile = getDbFile();
                    File backupFile = getBackupFile();
                    copyFile(dbFile, backupFile);

                    AppDatabase db = AppDatabase.getDbInstance(this);
                    db.clearAllTables();

                    runOnUiThread(() -> {
                        Snackbar.make(view, R.string.db_cleared, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v -> restoreDatabase())
                                .show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    executor.shutdown();
                }
            });
        });

        btnClearFrequentlyUsedProducts.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

            prefs.edit().remove(KEY_RECENT_PRODUCTS).apply();

            Snackbar.make(view, R.string.frequently_used_products_removed, Snackbar.LENGTH_LONG).show();
        });

        btnClearFrequentlyUsedColors.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

            prefs.edit().remove(KEY_RECENT_COLORS).apply();

            Snackbar.make(view, R.string.frequently_used_colors_removed, Snackbar.LENGTH_LONG).show();
        });

        btnSetNumberFrequently.setOnClickListener(view -> showSliderDialog(
                R.string.set_number_frequently,
                PREFS,
                KEY_MAX_RECENT,
                5,
                1,
                10
        ));

        btnChangeListLiters.setOnClickListener(view -> {
            SizesEditorFragment bottomSheet = new SizesEditorFragment();
            bottomSheet.show(getSupportFragmentManager(), "SizesEditor");
        });

        btnSetLenghtHistory.setOnClickListener(view -> showHistoryLenghtDialog());

        btnClearHistory.setOnClickListener(view -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
            Gson gson = new Gson();

            String json = prefs.getString(KEY_HISTORY_LIST, "");
            List<HistoryItem> backupList;

            if (!(json.isEmpty() || json.equals("[]"))) {
                Type type = new TypeToken<List<HistoryItem>>() {}.getType();
                backupList = gson.fromJson(json, type);
            } else {
                backupList = new ArrayList<>();
            }

            prefs.edit().remove(KEY_HISTORY_LIST).apply();

            Snackbar.make(view, R.string.history_cleared, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, view1 -> {
                        saveListToPrefs(backupList);
                    })
                    .show();
        });


        btnStepSize.setOnClickListener(v -> showStepSizeDialog());

    }

    private void restoreDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                AppDatabase.closeInstance();

                File dbFile = getDbFile();
                File backupFile = getBackupFile();

                if (backupFile.exists()) {
                    copyFile(backupFile, dbFile);
                }

                AppDatabase.getDbInstance(this);

                runOnUiThread(() -> {
                    Snackbar.make(findViewById(R.id.mainSV), R.string.db_restored, Snackbar.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                executor.shutdown();
            }
        });
    }

    private File getDbFile() {
        return getApplicationContext().getDatabasePath("avatintlocal");
    }

    private File getBackupFile() {
        return new File(getCacheDir(), "avatintlocal_backup");
    }

    private void copyFile(File from, File to) throws IOException {
        try (FileChannel src = new FileInputStream(from).getChannel();
             FileChannel dst = new FileOutputStream(to).getChannel()) {
            dst.transferFrom(src, 0, src.size());
        }
    }

    private void saveListToPrefs(List<HistoryItem> list) {
        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (list == null || list.isEmpty()) {
            editor.remove(KEY_HISTORY_LIST);
        } else {
            // Превращаем список объектов в строку JSON и сохраняем
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(KEY_HISTORY_LIST, json);
        }

        editor.apply();
    }

    private void showHistoryLenghtDialog() {
        showSliderDialog(
                R.string.set_history_limit, //  заголовок
                KEY_MAX_HISTORY,            // Имя файла
                KEY_MAX_HISTORY,            // Ключ
                DEFAULT_MAX_HISTORY,        // Дефолт
                10,                      // Мин. история
                100                      // Макс. история
        );
    }

    private void showStepSizeDialog() {
        showSliderDialog(
                R.string.set_step_liters,
                STEPPER_BUTTONS,            // Имя файла
                KEY_STEP_VALUE,             // Ключ
                1,                          // Дефолт
                1,                       // Мин. шаг
                2                       // Макс. шаг
        );
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

    private void showSliderDialog(
            @StringRes int titleResId,
            String prefsFileName,
            String prefsKey,
            int defaultValue,
            int min,
            int max
    ) {
        SharedPreferences prefs = getSharedPreferences(prefsFileName, MODE_PRIVATE);
        int currentValue = prefs.getInt(prefsKey, defaultValue);

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_step_size, null);

        TextView tvValue = dialogView.findViewById(R.id.tv_dialog_value);
        Slider slider = dialogView.findViewById(R.id.slider_step);

        slider.setValueFrom(min);
        slider.setValueTo(max);
        slider.setValue((float) currentValue);

        tvValue.setText(String.valueOf(currentValue));

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            tvValue.setText(String.valueOf((int) value));
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(titleResId)
                .setView(dialogView)
                .setIcon(R.drawable.ic_edit)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int newValue = (int) slider.getValue();
                    prefs.edit().putInt(prefsKey, newValue).apply();
                })
                .setNegativeButton(R.string.Cancel, null)
                .show();
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
        btnClearFrequentlyUsedProducts = findViewById(R.id.btnClearFrequentlyUsedProducts);
        btnClearFrequentlyUsedColors = findViewById(R.id.btnClearFrequentlyUsedColors);
        btnSetNumberFrequently = findViewById(R.id.btnSetNumberFrequently);
        btnChangeListLiters = findViewById(R.id.btnChangeListLiters);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        btnSetLenghtHistory = findViewById(R.id.btnSetLenghtHistory);

        btnStepSize= findViewById(R.id.btnStepSize);

    }

    public void goBack(View view) {
        finish();
    }

}