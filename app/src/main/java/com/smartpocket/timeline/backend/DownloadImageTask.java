package com.smartpocket.timeline.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.smartpocket.timeline.R;

import java.io.InputStream;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private Context context;
    private ImageView view;

    public DownloadImageTask(Context context, ImageView view) {
        this.context = context;
        this.view = view;
    }

    @Override
    protected void onPostExecute(final Bitmap result)
    {
        if (result != null) {

            view.setAdjustViewBounds(true);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageBitmap(result);

            Animation animAlphaIn = AnimationUtils.loadAnimation(context, R.anim.alpha_in);
            view.startAnimation(animAlphaIn);
        } else {
            Snackbar.make(view, context.getString(R.string.image_download_error), Snackbar.LENGTH_LONG).show();
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