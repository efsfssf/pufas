package com.dandomi.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "PRODUCTID",
                childColumns = "PARENTPRODUCTID",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("PARENTPRODUCTID")}
)
public class Product {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PRODUCTID")
    public int productId;

    @ColumnInfo(name = "PARENTPRODUCTID")
    public Integer parentProductId;

    @ColumnInfo(name = "PRODUCTNAME")
    public String productName;

    @ColumnInfo(name = "PRIMERCARDID")
    public Integer primerCardId;

    @NonNull
    @Override
    public String toString() {
        return productName;
    }
}
