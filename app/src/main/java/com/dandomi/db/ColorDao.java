package com.dandomi.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ColorDao {

    @Query("SELECT * FROM color ORDER BY COLORCODE")
    List<Color> getAllColors();

    @Insert
    void insertColor(Color... color);

    @Delete
    void delete(Color color);

    @Query("DELETE FROM color")
    void clear();

    @Query("SELECT COUNT(*) FROM Color")
    int countColor();

    @Query("SELECT COUNT(*) FROM color")
    int count();
}
