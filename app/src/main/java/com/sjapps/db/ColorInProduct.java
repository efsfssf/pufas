package com.sjapps.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = {
                @ForeignKey(
                        entity = Color.class,
                        parentColumns = "colorId",
                        childColumns = "colorId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "productId",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Formula.class,
                        parentColumns = "formulaId",
                        childColumns = "formulaId",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("colorId"),
                @Index("productId"),
                @Index("formulaId")
        }
)
public class ColorInProduct {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "COLOURINPRODUCTID")
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
