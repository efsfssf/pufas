package com.sjapps.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ColorDao {

    @Query("SELECT COLORID, COLORCODE FROM color ORDER BY COLORCODE")
    List<Color> getAllColors();

    @Insert
    void insertColor(Color... color);

    @Delete
    void delete(Color color);
}
