package com.ramotion.expandingcollection;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * LruCache for caching background bitmaps for {@link ECBackgroundSwitcherView}.
 * Key is id of page from {@link ECPager} and value is background bitmap from provided data.
 */
public class BackgroundBitmapCache {
    private LruCache<String, Bitmap> mBackgroundsCache;

    private static BackgroundBitmapCache instance;

    public static BackgroundBitmapCache getInstance() {
        if (instance == null) {
            instance = new BackgroundBitmapCache();
            instance.init();
        }
        return instance;
    }

    private void init() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 5;

        mBackgroundsCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToBgMemoryCache(Integer key, Bitmap bitmap) {
        addBitmapToBgMemoryCache(String.valueOf(key), bitmap);
    }

    public void addBitmapToBgMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromBgMemCache(key) == null) {
            mBackgroundsCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromBgMemCache(String key) {
        return mBackgroundsCache.get(key);
    }

}
