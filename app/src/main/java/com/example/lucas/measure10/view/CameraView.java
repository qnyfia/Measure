package com.example.lucas.measure10.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.lucas.measure10.activity.ImageActivity;
import com.example.lucas.measure10.db.ImageHelper;
import com.example.lucas.measure10.model.Image;

@SuppressLint("NewApi")
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/Demo";

    private SurfaceHolder sh;
    private Camera camera;
    private ProgressDialog pd;

    @SuppressWarnings("deprecation")
    public CameraView(Context context) {
        super(context);

        sh = getHolder(); // 取得繪圖指標
        sh.addCallback(this);
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @SuppressLint("NewApi")
    @Override
    public void surfaceCreated(SurfaceHolder holder) { // 當建立畫布完成後
        // TODO Auto-generated method stub
        if (camera == null) {
            camera = Camera.open(); // 開啟相機

            camera.setDisplayOrientation(90); // 相機畫面翻轉 90 度
            try {
                camera.setPreviewDisplay(holder); // 設定預覽畫面
                camera.setPreviewCallback(new PreviewCallback() {

                    public void onPreviewFrame(byte[] data, Camera arg1) {
                        CameraView.this.invalidate();
                    }

                });
            } catch (IOException e) {
                camera.release(); // 釋放相機資源
                camera = null;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) { // 當繪布被釋放後
        // TODO Auto-generated method stub
        if (camera != null) {
            camera.stopPreview(); // 停止預覽
            camera.setPreviewCallback(null);

            camera.release(); // 釋放相機資源
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) { // 當畫布大小被改變後
        // TODO Auto-generated method stub
        Camera.Parameters parameters = camera.getParameters(); // 取得相機設定
        camera.setParameters(parameters); // 設定相機設定
        camera.startPreview(); // 開始預覽
    }

    public void cameraTake() { // 拍照
        camera.autoFocus(afc); // 自動對焦
    }

    public Bitmap toBinary(Bitmap bitmap) { // 二值化方法
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap binarymap = null;
        binarymap = bitmap.copy(Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = binarymap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                if (gray <= 95)
                    gray = 0;
                else
                    gray = 255;
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }

    public Bitmap toCanny(Bitmap bitmap) { // Canny 方法
        Mat srcBitmapMat = new Mat();
        Utils.bitmapToMat(bitmap, srcBitmapMat); // 將 Bitmap 轉換成 OpenCV 可處理的 Mat 物件

        Mat bitmapMat = new Mat();
        Imgproc.cvtColor(srcBitmapMat, bitmapMat, Imgproc.COLOR_BGR2GRAY, 1); // RGB轉換成灰階形式

        Imgproc.blur(bitmapMat, bitmapMat, new Size(3.0, 3.0)); // 模糊化

        Imgproc.Canny(bitmapMat, bitmapMat, 100.0, 100.0, 3, true); // Canny

//    	perspectiveCorrection(bitmapMat, srcBitmapMat); // 矯正矩形

        Imgproc.cvtColor(bitmapMat, bitmapMat, Imgproc.COLOR_GRAY2BGRA, 4); // 灰階轉換成RGB形式

        Utils.matToBitmap(bitmapMat, bitmap); // 將 Mat 轉換成 Android 可處理的 Bitmap 物件

        int width = bitmap.getWidth(), height = bitmap.getHeight();
        for (int i=0; i<height; i++) // 黑白反轉迴圈
            for (int j=0; j<width; j++)
                if (bitmap.getPixel(j, i) == Color.WHITE)
                    bitmap.setPixel(j, i, Color.BLACK);
                else
                    bitmap.setPixel(j, i, Color.WHITE);

        return bitmap;
    }

	/* 矯正矩形
	private void perspectiveCorrection(Mat bitmapMat, Mat srcBitmapMat) {
		Mat lines = new Mat();
        Imgproc.HoughLinesP(bitmapMat, lines, 1.0, Math.PI / 180.0, 70, 30.0, 10.0);
        for (int i = 0, colSize = lines.cols(); i < colSize; i++) {
        	double v[] = lines.get(0, i);
            v[0] = 0;
            v[1] = (v[1] - v[3]) / (v[0] - v[2]) * -v[0] + v[1];
            v[2] = (double) srcBitmapMat.cols();
            v[3] = (v[1] - v[3]) / (v[0] - v[2]) * ((double) srcBitmapMat.cols() - v[2]) + v[3];
            lines.put(0, i, v);
        }

        MatOfPoint2f corners = new MatOfPoint2f();
        for (int i = 0, colSize = lines.cols(); i < colSize; i++) {
            for (int j = i+1; j < colSize; j++) {
            	MatOfPoint2f pt = computeIntersect(lines.get(0, i), lines.get(0, j));
                if (pt != null)
                    corners.push_back(pt);
            }
        }

        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(corners, approx, Imgproc.arcLength(corners, true) * 0.02, true);

        if (approx.toList().size() == 4) {
	        Point centerPoint = new Point(0.0, 0.0);
	        List <Point> cornersList = corners.toList();
	        for (int i = 0; i < cornersList.size(); i++) {
	        	centerPoint.x += cornersList.get(i).x;
	        	centerPoint.y += cornersList.get(i).y;
	        }
	        centerPoint.x *= 1.0 / (double) cornersList.size();
	        centerPoint.y *= 1.0 / (double) cornersList.size();

	        MatOfPoint2f center = new MatOfPoint2f(centerPoint);
	        sortCorners(corners, center);

	        Mat quad = Mat.zeros(bitmapMat.rows(), bitmapMat.cols(), bitmapMat.type());

	        MatOfPoint2f quadPoints = new MatOfPoint2f();
	        quadPoints.push_back(new MatOfPoint2f(new Point(0.0, 0.0)));
	        quadPoints.push_back(new MatOfPoint2f(new Point(quad.cols(), 0.0)));
	        quadPoints.push_back(new MatOfPoint2f(new Point(quad.cols(), quad.rows())));
	        quadPoints.push_back(new MatOfPoint2f(new Point(0, quad.rows())));

        	Mat transmtx = Imgproc.getPerspectiveTransform(corners, quadPoints);

	        Imgproc.warpPerspective(bitmapMat, bitmapMat, transmtx, quad.size());
        }
	}

	private MatOfPoint2f computeIntersect(double a[], double b[]) {
	    int x1 = (int) a[0], y1 = (int) a[1], x2 = (int) a[2], y2 = (int) a[3];
	    int x3 = (int) b[0], y3 = (int) b[1], x4 = (int) b[2], y4 = (int) b[3];
	    float d = ((x1-x2) * (y3-y4)) - ((y1-y2) * (x3-x4));
	    if (d > 0.0f)
	    	return new MatOfPoint2f(new Point(
    			(double)((x1*y2 - y1*x2) * (x3-x4) - (x1-x2) * (x3*y4 - y3*x4)) / d,
    			(double)((x1*y2 - y1*x2) * (y3-y4) - (y1-y2) * (x3*y4 - y3*x4)) / d
	    	));
	    else
	        return null;
	}

	private void sortCorners(MatOfPoint2f corners, MatOfPoint2f center) {
		MatOfPoint2f top = new MatOfPoint2f(),
								bot = new MatOfPoint2f();

		List<Point> cornersList = corners.toList();
		Point centerPoint = center.toList().get(0);
	    for (int i = 0, size = cornersList.size(); i < size; i++) {
	    	Point point = cornersList.get(i);
	        if (point.y < centerPoint.y)
	            top.push_back(new MatOfPoint2f(point));
	        else
	            bot.push_back(new MatOfPoint2f(point));
	    }

	    List<Point> topList = top.toList();
	    List<Point> botList = bot.toList();
	    MatOfPoint2f tl = new MatOfPoint2f(topList.get(0).x > topList.get(1).x ?
	    																	topList.get(1) : topList.get(0));
	    MatOfPoint2f tr = new MatOfPoint2f(topList.get(0).x > topList.get(1).x ?
																			topList.get(0) : topList.get(1));
	    MatOfPoint2f bl = new MatOfPoint2f(bot.toList().get(0).x > bot.toList().get(1).x ?
	    																	botList.get(1) : botList.get(0));
	    MatOfPoint2f br = new MatOfPoint2f(bot.toList().get(0).x > bot.toList().get(1).x ?
	    																	botList.get(0) : botList.get(1));

	    corners.release();
	    corners.push_back(tl);
	    corners.push_back(tr);
	    corners.push_back(br);
	    corners.push_back(bl);
	}*/

    AutoFocusCallback afc = new AutoFocusCallback() { // 自動對焦物件

        public void onAutoFocus(boolean success, Camera camera) {
            camera.takePicture(null, null, pc); // 取得圖片
        }

    };

    PictureCallback pc = new PictureCallback() { // 取得圖片物件

        public void onPictureTaken(final byte[] data, final Camera camera) {
            final Context context = getContext();
            final AlertDialog.Builder builder = new AlertDialog.Builder(context); // 建立互動訊息
            builder.setTitle("是否保存照片？");
            builder.setCancelable(false);
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Handler handler = new Handler();

                    handler.post(new Runnable(){

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            pd = ProgressDialog.show(context, null, "影像處理中...", true, false);

                            new Thread() {

                                @SuppressLint("SimpleDateFormat")
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    Bundle bundle = new Bundle();
                                    try {
                                        String time = new SimpleDateFormat("yyyyMMddHHmmss-").format(new Date()) + System.currentTimeMillis();
                                        ImageHelper ih = new ImageHelper(context);

                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inMutable = true;
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                                        Image image = new Image();
                                        image.setName(time + "-" + "1-first.png");
                                        bundle.putString("first", image.getName());
                                        File saveImage= new File(SAVE_PATH);
                                        if (!saveImage.exists())
                                            saveImage.mkdir();
                                        FileOutputStream fop = new FileOutputStream(saveImage.getPath() + "/" + image.getName());
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fop);
                                        fop.close();
                                        ih.set(image);

                                        image = new Image();
                                        image.setName(time + "-" + "2-canny.png");
                                        bundle.putString("canny", image.getName());
                                        fop = new FileOutputStream(saveImage.getPath() + "/" + image.getName());
                                        toCanny(bitmap).compress(Bitmap.CompressFormat.PNG, 100, fop);
                                        fop.close();
                                        ih.set(image);

                                        image = new Image();
                                        image.setName(time + "-" + "3-binary.png");
                                        bundle.putString("binary", image.getName());
                                        fop = new FileOutputStream(saveImage.getPath() + "/" + image.getName());
                                        toBinary(bitmap).compress(Bitmap.CompressFormat.PNG, 100, fop);
                                        fop.close();
                                        ih.set(image);

                                        ih.close();
                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated method stub
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated method stub
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent(context, ImageActivity.class);

                                    intent.putExtras(bundle);

                                    ((Activity)context).startActivityForResult(intent, 0);
                                    pd.dismiss();
                                }

                            }.start();
                        }

                    });
                    dialog.dismiss();
                }

            });

            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    camera.startPreview();
                    dialog.dismiss();
                }

            });

            builder.create().show();
        }

    };

}