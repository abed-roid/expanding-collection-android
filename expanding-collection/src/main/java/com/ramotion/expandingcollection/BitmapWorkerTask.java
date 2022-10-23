package com.ramotion.expandingcollection;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;

import androidx.annotation.DrawableRes;

import io.alterac.blurkit.BlurKit;

/**
 * Worker for async processing bitmaps through cache {@link BackgroundBitmapCache}
 */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

    private final Resources mResources;
    private final BackgroundBitmapCache cache;
    private final int mProvidedBitmapResId;
    private int downScale;
    private int blurRadius;

    public BitmapWorkerTask(Resources resources, @DrawableRes int providedBitmapResId, int downScale, int blurRadius) {
        this.mResources = resources;
        this.cache = BackgroundBitmapCache.getInstance();
        this.mProvidedBitmapResId = providedBitmapResId;
        this.downScale = downScale;
        this.blurRadius = blurRadius;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        Integer key = params[0];
        Bitmap cachedBitmap = cache.getBitmapFromBgMemCache(key);
        if (cachedBitmap == null) {
            cachedBitmap = BitmapFactory.decodeResource(mResources, mProvidedBitmapResId, new BitmapFactoryOptions());
            cachedBitmap = resize(cachedBitmap, cachedBitmap.getWidth() / downScale, cachedBitmap.getHeight() / downScale);
            darkenBitMap(cachedBitmap);
            cachedBitmap = BlurKit.getInstance().blur(cachedBitmap, blurRadius);
            cache.addBitmapToBgMemoryCache(mProvidedBitmapResId, cachedBitmap);
        }
        return cachedBitmap;
    }


    public static Bitmap resize(Bitmap bitmap, int width, int height){
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    public static Bitmap darkenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(0xFFCCCCCC, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }
}
