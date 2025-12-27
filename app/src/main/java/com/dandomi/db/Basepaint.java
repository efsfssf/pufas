package com.dandomi.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Basepaint {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BASEID")
    public int baseId;

    @ColumnInfo(name = "PRODUCTID")
    public int productId;

    @ColumnInfo(name = "ABASEID")
    public int abaseId;

    @ColumnInfo(name = "BASECODE")
    public String baseCode;

    @ColumnInfo(name = "SPECIFICGRAVITY")
    public Float specificGravity;

    @ColumnInfo(name = "MINFILL")
    public Float MINFILL;

    @ColumnInfo(name = "MAXFILL")
    public Float MAXFILL;

}
