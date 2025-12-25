package com.dandomi.pufas;

import com.dandomi.db.Color;
import com.dandomi.db.Product;

import java.util.List;

public class HistoryItem {
    public long timestamp;
    public Product product;
    public Color color;
    public double liters;
    public String baseName;
    public List<MainViewModel.FormulaItem> formulaResult;

    public HistoryItem(Product product, Color color, double liters, String baseName, List<MainViewModel.FormulaItem> formulaResult) {
        this.timestamp = System.currentTimeMillis();
        this.product = product;
        this.color = color;
        this.liters = liters;
        this.baseName = baseName;
        this.formulaResult = formulaResult;
    }
}
