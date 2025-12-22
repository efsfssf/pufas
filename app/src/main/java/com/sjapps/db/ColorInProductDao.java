package com.sjapps.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ColorInProductDao {

    @Query("SELECT FORMULAID\n" +
            "       FROM colorinproduct\n" +
            "       WHERE PRODUCTID = :productId AND COLORID = :colorId\n" +
            "       ORDER BY VERSION ASC\n" +
            "       LIMIT 1")
    Integer getColorInProduct(int productId, int colorId);

    @Insert
    void insertColorInProduct(ColorInProduct... colorInProduct);

    @Delete
    void delete(ColorInProduct colorInProduct);

    @Query("DELETE FROM colorInProduct")
    void clear();
}
