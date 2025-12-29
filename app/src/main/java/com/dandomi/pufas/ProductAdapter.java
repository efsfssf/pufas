package com.dandomi.pufas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dandomi.db.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    // Храним копию всех элементов для фильтрации (поиска), если пользователь начнет вводить текст
    private List<Product> itemsAll;

    private static final int TYPE_Product = 0;
    private static final int TYPE_DIVIDER_RECENT = 1;
    private static final int TYPE_DIVIDER_OTHER = 2;

    public static final Product DIVIDER_RECENT = new Product();
    public static final Product DIVIDER_OTHER  = new Product();


    public ProductAdapter(Context context, List<Product> items) {
        super(context, 0, items);
        this.itemsAll = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Если view еще не создана, "надуваем" её из нашего XML
        if (getItemViewType(position) == TYPE_DIVIDER_RECENT) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_dropdown_divider, parent, false);
            }
            return convertView;
        }

        if (getItemViewType(position) == TYPE_DIVIDER_OTHER) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_dropdown_divider_other, parent, false);
            }
            return convertView;
        }

        // обычная банка
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_dropdown_product, parent, false);
        }

        // Получаем текущий элемент
        Product item = getItem(position);

        if (item != null) {
            TextView nameView = convertView.findViewById(R.id.product_name);

            // Устанавливаем текст
            nameView.setText(item.toString());
        }

        return convertView;
    }

    // (Опционально) Чтобы работала фильтрация при вводе текста
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Product> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    suggestions.addAll(itemsAll);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Product item : itemsAll) {
                        if (item == DIVIDER_RECENT || item == DIVIDER_OTHER) continue;
                        if (item.toString().toLowerCase().contains(filterPattern)) {
                            suggestions.add(item);
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (results.values != null) {
                    addAll((List) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        Product item = getItem(position);

        if (item == DIVIDER_RECENT) return TYPE_DIVIDER_RECENT;
        if (item == DIVIDER_OTHER)  return TYPE_DIVIDER_OTHER;

        return TYPE_Product;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

}