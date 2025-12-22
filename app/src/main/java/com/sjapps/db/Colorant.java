package com.sjapps.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
public class Colorant {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "CNTID")
    public int CNTID;

    @ColumnInfo(name = "LIQUIDID")
    public int LIQUIDID;

    @ColumnInfo(name = "CNTCODE")
    public String CNTCODE;

    @ColumnInfo(name = "RGB")
    public int rgb;

    @ColumnInfo(name = "SPECIFICGRAVITY")
    public Float SPECIFICGRAVITY;
}
