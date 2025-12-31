package com.dandomi.pufas;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dandomi.db.Color;

import java.util.ArrayList;
import java.util.List;

public class ColorAdapter extends ArrayAdapter<Color> {

    // Храним копию всех элементов для фильтрации (поиска), если пользователь начнет вводить текст
    private List<Color> itemsAll;

    private static final int TYPE_COLOR = 0;
    private static final int TYPE_DIVIDER_RECENT = 1;
    private static final int TYPE_DIVIDER_OTHER = 2;

    public static final Color DIVIDER_RECENT = new Color();
    public static final Color DIVIDER_OTHER  = new Color();


    public ColorAdapter(Context context, List<Color> items) {
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

        // обычный цвет
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_dropdown_color, parent, false);
        }

        // Получаем текущий элемент
        Color item = getItem(position);

        if (item != null) {
            TextView nameView = convertView.findViewById(R.id.color_name);
            ImageView colorView = convertView.findViewById(R.id.color_view);

            // Устанавливаем текст
            nameView.setText(item.toString());

            // Устанавливаем цвет кружка

            if (item.rgb == null) {
                colorView.setImageResource(R.drawable.question_mark_20px);
                colorView.setBackground(null);
            }
            else {
                int color = 0xFF000000 | item.rgb;
                GradientDrawable d = new GradientDrawable();
                d.setShape(GradientDrawable.OVAL);
                d.setColor(color);

                colorView.setImageDrawable(null);
                colorView.setBackground(d);
            }
        }

        return convertView;
    }

    private int dp(int dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
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
        Color item = getItem(position);

        if (item == DIVIDER_RECENT) return TYPE_DIVIDER_RECENT;
        if (item == DIVIDER_OTHER)  return TYPE_DIVIDER_OTHER;

        return TYPE_COLOR;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

}