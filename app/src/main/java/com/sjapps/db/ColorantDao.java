package com.sjapps.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ColorantDao {

    @Query("SELECT CNTID, CNTCODE\n" +
            "\t   FROM colorant\n" +
            "\t   WHERE CNTID IN (:colorantId)")
    Integer getColorant(int colorantId);

    @Insert
    void insertColorant(Colorant... colorant);

    @Delete
    void delete(Colorant colorant);
}
