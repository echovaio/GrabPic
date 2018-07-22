package mobi.dingdian.android.grabpic.utils;

import android.content.Context;

public class SizeUtils {
    public static int dp2px(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(value * scale + 0.5f);
    }

    public static int px2dp(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(value / scale - 0.5f);
    }
}
