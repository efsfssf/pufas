package com.dandomi.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BasepaintDao {

    @Query("SELECT *\n" +
            "\t   FROM basepaint\n" +
            "\t   WHERE ABASEID IN (:abaseId)")
    Basepaint getBasepaint(int abaseId);

    @Query("SELECT * FROM Basepaint")
    List<Basepaint> getAllBasepaints();

    @Insert
    void insertBasepaint(Basepaint... basepaint);

    @Delete
    void delete(Basepaint basepaint);

    @Query("DELETE FROM basepaint")
    void clear();
}
