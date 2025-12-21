package com.sjapps.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Formula.class, Color.class, Product.class, ColorInProduct.class, Colorant.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FormulaDao formulaDao();
    public abstract ColorDao colorDao();
    public abstract ProductDao productDao();
    public abstract ColorInProductDao colorInProductDao();
    public abstract ColorantDao colorantDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDbInstance(Context context) {
        if (INSTANCE == null)
        {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "avatintlocal")
                    .allowMainThreadQueries()
                    .build();
        }

        return INSTANCE;
    }
}
