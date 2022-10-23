package com.ramotion.expandingcollection;

import android.content.Context;
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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import io.alterac.blurkit.BlurKit;

/**
 * Worker for async processing bitmaps through cache {@link BackgroundBitmapCache}
 */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<Context> mContextReference;
    private final BackgroundBitmapCache cache;
    private final Integer mProvidedBitmapResId;
    private String imageUrl;
    private int downScale;
    private int blurRadius;

    public BitmapWorkerTask(Context context, @DrawableRes Integer providedBitmapResId, String imageUrl, int downScale, int blurRadius) {
        this.mContextReference = new WeakReference<>(context);
        this.cache = BackgroundBitmapCache.getInstance();
        this.mProvidedBitmapResId = providedBitmapResId;
        this.imageUrl = imageUrl;
        this.downScale = downScale;
        this.blurRadius = blurRadius;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        Integer key = params[0];
        Bitmap cachedBitmap = cache.getBitmapFromBgMemCache(String.valueOf(imageUrl != null ? imageUrl : key));
        if (cachedBitmap == null && mContextReference.get() != null) {
            if(imageUrl != null){
                try {
                    Bitmap bitmap = Glide.with(mContextReference.get()).asBitmap().load(imageUrl).submit().get();
                    cache.addBitmapToBgMemoryCache(imageUrl, processBitmap(bitmap, downScale, blurRadius));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                cachedBitmap = BitmapFactory.decodeResource(mContextReference.get().getResources(), mProvidedBitmapResId, new BitmapFactoryOptions());
                cache.addBitmapToBgMemoryCache(String.valueOf(mProvidedBitmapResId), processBitmap(cachedBitmap, downScale, blurRadius));
            }
        }
        return cachedBitmap;
    }

    public static Bitmap processBitmap(Bitmap cachedBitmap, int downScale, int blurRadius){
        cachedBitmap = resize(cachedBitmap, cachedBitmap.getWidth() / downScale, cachedBitmap.getHeight() / downScale);
        darkenBitMap(cachedBitmap);
        return BlurKit.getInstance().blur(cachedBitmap, blurRadius);
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
