package mobi.dingdian.android.grabpic.imgloader.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class MemoryCacheHelper {
    private static final String TAG = "MemoryCacheHelper";

    private Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;

    public MemoryCacheHelper(Context mContext) {
        this.mContext = mContext;
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public LruCache<String, Bitmap> getMemoryCache() {
        return mMemoryCache;
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        Log.v(TAG, "加载内存缓存中的图片");
        return mMemoryCache.get(key);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            Log.v(TAG, "往内存缓存中添加图片");
            mMemoryCache.put(key, bitmap);
        }
    }
}
