package com.liu.opencvdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.liu.opencvdemo.widget.MyCvCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class CameraBaseActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    protected MyCvCameraView mOpenCvCameraView;

    private int previewWidth;
    private int previewHeight;

    private boolean mIsJavaCamera = true;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    if (mOpenCvCameraView != null) {
                        previewWidth = mOpenCvCameraView.getWidth();
                        previewHeight = mOpenCvCameraView.getHeight();
                        Log.e(TAG, "previewWidth:" + previewWidth + "  previewHeight:" + previewHeight);

//                        mOpenCvCameraView.setMaxFrameSize(previewWidth, previewHeight);previewHeight;
                        mOpenCvCameraView.setShowFullPreview(false);
                        mOpenCvCameraView.enableView();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Log.i(TAG, "parent------onCameraFrame");

        Mat frame = inputFrame.rgba();

//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Log.i("CVCamera", "竖屏显示...");
//
//            if (mOpenCvCameraView.isFrontCamare()) {
//                Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE);
//                Core.flip(frame, frame, 1);
//                return frame;
//            } else {
//                Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
//                return frame;
//            }
//        }

        return frame;
    }

    public void enableTouchListener() {
        if (null != mOpenCvCameraView) {
            mOpenCvCameraView.setOnTouchListener(this);
        }
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }
}

