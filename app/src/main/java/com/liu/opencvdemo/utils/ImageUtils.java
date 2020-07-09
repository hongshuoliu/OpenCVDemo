package com.liu.opencvdemo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能描述：图片保存等基本操作
 *
 * @author liuhongshuo
 * @date 2020-07-06
 */
public class ImageUtils {
    private static String TAG = "ImageUtils";

    public static File getSaveFilePath() {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            Log.i(TAG, "SD Card is not suitable...");
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String name = df.format(new Date(System.currentTimeMillis())) + ".jpg";
        File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "myOcrImages");
        filedir.mkdirs();
        String fileName = filedir.getAbsolutePath() + File.separator + name;
        File imageFile = new File(fileName);
        return imageFile;
    }

    public static String getRealPath(Uri uri, Context appContext) {
        String filePath = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {//4.4及以上
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = appContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
                    sel, new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        } else {//4.4以下，即4.4以上获取路径的方法
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = appContext.getContentResolver().query(uri, projection, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(column_index);
        }
        Log.i(TAG, "selected image path : " + filePath);
        return filePath;
    }

    public static void saveImage(Mat image) {
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "mybook");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String name = String.valueOf(System.currentTimeMillis()) + "_book.jpg";
        File tempFile = new File(fileDir.getAbsoluteFile() + File.separator, name);
        Imgcodecs.imwrite(tempFile.getAbsolutePath(), image);
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    public static Uri createImageUri(Context context) {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * assets下文件复制到指定目录
     *
     * @param context
     * @param assetsPath
     * @param desPath
     * @throws IOException
     */
    public static void doCopy(Context context, String assetsPath, String desPath) throws IOException {
        String[] srcFiles = context.getAssets().list(assetsPath);//for directory
        for (String srcFileName : srcFiles) {
            String outFileName = desPath + File.separator + srcFileName;
            String inFileName = assetsPath + File.separator + srcFileName;
            if (assetsPath.equals("")) {// for first time
                inFileName = srcFileName;
            }
            Log.e(TAG, "========= desPath: " + desPath);
            Log.e(TAG, "========= assets: " + assetsPath + "  filename: " + srcFileName + " infile: " + inFileName + " outFile: " + outFileName);
            try {
                InputStream inputStream = context.getAssets().open(inFileName);
                copyAndClose(inputStream, new FileOutputStream(outFileName));
            } catch (IOException e) {//if directory fails exception
                e.printStackTrace();
                new File(outFileName).mkdir();
                doCopy(context, inFileName, outFileName);
            }

        }
    }

    private static void closeQuietly(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
            ;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void copyAndClose(InputStream is, OutputStream out) throws IOException {
        copy(is, out);
        closeQuietly(is);
        closeQuietly(out);
    }

    private static void copy(InputStream is, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = is.read(buffer))) {
            out.write(buffer, 0, n);
        }
    }

}
