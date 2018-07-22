package mobi.dingdian.android.grabpic.imgloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import mobi.dingdian.android.grabpic.R;
import mobi.dingdian.android.grabpic.imgloader.helper.DiskCacheHelper;
import mobi.dingdian.android.grabpic.imgloader.helper.MemoryCacheHelper;
import mobi.dingdian.android.grabpic.imgloader.helper.NetworkHelper;
import mobi.dingdian.android.grabpic.utils.NetworkUtils;
import mobi.dingdian.android.grabpic.utils.SizeUtils;

public class SisterLoader {
    private static final String TAG = "SisterLoader";

    public static final int MESSAGE_POST_RESULT = 1;
    private static final int TAG_KEY_URI = R.id.sister_loader_uri;

    private static final int CPU_COUNT= Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;

    private Context mContext;
    private MemoryCacheHelper mMemoryCacheHelper;
    private DiskCacheHelper mDiskCacheHelper;

    /* 线程工厂创建线程 */
    private static final ThreadFactory mFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "SisterLoader#" + mCount);
        }
    };

    /* 线程池管理线程 */
    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(), mFactory);

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView resultImage = result.img;
            // 设置图片大小，并加载图片
            ViewGroup.LayoutParams params = resultImage.getLayoutParams();
            params.width = SizeUtils.dp2px(mContext.getApplicationContext(), result.reqWidth);
            params.height = SizeUtils.dp2px(mContext.getApplicationContext(), result.reqHeight);
            resultImage.setLayoutParams(params);
            resultImage.setImageBitmap(result.bitmap);
            String uri = (String) resultImage.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)) {
                resultImage.setImageBitmap(result.bitmap);
            } else {
                Log.w(TAG, "URL发生改变，不设置图片");
            }
        }
    };

    public static SisterLoader getInstance(Context context) {
        return new SisterLoader(context);
    }

    public SisterLoader(Context context) {
        mContext = context.getApplicationContext();
        mMemoryCacheHelper = new MemoryCacheHelper(mContext);
        mDiskCacheHelper = new DiskCacheHelper(mContext);
    }

    /* 同步加载图片 */
    private Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        final String key = NetworkHelper.hashKeyFromUrl(url);
        Bitmap bitmap = mMemoryCacheHelper.getBitmapFromMemoryCache(key);
        if (bitmap != null) {
            return bitmap;
        }
        try {
            bitmap = mDiskCacheHelper.loadBitmapFromDiskCache(key, reqWidth, reqHeight);
            if (bitmap != null) {
                mMemoryCacheHelper.addBitmapToMemoryCache(key, bitmap);
                return bitmap;
            }
            if (NetworkUtils.isAvailable(mContext)) {
                bitmap = mDiskCacheHelper.saveImgByte(key, reqWidth, reqHeight, NetworkHelper.downloadUrlToStream(url));
                Log.d(TAG, "加载网络上的图片， URL: " + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null && !mDiskCacheHelper.getIsDiskLruCacheCreated()) {
            Log.w(TAG, "磁盘缓存未被创建!");
            bitmap = NetworkHelper.downloadBitmapFromUrl(url);
        }
        return bitmap;
    }

    /* 异步加载图片 */
    public void bindBitmap(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {
        final String key = NetworkHelper.hashKeyFromUrl(url);
        imageView.setTag(TAG_KEY_URI, url);
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, url, bitmap, reqWidth, reqHeight);
                    mHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
}
