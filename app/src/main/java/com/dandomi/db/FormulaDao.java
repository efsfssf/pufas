package com.dandomi.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FormulaDao {

    @Query("SELECT CNTINFORMULA, ABASEID FROM formula WHERE FORMULAID = :formulaId LIMIT 1")
    FormulaCnt getFormula(int formulaId);

    @Query("SELECT * FROM Formula")
    List<Formula> getAllFormulas();

    @Insert
    void insertFormula(Formula... formula);

    @Delete
    void delete(Formula formula);

    @Query("DELETE FROM formula")
    void clear();

    @Query("SELECT * FROM Formula WHERE FORMULAID = :id")
    Formula getById(int id);

}
