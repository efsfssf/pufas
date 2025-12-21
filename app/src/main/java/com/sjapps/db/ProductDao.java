package com.sjapps.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT PRODUCTID, PRODUCTNAME\n" +
          "FROM product\n" +
       "ORDER BY PRODUCTNAME")
    List<Product> getAllProducts();

    @Insert
    void insertProduct(Product... product);

    @Delete
    void delete(Product product);
}
