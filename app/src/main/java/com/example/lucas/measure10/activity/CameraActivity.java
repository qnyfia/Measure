package com.example.lucas.measure10.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.lucas.measure10.R;
import com.example.lucas.measure10.view.CameraFocusView;
import com.example.lucas.measure10.view.CameraView;

public class CameraActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        init(); // 初始化
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 當 ImageActivity 結束時回傳結果
        // TODO Auto-generated method stub
        switch (resultCode) {
            case 1: // 當回傳結果為 1 時
                finish(); // 結束 CameraActivity
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() { // 初始化
        FrameLayout fl = ((FrameLayout) findViewById(R.id.preview)); // 取得 CameraActivity 的 Layout
        final CameraView cv = new CameraView(this); // 產生顯示鏡頭影像的物件
        fl.addView(cv); // 設定顯示鏡頭影像的物件到 CameraActivity 的 Layout 上
        fl.addView(new CameraFocusView(this), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)); // 產生並設定新的 Layout 到 CameraActivity 上並設定對焦框的物件到新的 Layout 上

        ((Button) findViewById(R.id.take)).setOnClickListener(new View.OnClickListener() { // 設定拍照按鈕的 Button 被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                cv.cameraTake(); // 呼叫顯示鏡頭影像的物件做拍照的動作
            }

        });

        ((Button) findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() { // 設定取消按鈕的 Button 被點擊時

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish(); // 結束 CameraActivity
            }

        });
    }

}