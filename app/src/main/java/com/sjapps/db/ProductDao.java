package com.sjapps.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT *\n" +
          "FROM product\n" +
       "ORDER BY PRODUCTNAME")
    List<Product> getAllProducts();

    @Insert
    void insertProduct(Product... product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM product")
    void clear();

    @Query("UPDATE Product SET PARENTPRODUCTID = :parentId WHERE PRODUCTID = :productId ")
    void updateParent(Integer productId, Integer parentId);

    @Query("SELECT * FROM Product WHERE PRODUCTID = :id")
    Product getById(int id);

    @Query("SELECT COUNT(*) FROM Product")
    int countProducts();
}
