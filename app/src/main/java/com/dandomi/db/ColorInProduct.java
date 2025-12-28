package com.dandomi.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Color.class,
                        parentColumns = "COLORID",
                        childColumns = "COLORID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "PRODUCTID",
                        childColumns = "PRODUCTID",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Formula.class,
                        parentColumns = "FORMULAID",
                        childColumns = "FORMULAID",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("COLORID"),
                @Index("PRODUCTID"),
                @Index("FORMULAID")
        }
)
public class ColorInProduct {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "COLORINPRODUCTID")
    public int colorInProductId;

    @ColumnInfo(name = "COLORID")
    public int colorId;

    @ColumnInfo(name = "PRODUCTID")
    public int productId;

    @ColumnInfo(name = "VERSION")
    public int version;

    @ColumnInfo(name = "FORMULAID")
    public int formulaId;

}
