package com.example.lucas.measure10.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.lucas.measure10.R;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(); // 初始化
        if(OpenCVLoader.initDebug())
        {
            Toast.makeText(getApplicationContext(),"OpenCV loaded Successful",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Could not load openCV",Toast.LENGTH_SHORT).show();
        }
    }



    private void init() { // 初始化
        ((Button) findViewById(R.id.camera)).setOnClickListener(new OnClickListener() { // 設定相機按鈕被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, CameraActivity.class)); // 前往 CameraActivity
            }

        });

        ((Button) findViewById(R.id.history)).setOnClickListener(new OnClickListener() { // 設定歷史訊息按鈕被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, HistoryActivity.class)); // 前往 HistoryActivity
            }

        });
    }

}