package com.dandomi.db;

import androidx.annotation.NonNull;
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
    public Integer rgb;

    @NonNull
    @Override
    public String toString() {
        return colorCode;
    }

}
