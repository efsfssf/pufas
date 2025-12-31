package com.dandomi.pufas;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.dandomi.db.Basepaint;
import com.dandomi.db.Formula;
import com.dandomi.pufas.controllers.SizesEditorFragment;
import com.dandomi.pufas.controllers.SizesRepository;
import com.dandomi.pufas.pufas.AppState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import java.util.Random;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.dandomi.about.AboutActivity;
import com.dandomi.db.Color;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.dandomi.db.Product;
import com.dandomi.logs.CustomExceptionHandler;
import com.dandomi.logs.LogActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextInputLayout productInput;
    AutoCompleteTextView productDropdown;

    TextInputLayout colorInput;
    AutoCompleteTextView colorDropdown;
    TextInputEditText canSizeEdit;
    TextInputLayout canSizeInput;

    MaterialButton calcButton;
    TextView baseWeight;
    TextView baseName;
    ShapeableImageView colorDot;
    TextView colorName;
    TextView colorData;
    MaterialToolbar topAppBar;

    private TableLayout resultTable;

    private MainViewModel viewModel;

    private Product selectedProduct = null;
    private Color selectedColor = null;
    private Basepaint selectedBase = null;

    static final String PREFS = "recent_items";
    static final String KEY_RECENT_COLORS = "recent_colors";
    static final String KEY_RECENT_PRODUCTS = "recent_products";
    static final String KEY_MAX_RECENT = "max_recent";
    private static final int DEFAULT_MAX_RECENT = 5;
    static final String KEY_MAX_HISTORY = "max_history";
    static final int DEFAULT_MAX_HISTORY = 50;
    static final String STEPPER_BUTTONS = "StepperButtons";
    static final String KEY_STEP_VALUE = "stepper_step";
    public static final String PREFS_HISTORY = "calculation_history";
    public static final String KEY_HISTORY_LIST = "history_list";
    private static final String KEY_THEME_SEED = "theme_seed_color";

    public AppState state;

    private List<Color> mCurrentColors = new ArrayList<>();
    private List<Product> mCurrentProducts = new ArrayList<>();


    private NestedScrollView nestedScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nestedScrollView = findViewById(R.id.scroll);
        MaterialAutoCompleteTextView colorDropdown = findViewById(R.id.colorDropdown);

        String[] colors = {
                "RAL 3014",
                "TVT F341",
                "White",
                "Black",
                "RAL 9005"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                colors
        );

        colorDropdown.setAdapter(adapter);

        // Ограничиваем высоту dropdown
        colorDropdown.setDropDownHeight(400);

        // При фокусе показываем dropdown и прокручиваем
        colorDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Небольшая задержка для корректной прокрутки
                v.postDelayed(() -> {
                    scrollToView(v);
                    colorDropdown.showDropDown();
                }, 100);
            }
        });

        // При клике также прокручиваем
        colorDropdown.setOnClickListener(v -> {
            scrollToView(v);
            colorDropdown.showDropDown();
        });

        // Отслеживаем появление клавиатуры
        setupKeyboardListener();
    }

    private void scrollToView(View view) {
        nestedScrollView.post(() -> {
            // Получаем позицию view
            int[] location = new int[2];
            view.getLocationInWindow(location);

            // Вычисляем сколько нужно прокрутить
            int viewTop = location[1];
            int scrollViewTop = nestedScrollView.getScrollY();
            int desiredPosition = viewTop + scrollViewTop - 200; // 200dp отступ сверху

            nestedScrollView.smoothScrollTo(0, desiredPosition);
        });
    }

    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { // Клавиатура видна
                // Находим view в фокусе
                View focusedView = getCurrentFocus();
                if (focusedView != null && focusedView.getId() == R.id.colorDropdown) {
                    focusedView.postDelayed(() -> scrollToView(focusedView), 100);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
    }
}