package com.sjapps.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "productId",
                childColumns = "parentProductId",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("parentProductId")}
)
public class Product {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PRODUCTID")
    public int productId;

    @ColumnInfo(name = "PARENTPRODUCTID")
    public int parentProductId;

    @ColumnInfo(name = "PRODUCTNAME")
    public String productName;

    @ColumnInfo(name = "PRIMERCARDID")
    public int primerId;
}
