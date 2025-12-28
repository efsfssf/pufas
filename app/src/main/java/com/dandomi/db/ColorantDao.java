package com.dandomi.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ColorantDao {

    @Query("SELECT *\n" +
            "\t   FROM colorant\n" +
            "\t   WHERE CNTID IN (:colorantId)")
    Colorant getColorant(int colorantId);

    @Query("SELECT *\n" +
            "\t   FROM colorant")
    List<Colorant> getAllColorants();

    @Insert
    void insertColorant(Colorant... colorant);

    @Delete
    void delete(Colorant colorant);

    @Query("DELETE FROM colorant")
    void clear();
}
