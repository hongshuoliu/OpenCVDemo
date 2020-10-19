package com.opencv.detect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.opencv.BaseActivity;
import com.opencv.detect.callback.ImageFileInterface;
import com.opencv.detect.utils.DetectionBasedTracker;
import com.opencv.detect.utils.ImageUtils;
import com.opencv.detect.utils.ToastUtils;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageActivity extends BaseActivity implements ImageFileInterface {
    private static final String TAG = "ImageActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private int mDetectorType = NATIVE_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private ImageView mImgSrc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        try {
            File externalDir = this.getExternalFilesDir(null);
            ImageUtils.doCopy(this, "images", externalDir.getPath());
            String path = externalDir.getPath() + File.separator + "lena.png";
            if (new File(path).exists()) {
                fileUri = Uri.parse(path);
            } else {
                ToastUtils.show(this, "复制文件失败，请先选择照片");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
    }

    private void initView() {

        mImgSrc = (ImageView) findViewById(R.id.img_src);
    }

    @Override
    public void onImageFileChanged(String filePath) {

    }


    @Override
    protected void loadOpenCVSuccess() {

        // Load native library after(!) OpenCV initialization
        System.loadLibrary("detection_based_tracker");

        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else {
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            }
            mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

        if (null != fileUri) {
            Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
            mImgSrc.setImageBitmap(bm);


        }
    }

    @Override
    protected void loadOpenCVFail() {
        ToastUtils.show(this, "OpenCV loaded fail");
    }

}
