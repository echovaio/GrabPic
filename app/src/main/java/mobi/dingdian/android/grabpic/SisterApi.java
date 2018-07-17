package mobi.dingdian.android.grabpic;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SisterApi {
    private static final String TAG = "Network";
    private static final String BASE_URL = "http://gank.io/api/data/福利/";

    public ArrayList<Sister> fetchSister(int count, int page) {
        String fetchUrl = BASE_URL + count + "/" + page;
        ArrayList<Sister> sisters = new ArrayList<>();
        try {
            URL url = new URL(fetchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            Log.v(TAG, "Server response: " + code);
            if (code == 200) {
                InputStream in = conn.getInputStream();
                byte[] data = readFromStream(in);
                String result = new String(data, "UTF-8");
                sisters = parseSister(result);
            } else {
                Log.e(TAG, "请求失败: " + code);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sisters;
    }

    private ArrayList<Sister> parseSister(String result) {
        ArrayList<Sister> sisters = new ArrayList<>();
        return sisters;
    }

    private byte[] readFromStream(InputStream in) {
        byte[] buffer = new byte[1024];
        return buffer;
    }
}
