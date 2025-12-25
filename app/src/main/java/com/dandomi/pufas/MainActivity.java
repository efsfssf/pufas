package com.dandomi.pufas;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.dandomi.pufas.R;

public class MainActivity extends AppCompatActivity {

    TextInputLayout productInput;
    AutoCompleteTextView productDropdown;

    TextInputLayout colorInput;
    AutoCompleteTextView colorDropdown;
    TextInputEditText canSizeEdit;
    TextInputLayout canSizeInput;

    MaterialButton calcButton;
    TextView baseWeight;
    ShapeableImageView colorDot;
    TextView colorName;
    TextView colorData;
    MaterialToolbar topAppBar;

    private TableLayout resultTable;

    private MainViewModel viewModel;

    private Product selectedProduct = null;
    private Color selectedColor = null;

    private static final String PREFS = "recent_items";
    private static final String KEY_RECENT_COLORS = "recent_colors";
    private static final String KEY_RECENT_PRODUCTS = "recent_products";
    private static final String KEY_MAX_RECENT = "max_recent";
    private static final int DEFAULT_MAX_RECENT = 5;
    private static final String KEY_MAX_HISTORY = "max_history";
    private static final int DEFAULT_MAX_HISTORY = 50;
    private static final String USER_SETTINGS = "UserSettings";
    private static final String STEPPER_BUTTONS = "StepperButtons";
    private static final String PREFS_HISTORY = "calculation_history";
    private static final String KEY_HISTORY_LIST = "history_list";
    private static final String KEY_THEME_SEED = "theme_seed_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeFromPrefs();
        super.onCreate(savedInstanceState);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        checkDatabaseAndRedirectIfEmpty();

