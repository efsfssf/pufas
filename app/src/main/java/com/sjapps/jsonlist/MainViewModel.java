package com.sjapps.jsonlist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.sjapps.db.Color;
import com.sjapps.db.Product;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final CatalogRepository repository;

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Color>> colors = new MutableLiveData<>();


    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CatalogRepository(application);
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

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }
}
