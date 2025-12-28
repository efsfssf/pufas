package com.dandomi.pufas.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SizesRepository {

    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_SIZES = "key_custom_sizes";
    private static final String DEFAULT_SIZES = "0.3,0.5,1.0,1.5";


    public static List<String> loadSizes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_SIZES, DEFAULT_SIZES);

        return new ArrayList<>(Arrays.asList(TextUtils.split(saved, ",")));
    }

    public static void saveSizes(Context context, List<String> sizes) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String joined = TextUtils.join(",", sizes);
        prefs.edit().putString(KEY_SIZES, joined).apply();
    }
}
