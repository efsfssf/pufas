package com.sjapps.jsonlist;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sjapps.db.Color;
import com.sjapps.db.Product;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextInputLayout productInput;
    AutoCompleteTextView productDropdown;

    TextInputLayout colorInput;
    AutoCompleteTextView colorDropdown;
    TextInputEditText canSizeInput;


    MaterialButton calcButton;

    private MainViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();

        viewModel = new ViewModelProvider(this)
                .get(MainViewModel.class);

        observeData();


    }

    private void observeData() {
        viewModel.getProducts().observe(this, products -> {
            productDropdown.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, products)
            );
        });

        viewModel.getColors().observe(this, colors -> {
            colorDropdown.setAdapter(
                    new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, colors)
            );
        });
    }

    private void setupListeners() {
        productDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) productDropdown.showDropDown();
        });

        productDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            Product p = (Product) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "productId=" + p.productId);
        });

        colorDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) colorDropdown.showDropDown();
        });

        colorDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            Color c = (Color) parent.getItemAtPosition(pos);
            Log.d("MainActivity", "colorId=" + c.colorId);
        });

        calcButton.setOnClickListener(v -> {



            int productId = Integer.parseInt(productDropdown.getText().toString());
            int colorId = Integer.parseInt(colorDropdown.getText().toString());
            int canSize = Integer.parseInt(canSizeInput.getText().toString());

            if (productDropdown.getText().toString().isEmpty() ||
                    colorDropdown.getText().toString().isEmpty() || canSizeInput.getText().toString().isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {

                List<MainViewModel.FormulaItem> result =
                        viewModel.calculateFormula(productId, colorId, canSize);

                runOnUiThread(() -> {
                    if (result == null) {
                        return;
                    } else {
                        return;
                    }
                });
            });
        });
    }

    private void initViews() {
        productInput = findViewById(R.id.productInput);
        productDropdown = (AutoCompleteTextView) productInput.getEditText();
        colorInput = findViewById(R.id.colorInput);
        colorDropdown = (AutoCompleteTextView) colorInput.getEditText();
        canSizeInput = findViewById(R.id.canSizeEditText);
        calcButton = findViewById(R.id.calcButton);

        canSizeInput.setText("10");
    }


}