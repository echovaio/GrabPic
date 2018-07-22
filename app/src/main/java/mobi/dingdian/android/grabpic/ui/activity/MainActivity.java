package mobi.dingdian.android.grabpic.ui.activity;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import mobi.dingdian.android.grabpic.R;
import mobi.dingdian.android.grabpic.bean.entity.Sister;
import mobi.dingdian.android.grabpic.imgloader.PictureLoader;
import mobi.dingdian.android.grabpic.imgloader.SisterLoader;
import mobi.dingdian.android.grabpic.network.SisterApi;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button showBtn;
    private Button refreshBtn;
    private ImageView showImg;
    private ArrayList<Sister> data;
    private int curPos = 0;
    private int page = 1;
    private PictureLoader loader;
    private SisterApi sisterApi;
    private SisterLoader mSisterLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sisterApi = new SisterApi();
        loader = new PictureLoader();
        mSisterLoader = SisterLoader.getInstance(MainActivity.this);
        initData();
        initUI();
    }

    private void initData() {
        data = new ArrayList<>();
        new SisterTask(page).execute();
    }

    private void initUI() {
        showBtn = findViewById(R.id.btn_show);
        refreshBtn = findViewById(R.id.btn_refresh);
        showImg = findViewById(R.id.img_show);
        showBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show:
                if (data != null && !data.isEmpty()) {
                    if (curPos > 9) {
                        curPos = 0;
                    }
                    //loader.load(showImg, data.get(curPos).getUrl());
                    mSisterLoader.bindBitmap(data.get(curPos).getUrl(), showImg, 400, 400);
                    curPos++;
                }
                break;
            case R.id.btn_refresh:
                page++;
                new SisterTask(page).execute();
                curPos = 0;
                break;
        }
    }

    private class SisterTask extends AsyncTask<Void, Void, ArrayList<Sister>> {

        private int page;

        public SisterTask(int page) {
            this.page = page;
        }

        @Override
        protected ArrayList<Sister> doInBackground(Void... voids) {
            return sisterApi.fetchSister(10, page);
        }

        @Override
        protected void onPostExecute(ArrayList<Sister> sisters) {
            super.onPostExecute(sisters);
            data.clear();
            data.addAll(sisters);
            Toast.makeText(MainActivity.this, "Refresh success!", Toast.LENGTH_SHORT).show();
        }
    }
}
