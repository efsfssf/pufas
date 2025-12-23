package com.sjapps.jsonlist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.sjapps.db.AppDatabase;
import com.sjapps.db.Color;
import com.sjapps.db.Product;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextInputLayout productInput;
    AutoCompleteTextView productDropdown;

    TextInputLayout colorInput;
    AutoCompleteTextView colorDropdown;

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
    }

    private void initViews() {
        productInput = findViewById(R.id.productInput);
        productDropdown = (AutoCompleteTextView) productInput.getEditText();
        colorInput = findViewById(R.id.colorInput);
        colorDropdown = (AutoCompleteTextView) colorInput.getEditText();
    }


}