package com.dandomi.pufas.controllers;

import android.transition.TransitionManager;
import android.view.View;
import android.webkit.WebSettings;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.dandomi.pufas.ImportDatabaseActivity;
import com.sjapps.jsonlist.R;
import com.dandomi.pufas.pufas.JsonFunctions;
import com.dandomi.pufas.pufas.controllers.RawJsonView;

public class AndroidRawJsonView extends RawJsonView {

    ImportDatabaseActivity importDatabaseActivity;


    public AndroidRawJsonView(ImportDatabaseActivity importDatabaseActivity, int textColor, int keyColor, int numberColor, int booleanAndNullColor, int bgColor) {
        super(textColor, keyColor, numberColor, booleanAndNullColor, bgColor);
        this.importDatabaseActivity = importDatabaseActivity;
        setup();
    }

    private void setup(){
        WebSettings webSettings = importDatabaseActivity.rawJsonWV.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
    }

    @Override
    public void toggleSplitView() {
        TransitionManager.endTransitions(importDatabaseActivity.viewGroup);
        TransitionManager.beginDelayedTransition(importDatabaseActivity.viewGroup, importDatabaseActivity.autoTransition);

        if (showJson){
            if (importDatabaseActivity.isVertical)
                importDatabaseActivity.rawJsonRL.animate()
                        .translationY(importDatabaseActivity.rawJsonRL.getHeight())
                        .setDuration(400)
                        .withEndAction(()-> importDatabaseActivity.rawJsonRL.setVisibility(View.GONE))
                        .start();
            else importDatabaseActivity.rawJsonRL.animate()
                    .translationX(importDatabaseActivity.rawJsonRL.getWidth())
                    .setDuration(400)
                    .withEndAction(()-> importDatabaseActivity.rawJsonRL.setVisibility(View.GONE))
                    .start();


            importDatabaseActivity.resizeSplitViewBtn.animate()
                    .scaleX(.5f)
                    .scaleY(.5f)
                    .withEndAction(() -> importDatabaseActivity.resizeSplitViewBtn.setVisibility(View.GONE))
                    .setDuration(150)
                    .start();
            showJson = false;
            if (importDatabaseActivity.listRL.getVisibility() == View.GONE)
                importDatabaseActivity.listRL.setVisibility(View.VISIBLE);

            importDatabaseActivity.guideLine.setGuidelinePercent(1f);
            return;
        }
        showJson = true;
        importDatabaseActivity.rawJsonRL.setVisibility(View.VISIBLE);

        importDatabaseActivity.guideLine.setGuidelinePercent(0.5f);
        importDatabaseActivity.handler.postDelayed(()->{
                    importDatabaseActivity.resizeSplitViewBtn.setVisibility(View.VISIBLE);
                    importDatabaseActivity.resizeSplitViewBtn.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(150)
                            .start();
                },
                350);
        importDatabaseActivity.rawJsonRL.animate().cancel();

        importDatabaseActivity.rawJsonRL.animate()
                .translationY(0)
                .translationX(0)
                .setDuration(400)
                .start();

        if (!isRawJsonLoaded)
            ShowJSON();
    }

    @Override
    public void ShowJSON() {
        if (importDatabaseActivity.data.getRawData().equals("-1")) {
            Snackbar.make(importDatabaseActivity.getWindow().getDecorView(), R.string.file_is_to_large_to_be_shown_in_a_split_screen, BaseTransientBottomBar.LENGTH_SHORT).show();
            if (importDatabaseActivity.progressView.getVisibility() == View.VISIBLE)
                importDatabaseActivity.loadingFinished(true);
            if (showJson)
                toggleSplitView();
            return;
        }
        if (importDatabaseActivity.data.getRawData().equals(""))
            return;

        importDatabaseActivity.loadingStarted(importDatabaseActivity.getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(importDatabaseActivity.data.getRawData());
            importDatabaseActivity.handler.post(()-> {
                updateRawJson(dataStr);
                importDatabaseActivity.loadingFinished(true);
                isRawJsonLoaded = true;
            });
        });
        thread.setName("loadingJson");
        thread.start();
    }

    public void updateRawJson(String json) {
        String htmlData = generateHtml(json, importDatabaseActivity.state);
        importDatabaseActivity.rawJsonWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

}
