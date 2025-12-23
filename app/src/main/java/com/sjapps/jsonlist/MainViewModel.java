package com.sjapps.jsonlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sj14apps.jsonlist.core.ListItem;
import com.sjapps.db.AppDatabase;
import com.sjapps.db.Color;
import com.sjapps.db.ColorInProduct;
import com.sjapps.db.Formula;
import com.sjapps.db.Product;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final CatalogRepository repository;

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Color>> colors = new MutableLiveData<>();

    private final AppDatabase db;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CatalogRepository(application);
        this.db = AppDatabase.getDbInstance(application);
        loadProducts();
        loadColors();
    }

    private void loadColors() {
        repository.getAllColors(colors::postValue);
    }

    private void loadProducts() {
        repository.getAllProducts(products::postValue);
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<Color>> getColors() {
        return colors;
    }

    @Nullable
    public Formula findFormulaRecursive(int productId, int colorId) {

        Integer currentProductId = productId;

        while (currentProductId != null) {
            ColorInProduct cip = db.colorInProductDao().find(currentProductId, colorId);

            if (cip != null) {
                return db.formulaDao().getById(cip.formulaId);
            }

            Product product = db.productDao().getById(currentProductId);

            if (product == null)
                return null;

            currentProductId = product.parentProductId;
        }

        return null;
    }

    // ===== CNTINFORMULA =====

    public static class FormulaItem {
        public int colorantId;
        public double amount;

        public FormulaItem(int colorantId, double amount) {
            this.colorantId = colorantId;
            this.amount = amount;
        }
    }

    public List<FormulaItem> calculateFormula(int productId, int colorId, double liters) {
        Formula formula = findFormulaRecursive(productId, colorId);

        if (formula == null)
            return null;

        List<FormulaItem> formulaItems = parseCntInFormula(formula.cntInFormula);

        for (FormulaItem item : formulaItems) {
            item.amount *= liters;
        }

        return formulaItems;
    }

    private List<FormulaItem> parseCntInFormula(String cntInFormula) {

        String clean = cntInFormula.replace("[[", "").replace("]]", "");

        String[] parts = clean.split("\\],\\s*\\[");

        String[] ids = parts[0].split(",");
        String[] amounts = parts[1].split(",");

        List<FormulaItem> result = new ArrayList<>();

        for (int i = 0; i < ids.length; i++) {
            int id = Integer.parseInt(ids[i].trim());
            double amount = Double.parseDouble(amounts[i].trim());

            result.add(new FormulaItem(id, amount));
        }

        return result;

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }
}
