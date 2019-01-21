package com.example.lucas.measure10.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.lucas.measure10.R;
import com.example.lucas.measure10.db.ImageHelper;
import com.example.lucas.measure10.model.Image;
import com.example.lucas.measure10.view.CameraView;


public class HistoryActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        init(); // 初始化
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // 當 HistoryDetailActivity 結束時回傳結果
        // TODO Auto-generated method stub
        switch (resultCode) {
            case 1: // 當回傳結果為 1 時
                finish(); // 結束 HistoryActivity
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init() { // 初始化
        final ProgressDialog pd = ProgressDialog.show(this, null, "載入中...", true, false); // 建立並顯示載入中訊息

        Handler handler = new Handler(); // 產生並使用 Handler 物件與正常程序公平搶系統資源
        handler.post(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                final ListView lv = (ListView) findViewById(R.id.images); // 取得 ListView 物件
                final List<Image> imageList = getImageList(); // 取得圖片列表
                lv.setAdapter(new ImageAdapter(HistoryActivity.this, imageList)); // 產生圖片 Adapter 並設定圖片列表 之後設定到 ListView 顯示結果
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() { // 設定 ListView 的項目被點擊時

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // TODO Auto-generated method stub
                        ImageHelper ih = new ImageHelper(HistoryActivity.this); // 產生與資料庫溝通的圖片物件
                        Image images[] = ih.getGroup(imageList.get(position).getName()); // 以群的方式取得圖片資料
                        ih.close(); // 關閉資料庫連結

                        Bundle bundle = new Bundle(); // 產生傳遞訊息給 HistoryDetailActivity 的 Bundle
                        bundle.putString("first", images[0].getName()); // 放入原始圖片檔名
                        bundle.putString("canny", images[1].getName()); // 放入 Canny 檔名
                        bundle.putString("binary", images[2].getName()); // 放入 Binary 檔名

                        Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class); // 產生前往 HistoryDetailActivity 的意圖
                        intent.putExtras(bundle); // 設定 Bundle 給意圖
                        startActivityForResult(intent, 0); // 前往 HistoryDetailActivity
                    }

                });

                lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { // 設定 ListView 的項目被長壓時

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        // TODO Auto-generated method stub
                        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this); // 產生互動式訊息

                        String name = imageList.get(position).getName(); // 取得圖片名稱
                        builder.setMessage(ImageHelper.SDF.format(new Date(Long.parseLong(name.substring(name.indexOf("-") + 1, name.indexOf("-", name.indexOf("-") + 1))))) + " 的歷史資料是否刪除？"); // 設定顯示訊息

                        builder.setPositiveButton("刪除", new DialogInterface.OnClickListener() { // 設定刪除按鈕被點擊時

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                ImageHelper ih = new ImageHelper(HistoryActivity.this); // 產生與資料庫溝通的圖片物件
                                Image image[] = ih.getGroup(imageList.get(position).getName()); // 以群的方式取得圖片資料
                                for (int i=0; i<image.length; i++) {
                                    new File(CameraView.SAVE_PATH + "/" + image[i].getName()).delete(); // 刪除圖片
                                    ih.delete(image[i]); // 刪除在資料庫的圖片資料
                                }
                                ih.close(); // 關閉資料庫連結

                                lv.setAdapter(new ImageAdapter(HistoryActivity.this, getImageList())); // 重設 ListView 狀態
                            }

                        });
                        builder.setNegativeButton("取消", null); // 設定取消按鈕被點擊時
                        builder.create().show(); // 建立並顯示互動式訊息
                        return true;
                    }

                });

                pd.dismiss(); // 載入中訊息關閉
            }

        });
    }

    private List<Image> getImageList() { // 取得圖片資料列表
        List<Image> imageList = new ArrayList<Image>(); // 建立新的圖片列表物件

        ImageHelper ih = new ImageHelper(this); // 產生與資料庫溝通的圖片物件
        Image images[] = ih.getAll(); // 取得所有圖片資料
        ih.close(); // 關閉資料庫連結

        if (images != null) // 如果有圖片資料
            for (int i=0; i<images.length; i++)
                if (images[i].getName().indexOf("first") > -1) // 如果是原始圖片檔名
                    imageList.add(images[i]); // 設定到圖片列表上

        return imageList;
    }

    private class ImageAdapter extends BaseAdapter { // 實作圖片 Adapter

        private LayoutInflater li;
        private List<Image> imageList;

        public ImageAdapter(Context context, List<Image> imageList) {
            this.li = LayoutInflater.from(context);
            this.imageList = imageList;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return imageList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return imageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null)
                convertView = li.inflate(R.layout.listview_image, null); // 建立新的列表物件

            Image image = imageList.get(position); // 取得圖片物件

            BitmapFactory.Options options = new BitmapFactory.Options(); // 防止OOM(Out of Memory 所設定的選項物件
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inSampleSize = 4;

            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(CameraView.SAVE_PATH + "/" + image.getName()), null, options); // 讀取原始圖檔

                Matrix matrix = new Matrix(); // 圖片旋轉 90 度所設定
                matrix.setRotate(90.0f);

                ImageView iv = ((ImageView)convertView.findViewById(R.id.image)); // 取得顯示圖片物件
                iv.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true)); // 重新建立翻轉 90 度的圖片並設定到顯示圖片的物件上
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String name = image.getName(); // 取得檔名
            TextView tv = ((TextView)convertView.findViewById(R.id.time)); // 取得顯示檔名物件
            tv.setText(ImageHelper.SDF.format(new Date(Long.parseLong(name.substring(name.indexOf("-") + 1, name.indexOf("-", name.indexOf("-") + 1)))))); // 將圖片檔名處理後設定在顯示檔名的物件上

            return convertView;
        }

    }

}