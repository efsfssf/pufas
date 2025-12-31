package com.dandomi.pufas;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.dandomi.db.Basepaint;
import com.dandomi.db.Formula;
import com.dandomi.pufas.controllers.SizesRepository;
import com.dandomi.pufas.pufas.AppState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;
import java.util.Random;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;
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
    androidx.appcompat.widget.Toolbar topAppBar;
    MaterialButton togglePointsBtn;
    NestedScrollView scrollView;

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

    private boolean hidePoints = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        applyThemeFromPrefs();
        super.onCreate(savedInstanceState);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this)
                .get(MainViewModel.class);

        checkDatabaseAndRedirectIfEmpty();

        initViews();
        setupListeners();
        setupKeyboardListener();


        setupQuickSizeButtons();
        setupStepperButtons();

        observeData();

        restoreCalculationStateIfAvailable();

        clearAllFocus();

        setSupportActionBar(topAppBar);

        topAppBar.setNavigationIcon(R.drawable.menu_24px);

        topAppBar.setNavigationOnClickListener(v -> {
            showExpressiveMenu();
        });

        state = FileSystem.loadStateData(this);

    }

    // TODO: ПЕРЕДЕЛАТЬ. ВРЕМЕННЫЙ ФИКС ПЕРЕКРЫТИЯ КЛАВИАТУРОЙ ПОЛЯ ВВОДА
    private void scrollToViewImproved(View view) {
        scrollView.post(() -> {
            // Получаем координаты view относительно экрана
            int[] viewLocation = new int[2];
            view.getLocationOnScreen(viewLocation);

            // Получаем координаты scrollView
            int[] scrollLocation = new int[2];
            scrollView.getLocationOnScreen(scrollLocation);

            // Высота экрана
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // Высота AppBar (учитываем CollapsingToolbar)
            View appBar = findViewById(R.id.topAppBar);
            int appBarHeight = appBar != null ? appBar.getHeight() : 0;

            // Примерная высота клавиатуры (40% экрана)
            int keyboardHeight = (int) (screenHeight * 0.4);

            // Высота dropdown
            int dropdownHeight = dpToPx(300);

            // Вычисляем нужную позицию прокрутки
            int viewTopRelativeToScroll = viewLocation[1] - scrollLocation[1];
            int currentScroll = scrollView.getScrollY();

            // Целевая позиция: view должен быть под AppBar с отступом
            int targetPosition = currentScroll + viewTopRelativeToScroll - appBarHeight - dpToPx(16);

            // Проверяем, поместится ли dropdown
            int viewBottom = viewLocation[1] + view.getHeight();
            int availableSpace = screenHeight - keyboardHeight - viewBottom;

            if (availableSpace < dropdownHeight) {
                // Dropdown не помещается - прокручиваем больше
                targetPosition += (dropdownHeight - availableSpace);
            }

            // Плавная прокрутка
            scrollView.smoothScrollTo(0, Math.max(0, targetPosition));
        });
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupKeyboardListener() {
        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Клавиатура ПОЯВИЛАСЬ
                View focusedView = getCurrentFocus();
                if (focusedView != null) {
                    // Проверяем оба поля
                    if (focusedView.getId() == R.id.actv_color ||
                            focusedView.getId() == R.id.actv_product || focusedView.getId() == R.id.canSizeEditText) {
                        focusedView.postDelayed(() -> scrollToViewImproved(focusedView), 200);
                    }
                }
            }
        });
    }


    private void applyThemeFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        // По умолчанию 0 (DynamicColors проигнорирует 0 и использует системную, если не задано)
        int seedColor = prefs.getInt(KEY_THEME_SEED, 0);

        if (seedColor != 0) {
            applyDynamicColorScheme(seedColor);
        }
    }

    private void applyDynamicColorScheme(int seedColor) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.setPixel(0, 0, seedColor);

        DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                .setContentBasedSource(bitmap)
                .build();

        DynamicColors.applyToActivityIfAvailable(this, options);
    }

    // --- ЛОГИКА ВОССТАНОВЛЕНИЯ ДАННЫХ ---

    private void restoreCalculationStateIfAvailable() {
        // Если ViewModel содержит данные (значит, мы только что сделали recreate)
        if (viewModel.hasCachedData()) {
            // 1. Восстанавливаем переменные
            this.selectedProduct = viewModel.cachedProduct;
            this.selectedColor = viewModel.cachedColor;

            // 2. Восстанавливаем UI ввода
            if (viewModel.cachedSize != null) {
                canSizeEdit.setText(viewModel.cachedSize);
            }
            if (selectedProduct != null) {
                productDropdown.setText(selectedProduct.productName, false); // false чтобы не триггерить фильтр
            }
            if (selectedColor != null) {
                colorDropdown.setText(selectedColor.colorCode, false);
            }

            // 3. Показываем таблицу результатов
            showResult(viewModel.cachedResult, viewModel.cachedFormula);
        }
    }

    private void checkDatabaseAndRedirectIfEmpty() {
        viewModel.isDatabaseEmpty().observe(this, isEmpty -> {
            if (isEmpty) {
                importLauncher.launch(
                        new Intent(this, ImportDatabaseActivity.class)
                );
            }
        });
    }

    private final ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // ✅ Импорт завершён, база уже не пустая
                            // тут можно обновить UI / ViewModel
                            viewModel.reload(); // или любой твой метод
                        }
                    }
            );


    private int getMaxRecent() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getInt(KEY_MAX_RECENT, DEFAULT_MAX_RECENT);
    }

    private int getMaxHistory() {
        SharedPreferences prefs = getSharedPreferences(KEY_MAX_HISTORY, MODE_PRIVATE);
        return prefs.getInt(KEY_MAX_HISTORY, DEFAULT_MAX_HISTORY);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_menu) {
            showExpressiveMenu();
            return true;
        }
        else if (id == R.id.action_hide_points) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExpressiveMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet_menu);

        NavigationView navigationView = bottomSheetDialog.findViewById(R.id.navigation_view);

        if (navigationView != null) {

            CrashUiHelper.applyToNavigationMenu(
                    this,
                    navigationView
            );

            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_history) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (itemId == R.id.nav_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                } else if (itemId == R.id.nav_log) {
                    startActivity(new Intent(MainActivity.this, LogActivity.class));
                }

                bottomSheetDialog.dismiss();
                return true;
            });
        }

        bottomSheetDialog.show();

    }

    private void observeData() {
        viewModel.getProducts().observe(this, products -> {
            mCurrentProducts = products;
            updateProductsAdapter();
        });

        viewModel.getColors().observe(this, colors -> {
            mCurrentColors = colors;
            updateColorAdapter();
        });
    }

    public void updateProductsAdapter() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Set<String> recentProducts = prefs.getStringSet(KEY_RECENT_PRODUCTS, new LinkedHashSet<>());

        List<Product> recent = new ArrayList<>();
        List<Product> others = new ArrayList<>();

        for (Product product : mCurrentProducts) {
            if (recentProducts.contains(product.productName)) {
                recent.add(product);
            } else {
                others.add(product);
            }
        }

        List<Product> finalProducts = new ArrayList<>();

        if (!recent.isEmpty()) {
            finalProducts.add(ProductAdapter.DIVIDER_RECENT);
            finalProducts.addAll(recent);
            finalProducts.add(ProductAdapter.DIVIDER_OTHER);
        }

        finalProducts.addAll(others);

        ProductAdapter adapter = new ProductAdapter(this, finalProducts);

        MaterialAutoCompleteTextView actv = findViewById(R.id.actv_product);

        actv.setAdapter(adapter);

        // Хак, чтобы dropdown открывался сразу полным списком при нажатии
        actv.setOnClickListener(v -> actv.showDropDown());
    }

    public void updateColorAdapter() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Set<String> recentColors = prefs.getStringSet(KEY_RECENT_COLORS, new LinkedHashSet<>());

        List<Color> recent = new ArrayList<>();
        List<Color> others = new ArrayList<>();

        for (Color color : mCurrentColors) {
            if (recentColors.contains(color.colorCode)) {
                recent.add(color);
            } else {
                others.add(color);
            }
        }

        List<Color> finalColors = new ArrayList<>();

        if (!recent.isEmpty()) {
            finalColors.add(ColorAdapter.DIVIDER_RECENT);
            finalColors.addAll(recent);
            finalColors.add(ColorAdapter.DIVIDER_OTHER);
        }

        finalColors.addAll(others);

        ColorAdapter adapter = new ColorAdapter(this, finalColors);

        MaterialAutoCompleteTextView actv = findViewById(R.id.actv_color);

        actv.setAdapter(adapter);

        // Хак, чтобы dropdown открывался сразу полным списком при нажатии
        actv.setOnClickListener(v -> actv.showDropDown());
    }

    private void saveRecentColor(String colorCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Set<String> set = new LinkedHashSet<>(
                prefs.getStringSet(KEY_RECENT_COLORS, new LinkedHashSet<>())
        );

        set.remove(colorCode);   // чтобы не было дублей
        set.add(colorCode);      // добавляем в конец (самый свежий)

        // ограничиваем размер
        int maxRecent = getMaxRecent();
        while (set.size() > maxRecent) {
            String first = set.iterator().next();
            set.remove(first);
        }

        prefs.edit().putStringSet(KEY_RECENT_COLORS, set).apply();
    }

    private void saveRecentProduct(String product) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        Set<String> set = new LinkedHashSet<>(
                prefs.getStringSet(KEY_RECENT_PRODUCTS, new LinkedHashSet<>())
        );

        set.remove(product);
        set.add(product);

        // ограничиваем размер
        int maxRecent = getMaxRecent();
        while (set.size() > maxRecent) {
            String first = set.iterator().next();
            set.remove(first);
        }

        prefs.edit().putStringSet(KEY_RECENT_PRODUCTS, set).apply();
    }

    private void setupListeners() {

        productDropdown.setDropDownHeight(dpToPx(300));

        productDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) productDropdown.showDropDown();
        });

        productDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            selectedProduct = (Product) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "productId=" + selectedProduct.productId);
            saveRecentProduct(selectedProduct.productName);
        });

        colorDropdown.setDropDownHeight(dpToPx(300)); // Ограничиваем высоту

        colorDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.postDelayed(() -> {
                    scrollToViewImproved(v);
                    colorDropdown.showDropDown();
                }, 150); // Увеличил задержку
            }
        });

        colorDropdown.setOnClickListener(v -> {
            scrollToViewImproved(v);
            colorDropdown.showDropDown();
        });

        colorDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            selectedColor = (Color) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "colorId=" + selectedColor.colorId);
            saveRecentColor(selectedColor.colorCode);
        });

        calcButton.setOnClickListener(v -> {

            if (selectedProduct == null || selectedColor == null) {
                Snackbar.make(v, getString(R.string.select_product_color), Toast.LENGTH_SHORT).show();
                return;
            }

            if (canSizeEdit.getText() == null || canSizeEdit.getText().toString().isEmpty()) {
                Snackbar.make(v, getString(R.string.select_size), Toast.LENGTH_SHORT).show();
                return;
            }

            int productId = selectedProduct.productId;
            int colorId = selectedColor.colorId;
            String canSizeText = canSizeEdit.getText().toString();
            double canSize = Double.parseDouble(canSizeText);
            Log.d("MainActivity", "productId=" + productId + ", colorId=" + colorId + ", canSize=" + canSize);


            if (productDropdown.getText().toString().isEmpty() ||
                    colorDropdown.getText().toString().isEmpty() || canSizeEdit.getText().toString().isEmpty()) {
                Snackbar.make(v, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            Formula formula = viewModel.getFormula(productId, colorId);
            if (formula == null) {
                showNotFound();
                return;
            }


            viewModel.calculateFormulaAsync(formula, canSize).observe(this, result -> {
                if (result == null)
                    showNotFound();
                else
                {
                    saveToHistory(selectedProduct, selectedColor, selectedBase, canSizeText, result);

                    Integer rgb = selectedColor.rgb != null ? selectedColor.rgb : 0xFF000000;

                    int color = 0xFF000000 | rgb;

                    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                    int currentColor = prefs.getInt(KEY_THEME_SEED, 0);

                    if (currentColor != color && (state != null && state.isChangeDynamicColor())) {
                        // ЦВЕТ ИЗМЕНИЛСЯ -> НУЖЕН RECREATE

                        // А. Сохраняем все данные расчета в ViewModel
                        viewModel.saveState(selectedProduct, selectedColor, canSizeText, formula, result);

                        // Б. Сохраняем новый цвет в настройки для следующего onCreate
                        prefs.edit().putInt(KEY_THEME_SEED, color).apply();

                        // В. Пересоздаем Activity (перезапустится onCreate с новым цветом)
                        recreate();
                    } else {
                        // ЦВЕТ ТОТ ЖЕ -> Просто показываем результат
                        // (Можно тоже сохранить в кэш на случай поворота экрана)
                        viewModel.saveState(selectedProduct, selectedColor, canSizeText, formula, result);
                        showResult(result, formula);
                    }
                }
            });
        });

        canSizeEdit.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String input = Objects.requireNonNull(canSizeEdit.getText()).toString().trim();
                if (!input.isEmpty()) {
                    try {
                        double value = Double.parseDouble(input);
                        if (value <= 0) {
                            canSizeInput.setError("Введите положительное число");
                        } else if (value > 200) {
                            canSizeInput.setError("Значение не должно превышать 200");
                        } else {
                            canSizeInput.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        canSizeInput.setError("Введите корректное число");
                    }
                } else {
                    canSizeInput.setError("Введите значение");
                }
            }
        });

        togglePointsBtn.setOnClickListener(v -> {
            hidePoints = togglePointsBtn.isChecked();

            // если результат уже показан — просто перерисовываем таблицу
            if (viewModel.hasCachedData()) {
                showResult(viewModel.cachedResult, viewModel.cachedFormula);
            }
        });
    }

    private void clearAllFocus() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            root.requestFocus();
        }

        if (productDropdown != null) {
            productDropdown.clearFocus();
            productDropdown.dismissDropDown();
        }

        if (colorDropdown != null) {
            colorDropdown.clearFocus();
            colorDropdown.dismissDropDown();
        }
    }

    private void saveToHistory(Product selectedProduct, Color selectedColor, Basepaint selectedBase, String canSizeText, List<MainViewModel.FormulaItem> result) {
        SharedPreferences prefs = getSharedPreferences(PREFS_HISTORY, MODE_PRIVATE);
        Gson gson = new Gson();

        String json = prefs.getString(KEY_HISTORY_LIST, "");
        List<HistoryItem> historyList;

        if (!(json.isEmpty() || json.equals("[]"))) {
            Type type = new TypeToken<List<HistoryItem>>() {}.getType();
            historyList = gson.fromJson(json, type);
        } else {
            historyList = new ArrayList<>();
        }

        HistoryItem newItem = new HistoryItem(
                selectedProduct,
                selectedColor,
                Double.parseDouble(canSizeText),
                selectedBase,
                result
        );

        historyList.add(0, newItem);


        if (historyList.size() > getMaxHistory()) {
            historyList.remove(historyList.size() - 1);
        }

        String newJson = gson.toJson(historyList);
        prefs.edit().putString(KEY_HISTORY_LIST, newJson).apply();

        Log.d("History", "Saved calculation. Total items: " + historyList.size());
    }

    private void showNotFound() {
        Snackbar.make(
                scrollView,
                getString(R.string.error_not_found),
                Toast.LENGTH_LONG
        ).show();
    }

    private void showResult(List<MainViewModel.FormulaItem> items, Formula formula) {
        for (MainViewModel.FormulaItem item : items) {
            Log.d("RESULT",
                    "Colorant " + item.colorantCode +
                            " = " + item.amount);
        }

        String litersView = canSizeEdit.getText() + " " + getString(R.string.liters);

        // Заполняем данные о выбранном продукте

        if (selectedColor.rgb != null) {

            int color = 0xFF000000 | selectedColor.rgb;

            colorDot.setImageDrawable(null); // убираем иконку
            colorDot.setBackgroundTintList(ColorStateList.valueOf(color));

        } else {

            colorDot.setBackgroundTintList(null); // убираем цвет
            colorDot.setImageResource(R.drawable.question_mark_20px);
        }
        baseWeight.setText(litersView);
        colorName.setText(selectedColor.colorCode);
        colorData.setText(selectedColor.rgb != null ? String.format("#%08X", selectedColor.rgb) : getString(R.string.no_color_data_available));

        selectedBase = viewModel.getBasepaint(formula);
        if (selectedBase != null && selectedBase.baseCode != null) {
            String base = "Base " + selectedBase.baseCode;
            baseName.setText(base);
        } else {
            baseName.setText(getString(R.string.base_not_available));
        }

        resultTable.removeAllViews();

        TableRow header = new TableRow(this);
        header.setPadding(0,0,0,dp(8));

        header.addView(createHeaderCell(getString(R.string.Code)));
        header.addView(createHeaderCell(getString(R.string.Values_1L), 1, Gravity.END));
        header.addView(createHeaderCell(getString(R.string.Result), 1, Gravity.END));

        resultTable.addView(header);

        for (MainViewModel.FormulaItem item : items)
        {
            double value1L = item.amount1L;
            String result = String.format(Locale.US, "%.1f", item.amount);

            if (hidePoints) {
                result = result.replace(",", "");
            }

            TableRow row = createRow(
                    item.rgb,
                    String.valueOf(item.colorantCode),
                    value1L,
                    result
            );

            resultTable.addView(row);
            resultTable.addView(createDivider());
        }

    }

    private View createColorCircle(@Nullable Integer rgb) {
        View view = new View(this);
        int size = dp(12);

        if (rgb == null) {
            ImageView iv = new ImageView(this);

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(8));
            lp.gravity = Gravity.CENTER_VERTICAL;

            iv.setImageResource(R.drawable.question_mark_20px);
            iv.setLayoutParams(lp);

            return iv;
        }

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(size, size);
        lp.setMarginEnd(dp(8));
        lp.gravity = Gravity.CENTER_VERTICAL;

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(0xFF000000 | rgb);

        view.setBackground(drawable);
        view.setLayoutParams(lp);

        return view;
    }


    private View createCodeCell(int rgb, String code) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                2f
        ));
        layout.setGravity(Gravity.CENTER_VERTICAL);

        // кружок цвета
        layout.addView(createColorCircle(rgb));

        // текст кода
        TextView tv = new TextView(this);
        tv.setText(code);
        tv.setTextAppearance(
                com.google.android.material.R.style.TextAppearance_Material3_BodyMedium
        );

        layout.addView(tv);

        return layout;
    }


    private View createDivider() {
        View v = new View(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        v.setLayoutParams(params);
        v.setBackgroundColor(
                MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorOutlineVariant,
                        android.graphics.Color.GRAY
                )
        );
        v.setAlpha(0.2f);
        return v;
    }

    @SuppressLint("DefaultLocale")
    private TableRow createRow(Integer RGB, String code, double value1L, String result) {

        TableRow row = new TableRow(this);
        row.setPadding(0, dp(6), 0, dp(6));

        TextView tvCode = new TextView(this);
        tvCode.setText(code);
        tvCode.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        tvCode.setTextAppearance(this, R.style.DataCell_Text);

        TextView tvValue1L = new TextView(this);
        tvValue1L.setText(String.format("%.1f", value1L));
        tvValue1L.setGravity(Gravity.END);
        tvValue1L.setTextAppearance(this, R.style.DataCell_Num);

        TextView tvResult = new TextView(this);
        tvResult.setText(result);
        tvResult.setGravity(Gravity.END);
        tvResult.setTextAppearance(this, R.style.DataCell_Num);

        row.addView(createCodeCell(RGB, code));
        row.addView(tvValue1L);
        row.addView(tvResult);

        return row;
    }

    private View createHeaderCell(String text) {
        return createHeaderCell(text, 2, Gravity.START);
    }

    private View createHeaderCell(String text, int weight, int gravity) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(gravity);
        tv.setLayoutParams(
                new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight)
        );
        tv.setTextAppearance(this, R.style.TextAppearance_Material3_LabelMedium);
        tv.setTextColor(
                MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorOutline,
                        android.graphics.Color.GRAY
                )
        );
        return tv;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupQuickSizeButtons() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupQuickSizes);

        // 1. Список значений
        List<String> sizes = SizesRepository.loadSizes(this);

        // Очищаем группу перед заполнением (на случай перезагрузки настроек)
        chipGroup.removeAllViews();

        for (String size : sizes) {
            Chip chip = new Chip(this);
            chip.setTextAppearanceResource(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge);
            chip.setCheckable(false);

            double value = Double.parseDouble(size);
            String clear_size = "";
            if (value == (long) value) {
                clear_size = String.format(Locale.US, "%d", (long) value);
            } else {
                clear_size = String.format(Locale.US, "%.1f", value);
            }

            chip.setText(size);

            String finalClear_size = clear_size;
            chip.setOnClickListener(v -> {
                canSizeEdit.setText(finalClear_size);
                canSizeEdit.setSelection(finalClear_size.length());
            });

            chipGroup.addView(chip);
        }
    }

    private void setupStepperButtons() {
        MaterialButton btnMinus = findViewById(R.id.btn_minus);
        MaterialButton btnPlus = findViewById(R.id.btn_plus);

        SharedPreferences prefs = getSharedPreferences(STEPPER_BUTTONS, MODE_PRIVATE);
        btnMinus.setOnClickListener(v -> {
            int step = getCurrentStep(prefs);
            changeValue(canSizeEdit, -step);
        });

        btnPlus.setOnClickListener(v -> {
            int step = getCurrentStep(prefs);
            changeValue(canSizeEdit, step);
        });
    }

    private int getCurrentStep(SharedPreferences prefs) {
        if (prefs.getBoolean(STEPPER_BUTTONS, true)) {
            return prefs.getInt(KEY_STEP_VALUE, 1);
        }

        return 1;
    }

    private void changeValue(TextInputEditText canSizeEdit, int step) {
        String currentText = Objects.requireNonNull(canSizeEdit.getText()).toString();
        double value = 0.0;

        if (!TextUtils.isEmpty(currentText)) {
            try {
                value = Double.parseDouble(currentText.replace(",", "."));
            } catch (NumberFormatException e) {
                value = 0.0;
            }
        }

        if (step >= 0 || value > 1)
            value += step;

        if (value <= 0) value = 1;

        if (value > 200) value = 200;

        if (value == (long) value) {
            canSizeEdit.setText(String.format(Locale.US, "%d", (long) value));
        } else {
            canSizeEdit.setText(String.format(Locale.US, "%.1f", value));
        }

        canSizeEdit.setSelection(canSizeEdit.getText().length());

    }

    List<String> emojis = Arrays.asList(
            "😀", "😂", "😍", "🤩", "😎",
            "🤗", "🥳", "😜", "🤪", "😇",
            "🤓", "🧐", "🤠", "🌈", "🥸",
            "✅", "😳", "💫", "🥶", "🥴",
            "😈", "👻", "👾", "🤖", "👋",
            "👍", "👏", "🙌", "🤝", "🙏",
            "💪", "🧠", "👀", "👂", "👄",
            "❤️", "💖", "💙", "💚", "💛",
            "🧡", "💜", "🖤", "🤍", "🤎",
            "✨", "🌟", "⭐", "💥", "🔥",
            "🌈", "🌞", "🌙", "🎯", "🎲",
            "🎁", "🎉", "🎊", "🎈", "🪄",
            "⚡", "💎", "👑", "🛡️", "⚔️",
            "🎮", "🕹️", "🏆", "🏆", "🏅",
            "🥇", "🥈", "🥉", "⚽", "🏀",
            "🏈", "⚾", "🎾", "🏐", "🎱",
            "🏓", "🏸", "🥊", "🥋", "🛹",
            "🚲", "💥", "🚀", "✈️", "🛸",
            "🚁", "🚤", "⛵", "⚓", "🧭",
            "🏝️", "🌋", "🌌", "🌠", "🌊",
            "🌳", "🌵", "🌷", "🌸", "🌹",
            "🍀", "🍁", "🍂", "🍃", "🍄",
            "🦀", "🦑", "🐙", "🐟", "🐬"
    );
    String rareEmoji = "🦄";
    String ultraRareEmoji = "🥕🐇";
    private void initViews() {
        productInput = findViewById(R.id.productInput);
        productDropdown = (AutoCompleteTextView) productInput.getEditText();
        colorInput = findViewById(R.id.colorInput);
        colorDropdown = (AutoCompleteTextView) colorInput.getEditText();
        canSizeEdit = findViewById(R.id.canSizeEditText);
        canSizeInput = findViewById(R.id.canSizeInput);
        calcButton = findViewById(R.id.calcButton);
        resultTable = findViewById(R.id.resultTable);
        baseWeight = findViewById(R.id.baseWeight);
        baseName = findViewById(R.id.baseName);
        colorDot = findViewById(R.id.colorDot);
        colorName = findViewById(R.id.colorName);
        colorData = findViewById(R.id.colorData);
        topAppBar = findViewById(R.id.topAppBar);
        togglePointsBtn = findViewById(R.id.btn_toggle_points);
        scrollView = findViewById(R.id.scrollView);

        canSizeEdit.setText(R.string.default_value_size);

        Random random = new Random();
        String selected;

        if (random.nextInt(100) == 0)
        {
            selected = rareEmoji;
        } else if (random.nextInt(2000) == 0) {
            selected = ultraRareEmoji;
        } else {
            selected = emojis.get(random.nextInt(emojis.size()));
        }

        colorData.append(selected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Логика меню (как была)
        Menu menu = topAppBar.getMenu();
        CrashUiHelper.apply(this, menu);

        // ДОБАВИТЬ ЭТУ СТРОКУ: Обновляем иконку бургера
        CrashUiHelper.applyToToolbar(this, topAppBar);

        // этот метод сработает, считает новые данные и перерисует кнопки.
        setupQuickSizeButtons();

        // обновляем частоиспользуемые цвета и продукты
        updateColorAdapter();
        updateProductsAdapter();

        Log.d(TAG, "onResume: resume");
    }
}