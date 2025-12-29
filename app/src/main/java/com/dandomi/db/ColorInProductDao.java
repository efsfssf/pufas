package com.dandomi.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ColorInProductDao {

    @Query("SELECT FORMULAID\n" +
            "       FROM colorinproduct\n" +
            "       WHERE PRODUCTID = :productId AND COLORID = :colorId\n" +
            "       ORDER BY VERSION ASC\n" +
            "       LIMIT 1")
    Integer getColorInProduct(int productId, int colorId);

    @Query("SELECT *\n" +
        "FROM colorinproduct\n"
    )
    List<ColorInProduct> getAllColorInProducts();

    @Insert
    void insertColorInProduct(ColorInProduct... colorInProduct);

    @Delete
    void delete(ColorInProduct colorInProduct);

    @Query("SELECT * FROM ColorInProduct\n" +
        "WHERE PRODUCTID = :productId\n" +
          "AND COLORID = :colorId\n" +
        "ORDER BY VERSION ASC\n" +
        "LIMIT 1")
    ColorInProduct find(int productId, int colorId);

    @Query("DELETE FROM colorInProduct")
    void clear();

    @Query("SELECT COUNT(*) FROM colorinproduct")
    int count();
}
