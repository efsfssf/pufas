package com.dandomi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dandomi.pufas.HistoryItem;
import com.dandomi.pufas.MainViewModel;
import com.dandomi.pufas.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> items;

    public HistoryAdapter(List<HistoryItem> items) {
        this.items = items;
    }

    @NotNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);

        holder.header.setText(item.getHeader(holder.itemView.getContext()));

        StringBuilder sb = new StringBuilder();
        for (MainViewModel.FormulaItem f : item.getColorants()) {
            sb.append(f.colorantCode)
                    .append(": ")
                    .append(String.format(Locale.US, "%.1f", f.amount))
                    .append(" ml\n");
        }

        holder.colorants.setText(sb.toString().trim());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clearItems() {
        items.clear(); // Чистим сам список в памяти
        notifyDataSetChanged(); // Говорим RecyclerView перерисоваться
    }

    public void setItems(List<HistoryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView header, colorants;

        ViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.textHeader);
            colorants = itemView.findViewById(R.id.textColorants);
        }
    }
}
