package com.sjapps.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Color {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "COLORID")
    public int colorId;

    @ColumnInfo(name = "COLORCODE")
    public String colorCode;

    @ColumnInfo(name = "RGB")
    public int rgb;
}
