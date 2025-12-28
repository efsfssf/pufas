package com.dandomi.db;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DatabaseExportUtil {

    public static File exportDatabase(Context context) throws IOException {
        AppDatabase db = AppDatabase.getDbInstance(context);

        JSONObject root = new JSONObject();

        try {
            root.put("Color", colorsToJSONArray(db.colorDao().getAllColors()));
            root.put("Colorant", colorantsToJSONArray(db.colorantDao().getAllColorants()));
            root.put("ColorInProduct", colorInProductsToJSONArray(db.colorInProductDao().getAllColorInProducts()));
            root.put("Formula", formulasToJSONArray(db.formulaDao().getAllFormulas()));
            root.put("Product", productsToJSONArray(db.productDao().getAllProducts()));
            root.put("Basepaint", basepaintsToJSONArray(db.basepaintDao().getAllBasepaints()));

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        File file = new File(context.getCacheDir(), "database_export.json");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(root.toString().getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }

    public static JSONArray basepaintsToJSONArray(List<Basepaint> basepaints) {
        JSONArray arr = new JSONArray();
        for (Basepaint basepaint : basepaints) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("BASEID", basepaint.baseId);
                obj.put("PRODUCTID", basepaint.productId);
                obj.put("ABASEID", basepaint.abaseId);
                obj.put("BASECODE", basepaint.baseCode);
                obj.put("SPECIFICGRAVITY", basepaint.specificGravity);
                obj.put("MINFILL", basepaint.MINFILL);
                obj.put("MAXFILL", basepaint.MAXFILL);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray productsToJSONArray(List<Product> products) {
        JSONArray arr = new JSONArray();
        for (Product product : products) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("PRODUCTID", product.productId);
                obj.put("PARENTPRODUCTID", product.parentProductId);
                obj.put("PRODUCTNAME", product.productName);
                obj.put("PRIMERCARDID", product.primerCardId);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray formulasToJSONArray(List<Formula> formulas) {
        JSONArray arr = new JSONArray();
        for (Formula formula : formulas) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("FORMULAID", formula.formulaId);
                obj.put("ABASEID", formula.aBaseId);
                obj.put("COLORID", formula.colorId);
                obj.put("CNTINFORMULA", formula.cntInFormula);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray colorInProductsToJSONArray(List<ColorInProduct> colorInProducts) {
        JSONArray arr = new JSONArray();
        for (ColorInProduct colorInProduct : colorInProducts) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("COLORINPRODUCTID", colorInProduct.colorInProductId);
                obj.put("COLORID", colorInProduct.colorId);
                obj.put("PRODUCTID", colorInProduct.productId);
                obj.put("VERSION", colorInProduct.version);
                obj.put("FORMULAID", colorInProduct.formulaId);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray colorantsToJSONArray(List<Colorant> colorants) {
        JSONArray arr = new JSONArray();
        for (Colorant colorant : colorants) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("CNTID", colorant.CNTID);
                obj.put("LIQUIDID", colorant.LIQUIDID);
                obj.put("CNTCODE", colorant.CNTCODE);
                obj.put("RGB", colorant.rgb);
                obj.put("SPECIFICGRAVITY", colorant.SPECIFICGRAVITY);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;
    }

    public static JSONArray colorsToJSONArray(List<Color> colors) {
        JSONArray arr = new JSONArray();
        for (Color color : colors) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("COLORID", color.colorId);
                obj.put("COLORCODE", color.colorCode);
                obj.put("RGB", color.rgb);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            arr.put(obj);
        }
        return arr;

    }
}