        initViews();
        setupListeners();
        setupQuickSizeButtons();
        setupStepperButtons();
        observeData();
        restoreCalculationStateIfAvailable();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.menu_24px);
        toolbar.setNavigationOnClickListener(v -> showExpressiveMenu());

        // ===============================
        // EDGE-TO-EDGE + KEYBOARD FIX
        // ===============================

        // ВАЖНО: false — иначе IME inset'ы НЕ ПРИДУТ
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.rootCoordinator);
        ScrollView scrollView = findViewById(R.id.ScrollViewId);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets  = insets.getInsets(WindowInsetsCompat.Type.ime());

            // Верх — статус-бар
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    0
            );

            // Низ — клавиатура ИЛИ навбар
            int bottomPadding = Math.max(systemBars.bottom, imeInsets.bottom);

            scrollView.setPadding(
                    scrollView.getPaddingLeft(),
                    scrollView.getPaddingTop(),
                    scrollView.getPaddingRight(),
                    bottomPadding
            );

            // ⬅️ НИКОГДА НЕ CONSUMED
            return insets;
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
            showResult(viewModel.cachedResult);
        }
    }

    private void checkDatabaseAndRedirectIfEmpty() {
        viewModel.isDatabaseEmpty().observe(this, isEmpty -> {
            if (isEmpty) {
                startActivity(new Intent(this, ImportDatabaseActivity.class));
                finish();
            }
        });
    }

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

            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            Set<String> recentProducts = prefs.getStringSet(KEY_RECENT_PRODUCTS, new LinkedHashSet<>());

            productDropdown.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products)
            );
        });

        viewModel.getColors().observe(this, colors -> {

            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            Set<String> recentColors = prefs.getStringSet(KEY_RECENT_COLORS, new LinkedHashSet<>());

            List<Color> recent = new ArrayList<>();
            List<Color> others = new ArrayList<>();

            for (Color color : colors) {
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
        });
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

        set.remove(product);   // чтобы не было дублей
        set.add(product);      // добавляем в конец (самый свежий)

        // ограничиваем размер
        int maxRecent = getMaxRecent();
        while (set.size() > maxRecent) {
            String first = set.iterator().next();
            set.remove(first);
        }

        prefs.edit().putStringSet(KEY_RECENT_PRODUCTS, set).apply();
    }

    private void setupListeners() {
        productDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) productDropdown.showDropDown();
        });

        productDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            selectedProduct = (Product) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "productId=" + selectedProduct .productId);
            saveRecentProduct(selectedProduct.productName);
        });

        colorDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) colorDropdown.showDropDown();
        });

        colorDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            selectedColor = (Color) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "colorId=" + selectedColor.colorId);
            saveRecentColor(selectedColor.colorCode);
        });

        calcButton.setOnClickListener(v -> {

            if (selectedProduct == null || selectedColor == null) {
                Toast.makeText(this, "Выберите продукт и цвет", Toast.LENGTH_SHORT).show();
                return;
            }

            if (canSizeEdit.getText() == null || canSizeEdit.getText().toString().isEmpty()) {
                Toast.makeText(this, "Введите литры", Toast.LENGTH_SHORT).show();
                return;
            }

            int productId = selectedProduct.productId;
            int colorId = selectedColor.colorId;
            String canSizeText = canSizeEdit.getText().toString();
            double canSize = Double.parseDouble(canSizeText);
            Log.d("MainActivity", "productId=" + productId + ", colorId=" + colorId + ", canSize=" + canSize);


            if (productDropdown.getText().toString().isEmpty() ||
                    colorDropdown.getText().toString().isEmpty() || canSizeEdit.getText().toString().isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.calculateFormulaAsync(productId, colorId, canSize).observe(this, result -> {
                if (result == null)
                    showNotFound();
                else
                {
                    saveToHistory(selectedProduct, selectedColor, canSizeText, result);
                    int color = 0xFF000000 | selectedColor.rgb;

                    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                    int currentColor = prefs.getInt(KEY_THEME_SEED, 0);

                    if (currentColor != color) {
                        // ЦВЕТ ИЗМЕНИЛСЯ -> НУЖЕН RECREATE

                        // А. Сохраняем все данные расчета в ViewModel
                        viewModel.saveState(selectedProduct, selectedColor, canSizeText, result);

                        // Б. Сохраняем новый цвет в настройки для следующего onCreate
                        prefs.edit().putInt(KEY_THEME_SEED, color).apply();

                        // В. Пересоздаем Activity (перезапустится onCreate с новым цветом)
                        recreate();
                    } else {
                        // ЦВЕТ ТОТ ЖЕ -> Просто показываем результат
                        // (Можно тоже сохранить в кэш на случай поворота экрана)
                        viewModel.saveState(selectedProduct, selectedColor, canSizeText, result);
                        showResult(result);
                    }
                }
            });
        });

        canSizeEdit.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String input = canSizeEdit.getText().toString().trim();
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
    }

    private void saveToHistory(Product selectedProduct, Color selectedColor, String canSizeText, List<MainViewModel.FormulaItem> result) {
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
                "",
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
        Toast.makeText(
                this,
                "Формула для выбранного цвета не найдена",
                Toast.LENGTH_LONG
        ).show();
    }

    private void showResult(List<MainViewModel.FormulaItem> items) {
        for (MainViewModel.FormulaItem item : items) {
            Log.d("RESULT",
                    "Colorant " + item.colorantId +
                            " = " + item.amount);
        }

        String litersView = canSizeEdit.getText() + " " + getString(R.string.liters);

        // Заполняем данные о выбранном продукте
        int color = 0xFF000000 | selectedColor.rgb;
        baseWeight.setText(litersView);
        colorDot.setBackgroundColor(color);
        colorDot.setBackgroundTintList(ColorStateList.valueOf(color));
        colorDot.setImageIcon(null);
        colorName.setText(selectedColor.colorCode);
        colorData.setText(String.format("#%08X", (color)));

        resultTable.removeAllViews();

        TableRow header = new TableRow(this);
        header.setPadding(0,0,0,dp(8));

        header.addView(createHeaderCell(getString(R.string.Code), 2));
        header.addView(createHeaderCell(getString(R.string.Values_1L), 1, Gravity.END));
        header.addView(createHeaderCell(getString(R.string.Result), 1, Gravity.END));

        resultTable.addView(header);

        for (MainViewModel.FormulaItem item : items)
        {
            double value1L = item.amount1L;
            double result = item.amount;

            TableRow row = createRow(
                    String.valueOf(item.colorantId),
                    value1L,
                    result
            );

            resultTable.addView(row);
            resultTable.addView(createDivider());
        }

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
    private TableRow createRow(String code, double value1L, double result) {

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
        tvResult.setText(String.format("%.1f", result));
        tvResult.setGravity(Gravity.END);
        tvResult.setTextAppearance(this, R.style.DataCell_Num);

        row.addView(tvCode);
        row.addView(tvValue1L);
        row.addView(tvResult);

        return row;
    }

    private View createHeaderCell(String text, int weight) {
        return createHeaderCell(text, weight, Gravity.START);
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

        // 1. Список значений (в будущем будете брать его из UserSettings)
        List<String> sizes = loadSizes(this);

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

    private List<String> loadSizes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(USER_SETTINGS, MODE_PRIVATE);
        String saved = prefs.getString("quick_sizes", "");
        if (saved.isEmpty())
            return Arrays.asList("0.5", "1.0", "2.5", "5.0", "10.0", "15.0", "20.0");
        return new ArrayList<>(Arrays.asList(TextUtils.split(saved, ",")));
    }

    private void setupStepperButtons() {
        MaterialButton btnMinus = findViewById(R.id.btn_minus);
        MaterialButton btnPlus = findViewById(R.id.btn_plus);

        SharedPreferences prefs = getSharedPreferences(STEPPER_BUTTONS, MODE_PRIVATE);
        int step;
        if (prefs.getBoolean("stepper_buttons", true)) {
            step = prefs.getInt("stepper_step", 1);
        } else {
            step = 1;
        }

        btnMinus.setOnClickListener(v -> changeValue(canSizeEdit, (step * -1)));
        btnPlus.setOnClickListener(v -> changeValue(canSizeEdit, step));
    }

    private void changeValue(TextInputEditText canSizeEdit, int step) {
        String currentText = canSizeEdit.getText().toString();
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
        colorDot = findViewById(R.id.colorDot);
        colorName = findViewById(R.id.colorName);
        colorData = findViewById(R.id.colorData);
        topAppBar = findViewById(R.id.topAppBar);

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
        Log.d(TAG, "onResume: resume");
    }
}