package com.dandomi.pufas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dandomi.db.AppDatabase;
import com.dandomi.db.Color;
import com.dandomi.db.ColorInProduct;
import com.dandomi.db.Formula;
import com.dandomi.db.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public Product cachedProduct = null;
    public Color cachedColor = null;
    public String cachedSize = null;
    public List<FormulaItem> cachedResult = null;

    public void saveState(Product p, Color c, String size, List<FormulaItem> res) {
        this.cachedProduct = p;
        this.cachedColor = c;
        this.cachedSize = size;
        this.cachedResult = res;
    }

    public boolean hasCachedData() {
        return cachedResult != null;
    }

    // Метод для очистки, если нужно (например при выходе)
    public void clearCache() {
        cachedProduct = null;
        cachedColor = null;
        cachedSize = null;
        cachedResult = null;
    }

    public LiveData<Boolean> isDatabaseEmpty() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        @SuppressWarnings("resource")
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                boolean empty = db.productDao().countProducts() == 0;
                boolean empty2 = db.colorDao().countColor() == 0;
                empty = empty || empty2;
                result.postValue(empty);
            } finally {
                executor.shutdown();
            }
        });

        return result;
    }

    public LiveData<List<FormulaItem>> calculateFormulaAsync(int productId, int colorId, double liters) {
        MutableLiveData<List<FormulaItem>> result = new MutableLiveData<>();

        @SuppressWarnings("resource")
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            List<FormulaItem> data = calculateFormula(productId, colorId, liters);

            result.postValue(data);
        });

        return result;

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
        public double amount1L;
        public double amount;

        public FormulaItem(int colorantId, double amount1L, double amount) {
            this.colorantId = colorantId;
            this.amount1L = amount1L;
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

            result.add(new FormulaItem(id, amount, amount));
        }

        return result;

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }
}
