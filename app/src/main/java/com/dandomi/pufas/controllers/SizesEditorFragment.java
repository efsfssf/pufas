package com.dandomi.pufas.controllers;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.*;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dandomi.pufas.controllers.SizesEditorFragment.SizesAdapter;
import com.dandomi.pufas.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.sjapps.library.customdialog.adapter.DefaultListAdapterGeneric;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SizesEditorFragment extends BottomSheetDialogFragment {

    private List<String> sizes;
    private SizesAdapter adapter;

    @Override
    public void onStart() {
        super.onStart();
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = (com.google.android.material.bottomsheet.BottomSheetDialog) getDialog();
        if (dialog != null) {
            // Находим нижний контейнер
            android.view.View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            // Получаем поведение (Behavior)
            com.google.android.material.bottomsheet.BottomSheetBehavior<android.view.View> behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);

            // 1. Сразу раскрываем на всю высоту
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);

            // 2. (Опционально) Запрещаем сворачивать в "половинчатое" состояние при свайпе вниз
            behavior.setSkipCollapsed(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saveInstanceState) {
        return inflater.inflate(R.layout.fragment_sizes_editor, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle saveInstanceState) {
        super.onViewCreated(view, saveInstanceState);

        // 1. Загрузка данных
        sizes = SizesRepository.loadSizes(requireContext());

        // 2. Настраиваем RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerSizes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SizesAdapter(sizes, new SizesAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                showEditDialog(position);
            }

            @Override
            public void onDeleteClick(int position) {
                sizes.remove(position);
                adapter.notifyItemRemoved(position);
                save();
                //updateFabState();
            }
        });
        recyclerView.setAdapter(adapter);

        // 3. Кнопка добавить
        view.findViewById(R.id.fabAddSize).setOnClickListener(view1 -> {
            if (sizes.size() >= 20) {
                Snackbar.make(view, "Достигнут лимит (20 значений)", Snackbar.LENGTH_SHORT)
                        // Самая важная строчка для красоты:
                        // Привязываем его к кнопке, чтобы он появился НАД ней, а не перекрыл её
                        .setAnchorView(view1)
                        .show();
                return;
            }

            showAddDialog();
        });
    }

    private void save() {
        SizesRepository.saveSizes(requireContext(), sizes);
    }

    private void showAddDialog() {
        showSizeInputDialog(getString(R.string.add_volume), null, -1);
    }

    private void showEditDialog(int position) {
        showSizeInputDialog(getString(R.string.edit), sizes.get(position), position);
    }

    private void showSizeInputDialog(String title, @Nullable String currentValue, int position) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.imput_liters_hit);
        if (currentValue != null) input.setText(currentValue);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String newValue = input.getText().toString().trim();
                    if (!newValue.isEmpty()) {
                        if (position == -1) {
                            // add
                            sizes.add(newValue);
                            adapter.notifyItemInserted(sizes.size() - 1);
                            //updateFabState();
                        } else {
                            // Edit
                            sizes.set(position, newValue);
                            adapter.notifyItemChanged(position);
                        }
                        save();
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }



    private void updateFabState() {
        FloatingActionButton fab = requireActivity().findViewById(R.id.fabAddSize);
        if (sizes.size() >= 20) {
            fab.setEnabled(false);
        } else {
            fab.setEnabled(true);
        }
    }

    // --- Простой внутренний адаптер ---
    public static class SizesAdapter extends RecyclerView.Adapter<SizesAdapter.ViewHolder> {
        private final List<String> data;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onEditClick(int position);
            void onDeleteClick(int position);
        }

        SizesAdapter(List<String> data, OnItemClickListener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_size_setting, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.tvValue.setText(data.get(position));

            // Клик по корзине - удаление
            holder.btnDelete.setOnClickListener(view -> listener.onDeleteClick(holder.getAdapterPosition()));

            // Клик по самой карточке - редактирование
            holder.itemView.setOnClickListener(view -> listener.onEditClick(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvValue;
            MaterialButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvValue = itemView.findViewById(R.id.tvSizeValue);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

    }
}
