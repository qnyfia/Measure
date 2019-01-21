package com.example.lucas.measure10.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.lucas.measure10.R;
import com.example.lucas.measure10.view.ImageView;

@SuppressWarnings("deprecation")
public class ImageActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        init(); // 初始化
    }

    @Override
    protected void onDestroy() { // 當顯示圖片的 Activity 結束被釋放時
        // TODO Auto-generated method stub
        super.onDestroy();

        ImageView.reset(); // 重新設定 ImageView 有關靜態部份共用的變數
    }

    private void init() { // 初始化
        Bundle bundle = getIntent().getExtras(); // 接收從 CameraActivity 所傳的 Bundle 內含 原始圖片、 canny 與 binary 的檔名

        TabHost th = getTabHost(); // 取得 Tab Layout 物件

        Intent intent = new Intent(this, CannyActivity.class);
        intent.putExtras(bundle);
        th.addTab(th.newTabSpec("canny").setIndicator("Canny").setContent(intent)); // 設定第一個 Tab

        intent = new Intent(this, BinaryActivity.class);
        intent.putExtras(bundle);
        th.addTab(th.newTabSpec("binary").setIndicator("Binary").setContent(intent)); // 設定第二個 Tab

        ((Button) findViewById(R.id.home)).setOnClickListener(new View.OnClickListener() { // 設定回首頁按鈕的 Button 被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setResult(1); // 設定返回給 CameraActivity 的結果
                finish(); // 結束 ImageActivity
            }

        });

        ((Button) findViewById(R.id.reset)).setOnClickListener(new View.OnClickListener() { // 設定重置按鈕的 Button 被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ImageView.clearPoint(); // 清除原有在 ImageView 上所選的點
                Toast.makeText(ImageActivity.this, "重設完成", Toast.LENGTH_SHORT).show(); // 顯示訊息
            }

        });
    }

}
