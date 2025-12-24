package com.sjapps.jsonlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sjapps.db.Color;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;
import java.util.List;

public class ColorAdapter extends ArrayAdapter<Color> {

    // Храним копию всех элементов для фильтрации (поиска), если пользователь начнет вводить текст
    private List<Color> itemsAll;

    private static final int TYPE_COLOR = 0;
    private static final int TYPE_DIVIDER = 1;

    public ColorAdapter(Context context, List<Color> items) {
        super(context, 0, items);
        this.itemsAll = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Если view еще не создана, "надуваем" её из нашего XML
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_dropdown_color, parent, false);
        }

        // Получаем текущий элемент
        Color item = getItem(position);

        if (item != null) {
            TextView nameView = convertView.findViewById(R.id.color_name);
            View colorView = convertView.findViewById(R.id.color_view);

            // Устанавливаем текст
            nameView.setText(item.toString());

            // Устанавливаем цвет кружка
            colorView.setBackgroundColor(0xFF000000 | item.rgb);
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
                List<Color> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    suggestions.addAll(itemsAll);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Color item : itemsAll) {
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
}