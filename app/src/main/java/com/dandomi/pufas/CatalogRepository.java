package com.dandomi.pufas;

import android.content.Context;

import androidx.core.util.Consumer;

import com.dandomi.db.AppDatabase;
import com.dandomi.db.Color;
import com.dandomi.db.Product;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CatalogRepository
{
    private final AppDatabase db;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public CatalogRepository(Context context) {
        db = AppDatabase.getDbInstance(context);
    }

    public void getAllProducts(Consumer<List<Product>> callback) {
        executor.execute(() -> {
            List<Product> products = db.productDao().getAllProducts();
            callback.accept(products);
        });
    }

    public void getAllColors(Consumer<List<Color>> callback) {
        executor.execute(() -> {
            List<Color> colors = db.colorDao().getAllColors();
            callback.accept(colors);
        });
    }

    public void close() {
        executor.shutdown();
    }
}


