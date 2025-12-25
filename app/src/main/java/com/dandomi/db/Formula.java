package com.dandomi.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Color.class,
                parentColumns = "COLORID",
                childColumns = "COLORID",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("COLORID")}
)
public class Formula {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "FORMULAID")
    public int formulaId;

    @ColumnInfo(name = "ABASEID")
    public int aBaseId;

    @ColumnInfo(name = "COLORID")
    public int colorId;

    @ColumnInfo(name = "CNTINFORMULA")
    public String cntInFormula;
}
