package com.liu.opencvdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.liu.opencvdemo.image.ImageFileInterface;
import com.liu.opencvdemo.utils.ImageUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_STORAGE = 102;
    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    //图片文件路径
    protected Uri fileUri;

    protected boolean loadOpenCVSuccess = false;

    /**
     *
     */
    private ImageFileInterface imageFileInterface;

    // 是否是Android 10以上手机
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    loadOpenCVSuccess = true;
                    loadOpenCVSuccess();
                }
                break;
                default: {
                    loadOpenCVSuccess = false;
                    loadOpenCVFail();
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_camare) {
            openCamera();

        } else if (id == R.id.action_photo) {
            pickUpImage();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void requestPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (Build.VERSION.SDK_INT >= 23 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_CAMERA);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void convert2Gray(ImageView iv) {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if (src.empty()) {
            return;
        }
        Mat dst = new Mat();

        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        Bitmap bitmap = grayMat2Bitmap(dst);
        iv.setImageBitmap(bitmap);
        src.release();
        dst.release();
    }

    private Bitmap grayMat2Bitmap(Mat result) {
        Mat image = null;
        if (result.cols() > 1000 || result.rows() > 1000) {
            image = new Mat();
            Imgproc.resize(result, image, new Size(result.cols() / 4, result.rows() / 4));
        } else {
            image = result;
        }
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGBA);
        Utils.matToBitmap(image, bitmap);
        image.release();
        return bitmap;
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // 适配android 10
                photoUri = ImageUtils.createImageUri(this);
            } else {
                photoFile = ImageUtils.getSaveFilePath();

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            fileUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    /**
     * 相册
     */
    private void pickUpImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                File f = new File(ImageUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(f);
                if (null != imageFileInterface) {
                    imageFileInterface.onImageFileChanged(fileUri.getPath());
                }
            }
        }

    }

    public void setImageFileInterface(ImageFileInterface imageFileInterface) {
        this.imageFileInterface = imageFileInterface;
    }

    public void displaySImage(ImageView imageView) {
        if (fileUri == null) {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUri.getPath(), options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if (w > 1000 || h > 1000) {
            while (Math.max(w / inSample, h / inSample) > 1000) {
                inSample *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath(), options);
        imageView.setImageBitmap(bm);
    }


    protected abstract void loadOpenCVSuccess();

    protected abstract void loadOpenCVFail();

}
