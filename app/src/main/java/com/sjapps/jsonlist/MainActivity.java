package com.sjapps.jsonlist;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.sjapps.db.Color;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.sjapps.db.Product;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

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
    private static final int MAX_RECENT = 5;

    public static final Color DIVIDER = new Color();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();

        viewModel = new ViewModelProvider(this)
                .get(MainViewModel.class);

        observeData();

        // Находим тулбар (убедитесь, что он есть в layout activity_main)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // 1. Устанавливаем иконку бургера СЛЕВА
        toolbar.setNavigationIcon(R.drawable.menu_24px);

        // 2. Вешаем слушатель нажатия именно на эту левую иконку
        toolbar.setNavigationOnClickListener(v -> {
            showExpressiveMenu(); // <--- Вызываем меню отсюда
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // Связываем ID из вашего XML (action_menu) с методом показа
        if (id == R.id.action_menu) {
            showExpressiveMenu(); // <--- ВОТ ЗДЕСЬ вызываем метод
            return true;
        }

        // Обработка кнопки "Скрыть баллы"
        else if (id == R.id.action_hide_points) {
            // Ваша логика скрытия
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExpressiveMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.layout_bottom_sheet_menu);

        NavigationView navigationView = bottomSheetDialog.findViewById(R.id.navigation_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_history) {
                    Toast.makeText(this, "История", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_settings) {
                    Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_about) {
                    Toast.makeText(this, "О приложении", Toast.LENGTH_SHORT).show();
                }

                bottomSheetDialog.dismiss();
                return true;
            });
        }

        bottomSheetDialog.show();

    }

    private void observeData() {
        viewModel.getProducts().observe(this, products -> {
            productDropdown.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products)
            );
        });

        viewModel.getColors().observe(this, colors -> {
            ColorAdapter adapter = new ColorAdapter(this, colors);

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
        while (set.size() > MAX_RECENT) {
            String first = set.iterator().next();
            set.remove(first);
        }

        prefs.edit().putStringSet(KEY_RECENT_COLORS, set).apply();
    }


    private void setupListeners() {
        productDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) productDropdown.showDropDown();
        });

        productDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            selectedProduct = (Product) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "productId=" + selectedProduct .productId);
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
            double canSize = Double.parseDouble(canSizeEdit.getText().toString());

            if (productDropdown.getText().toString().isEmpty() ||
                    colorDropdown.getText().toString().isEmpty() || canSizeEdit.getText().toString().isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {

                List<MainViewModel.FormulaItem> result =
                        viewModel.calculateFormula(productId, colorId, canSize);

                runOnUiThread(() -> {
                    if (result == null) {
                        showNotFound();
                    } else {
                        showResult(result);
                    }
                });
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
    }


}