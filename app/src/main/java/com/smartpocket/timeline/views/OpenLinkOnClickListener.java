package com.smartpocket.timeline.views;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

/**
 * OnClickListener used to open a link in the system's browser
 */
public class OpenLinkOnClickListener implements View.OnClickListener {
    private Activity activity;
    private String url;

    public OpenLinkOnClickListener(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
    }
}
