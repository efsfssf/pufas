package com.dandomi.pufas;

import android.content.Context;
import android.text.TextUtils;

import com.dandomi.db.Basepaint;
import com.dandomi.db.Color;
import com.dandomi.db.Product;

import java.util.ArrayList;
import java.util.List;

public class HistoryItem {
    public long timestamp;
    public Product product;
    public Color color;
    public double liters;
    public Basepaint base;
    public List<MainViewModel.FormulaItem> formulaResult;

    public HistoryItem(Product product, Color color, double liters, Basepaint base, List<MainViewModel.FormulaItem> formulaResult) {
        this.timestamp = System.currentTimeMillis();
        this.product = product;
        this.color = color;
        this.liters = liters;
        this.base = base;
        this.formulaResult = formulaResult;
    }


    public String getHeader(Context context) {
        if (product == null || color == null)
            return context.getString(R.string.no_data_available);


        String productName = safeString(product.productName);
        String colorName = safeString(color.colorCode);
        String litersStr = safeString(liters);
        String baseCode = (base != null) ? safeString(base.baseCode) : null;

        List<String> parts = new ArrayList<>();
        parts.add(productName);
        parts.add(colorName);
        parts.add(litersStr);
        if (baseCode != null && !baseCode.isEmpty())
            parts.add(baseCode);

        return TextUtils.join("\n", parts);

    }

    private String safeString(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

    public List<MainViewModel.FormulaItem> getColorants() {
        return formulaResult;
    }
}
