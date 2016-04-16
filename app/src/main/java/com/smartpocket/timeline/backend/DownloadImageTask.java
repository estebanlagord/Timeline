package com.smartpocket.timeline.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private ImageView view;

    public DownloadImageTask(ImageView view) {
        this.view = view;
    }

    @Override
    protected void onPostExecute(final Bitmap result)
    {
        if (result != null) {
            view.setImageBitmap(result);
            view.setAdjustViewBounds(true);
        }
    }

    @Override
    protected Bitmap doInBackground(String... urlStr)
    {

        Bitmap result = null;
        try {
            InputStream content = (InputStream)new URL(urlStr[0]).getContent();
            result = BitmapFactory.decodeStream(content);
        } catch(Exception e) {
            Log.w("Detalle", "Unable to download image from URL: " + urlStr[0]);
            e.printStackTrace();
        }
        return result;
    }
}