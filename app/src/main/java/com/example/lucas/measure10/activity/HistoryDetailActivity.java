package com.example.lucas.measure10.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

import com.example.lucas.measure10.R;
import com.example.lucas.measure10.db.ImageHelper;
import com.example.lucas.measure10.model.Image;
import com.example.lucas.measure10.view.CameraView;

public class HistoryDetailActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        init(); // 初始化
    }

    private void init() { // 初始化
        ImageHelper ih = new ImageHelper(this); // 產生與資料庫溝通的圖片物件
        Image image = ih.get(getIntent().getExtras().getString("first"))[0]; // 取得原始圖片資料
        ih.close(); // 關閉資料庫連結

        try {
            BitmapFactory.Options options = new BitmapFactory.Options(); // 防止OOM(Out of Memory 所設定的選項物件
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inSampleSize = 2;

            String result = image.getResult(); // 取得處理圖片時所獲得的結果資料
            if (result == null)
                result = image.getName(); // 取得圖片檔名
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(CameraView.SAVE_PATH + "/" + result), null, options); // 讀取原始圖檔

            Matrix matrix = new Matrix(); // 圖片旋轉 90 度所設定
            matrix.setRotate(90.0f);

            ((ImageView) findViewById(R.id.image)).setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)); // 取得顯示圖片物件並重新建立翻轉 90 度的圖片並設定到顯示圖片的物件上
            ((TextView) findViewById(R.id.detail)).setText(  // 取得顯示結果資料的物件並設定結果
                    "日期：" + ImageHelper.SDF.format(new Date(Long.parseLong(result.substring(result.indexOf("-") + 1, result.indexOf("-", result.indexOf("-") + 1))))) + "\n\n" +
                            image.getMessage()
            );
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ((Button)findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() { // 設定返回按鈕被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish(); // 結束 HistoryDetailActivity
            }

        });

        ((Button)findViewById(R.id.home)).setOnClickListener(new View.OnClickListener() { // 設定回首頁按鈕被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setResult(1); // 設定返回給 HistoryActivity 的結果
                finish(); // 結束 HistoryDetailActivity
            }

        });

        ((Button)findViewById(R.id.upload)).setOnClickListener(new View.OnClickListener() { // 設定上傳按鈕被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse("http://10.32.10.67/WebSite1/Default.aspx"); // 設定網址
                Intent it = new Intent(Intent.ACTION_VIEW, uri); // 產生新的意圖
                startActivity(it); // 開啟瀏覽器並前往網址
            }

        });
    }

}
