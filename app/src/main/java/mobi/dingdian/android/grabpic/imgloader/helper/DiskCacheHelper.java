package mobi.dingdian.android.grabpic.imgloader.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import mobi.dingdian.android.grabpic.imgloader.SisterCompress;
import mobi.dingdian.android.grabpic.imgloader.disklrucache.DiskLruCache;

public class DiskCacheHelper {
    private static final String TAG = "DiskCacheHelper";
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int DISK_CACHE_INDEX = 0;

    private Context mContext;
    private DiskLruCache mDiskLruCache;
    private SisterCompress mCompress;
    private boolean mIsDiskLruCacheCreated = false;

    public DiskCacheHelper(Context mContext) {
        this.mContext = mContext;
        mCompress = new SisterCompress();
        File diskCacheDir = getDiskCacheDir(mContext, "diskCache");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdir();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getDiskCacheDir(Context mContext, String dirName) {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String cachePath = null;
        if (externalStorageAvailable) {
            cachePath = mContext.getExternalCacheDir().getPath();
        } else {
            cachePath = mContext.getCacheDir().getPath();
        }
        Log.v(TAG, "diskCachePath = " + cachePath);
        return new File(cachePath + File.separator + dirName);
    }

    private long getUsableSpace(File path) {
        return path.getUsableSpace();
    }

    /* 根据key加载磁盘缓存中的图片 */
    public Bitmap loadBitmapFromDiskCache(String key, int reqWidth, int reqHeight) throws IOException {
        Log.v(TAG, "加载磁盘缓存中的图片");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在UI线程中加载图片");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mCompress.decodeBitmapFromFileDescriptor(fileDescriptor, reqWidth, reqHeight);
        }
        return bitmap;
    }

    /* 将图片字节缓存到磁盘，并返回Bitmap用于显示 */
    public Bitmap saveImgByte(String key, int reqWidth, int reqHeight, byte[] bytes) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在UI线程中做网络操作");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream output = editor.newOutputStream(DISK_CACHE_INDEX);
                output.write(bytes);
                output.flush();
                editor.commit();
                output.close();
                return loadBitmapFromDiskCache(key, reqWidth, reqHeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DiskLruCache getDiskLruCache() {
        return mDiskLruCache;
    }

    public boolean getIsDiskLruCacheCreated() {
        return mIsDiskLruCacheCreated;
    }

    public void setDiskLruCacheCreated(boolean diskLruCacheCreated) {
        this.mIsDiskLruCacheCreated = diskLruCacheCreated;
    }
}
