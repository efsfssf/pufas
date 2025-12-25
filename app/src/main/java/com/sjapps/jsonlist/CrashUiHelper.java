package com.sjapps.jsonlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Toolbar;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;
import com.sj14apps.jsonlist.core.AppState;

public final class CrashUiHelper {

    private CrashUiHelper() {}

    public static void apply(
            @NonNull Activity activity,
            @NonNull View menuBtn,
            @NonNull TextView logBtn
    ) {

        AppState state = FileSystem.loadStateData(activity);

        if (!state.hasCrashLogs()) {
            logBtn.setVisibility(View.GONE);
            menuBtn.setBackground(null);
            return;
        }

        logBtn.setVisibility(View.VISIBLE);

        TypedValue typedValue = new TypedValue();

        if (state.hasNewCrash()) {
            activity.getTheme()
                    .resolveAttribute(R.attr.colorOnError, typedValue, true);

            logBtn.setTextColor(typedValue.data);
            logBtn.setBackgroundResource(R.drawable.ripple_red);

            if (menuBtn instanceof ImageButton) {
                ((ImageButton) menuBtn)
                        .setImageResource(R.drawable.menu_with_dot);
            }
        } else {
            activity.getTheme()
                    .resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);

            logBtn.setTextColor(typedValue.data);
            logBtn.setBackgroundResource(R.drawable.ripple_list2);

            if (menuBtn instanceof ImageButton) {
                ((ImageButton) menuBtn)
                        .setImageResource(R.drawable.ic_menu);
            }
        }
    }

    public static void apply(
            @NonNull Context context,
            @NonNull Menu menu
    ) {
        MenuItem logItem = menu.findItem(R.id.nav_log);
        if (logItem == null) return;

        AppState state = FileSystem.loadStateData(context);

        if (!state.hasCrashLogs()) {
            logItem.setVisible(false);
            return;
        }

        logItem.setVisible(true);

        if (state.hasNewCrash()) {
            // 1. Берем исходную иконку (жука)
            Drawable baseIcon = ContextCompat.getDrawable(context, R.drawable.frame_bug_24px);
            if (baseIcon == null) return;
            baseIcon = baseIcon.mutate(); // Обязательно, чтобы не испортить оригинал

            // 2. Получаем цвет иконки из темы (обычно черный или белый)
            int iconColor = getThemeColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant);
            if (iconColor == 0) iconColor = Color.BLACK;

            // Красим жука вручную в цвет темы
            DrawableCompat.setTint(baseIcon, iconColor);

            // 3. Создаем красную точку
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(Color.RED);

            // Размер точки
            int dotSize = dpToPx(context, 6);
            dot.setSize(dotSize, dotSize);
            dot.setBounds(0, 0, dotSize, dotSize);

            // 4. Собираем слой: Жук + Точка
            LayerDrawable finalIcon = new LayerDrawable(new Drawable[]{baseIcon, dot});

            // Позиционируем точку (справа сверху)
            finalIcon.setLayerGravity(1, Gravity.TOP | Gravity.END);
            // Настраиваем отступы, чтобы точка сидела красиво
            finalIcon.setLayerInset(1, dpToPx(context, 12), dpToPx(context, 4), dpToPx(context, 4), dpToPx(context, 12));

            // 5. Устанавливаем иконку
            logItem.setIcon(finalIcon);

            // 6. ВАЖНО: Отключаем системную перекраску для этого пункта,
            // иначе красная точка станет цветом текста меню
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                logItem.setIconTintList(null);
            }
        } else {
            logItem.setIcon(R.drawable.frame_bug_24px);
        }
    }


    public static void applyToNavigationMenu(
            @NonNull Context context,
            @NonNull com.google.android.material.navigation.NavigationView navigationView
    ) {
        Menu menu = navigationView.getMenu();
        MenuItem logItem = menu.findItem(R.id.nav_log);
        if (logItem == null) return;

        AppState state = FileSystem.loadStateData(context);

        if (!state.hasCrashLogs()) {
            logItem.setVisible(false);
            return;
        }

        logItem.setVisible(true);

        // --- ГЛАВНОЕ ИСПРАВЛЕНИЕ ---

        // 1. Получаем цвет, в который должны быть покрашены ОБЫЧНЫЕ иконки (серый/белый)
        int normalIconColor = getThemeColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant);
        if (normalIconColor == 0) normalIconColor = Color.GRAY;

        // 2. Отключаем автоматическую покраску иконок в NavigationView.
        // Теперь NavigationView будет рисовать иконки "как есть" (оригинальные цвета).
        navigationView.setItemIconTintList(null);

        // 3. Так как мы отключили авто-покраску, нам нужно вручную покрасить
        // ВСЕ остальные иконки меню в нормальный цвет, иначе они станут черными (или дефолтными).
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) {
                Drawable icon = item.getIcon().mutate(); // Важно!
                DrawableCompat.setTint(icon, normalIconColor);
                item.setIcon(icon);
            }
        }

        // --- ТЕПЕРЬ СОЗДАЕМ ИКОНКУ С ТОЧКОЙ ---

        if (state.hasNewCrash()) {
            // Берем жука
            Drawable baseIcon = ContextCompat.getDrawable(context, R.drawable.frame_bug_24px);
            if (baseIcon != null) {
                baseIcon = baseIcon.mutate();
                // Красим самого жука в нормальный цвет (белый/серый)
                DrawableCompat.setTint(baseIcon, normalIconColor);

                // Рисуем красную точку
                GradientDrawable dot = new GradientDrawable();
                dot.setShape(GradientDrawable.OVAL);
                dot.setColor(Color.RED); // Красный цвет сохранится, т.к. setItemIconTintList(null)

                int dotSize = dpToPx(context, 6);
                dot.setSize(dotSize, dotSize);
                dot.setBounds(0, 0, dotSize, dotSize);

                // Слой
                LayerDrawable finalIcon = new LayerDrawable(new Drawable[]{baseIcon, dot});

                // Позиционируем точку
                finalIcon.setLayerGravity(1, Gravity.TOP | Gravity.END);
                // Подбираем отступы (inset), чтобы точка была красиво сбоку
                finalIcon.setLayerInset(1, dpToPx(context, 12), dpToPx(context, 4), dpToPx(context, 4), dpToPx(context, 12));

                logItem.setIcon(finalIcon);
            }
        } else {
            // Если новых крашей нет, просто ставим обычного жука (уже покрашенного в цикле выше,
            // но для надежности можно явно задать)
            Drawable normalBug = ContextCompat.getDrawable(context, R.drawable.frame_bug_24px);
            if (normalBug != null) {
                normalBug = normalBug.mutate();
                DrawableCompat.setTint(normalBug, normalIconColor);
                logItem.setIcon(normalBug);
            }
        }
    }

    public static void applyToToolbar(@NonNull Context context, @NonNull MaterialToolbar toolbar) {
        AppState state = FileSystem.loadStateData(context);

        // Получаем стандартный цвет иконки из темы (обычно черный или белый)
        int iconColor = getThemeColor(context, com.google.android.material.R.attr.colorOnSurface);
        if (iconColor == 0) iconColor = Color.BLACK; // Fallback

        Drawable menuIcon = ContextCompat.getDrawable(context, R.drawable.menu_24px);
        if (menuIcon == null) return;

        // Важно: мутируем иконку, чтобы не покрасить все остальные иконки в приложении
        menuIcon = menuIcon.mutate();

        if (state.hasCrashLogs() && state.hasNewCrash()) {
            // 1. Красим основную иконку в цвет темы вручную
            DrawableCompat.setTint(menuIcon, iconColor);

            // 2. Создаем красную точку программно (чтобы не нужен был png)
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(Color.RED);

            // Размер точки (в пикселях)
            int dotSize = dpToPx(context, 8);
            dot.setSize(dotSize, dotSize);
            dot.setBounds(0, 0, dotSize, dotSize);

            // 3. Собираем "Слоеный пирог"
            LayerDrawable finalIcon = new LayerDrawable(new Drawable[]{menuIcon, dot});

            // 4. Позиционируем точку (Layer 1) в правом верхнем углу
            // InsetDrawable или просто Gravity внутри LayerDrawable (API 23+)
            finalIcon.setLayerGravity(1, Gravity.TOP | Gravity.END);

            // Немного сдвигаем точку, чтобы она не висела на самом краю (отступы)
            finalIcon.setLayerInset(1, dpToPx(context, 14), dpToPx(context, 4), dpToPx(context, 4), dpToPx(context, 14));

            // 5. Устанавливаем иконку
            toolbar.setNavigationIcon(finalIcon);

            // 6. КРИТИЧЕСКИ ВАЖНО: Убираем тинт самого тулбара,
            // иначе он покрасит нашу красную точку обратно в черный/белый
            // toolbar.setNavigationIconTint(null);

        } else {
            // Если краша нет - возвращаем всё как было

            // Очищаем тинт drawable (на всякий случай)
            DrawableCompat.setTintList(menuIcon, null);
            toolbar.setNavigationIcon(menuIcon);

            // Возвращаем системный тинт тулбару (чтобы иконка красилась темой)
            // toolbar.setNavigationIconTint(iconColor);
        }
    }

    // Вспомогательный метод для получения цвета из атрибутов темы
    private static int getThemeColor(Context context, int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return 0;
    }

    private static int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
