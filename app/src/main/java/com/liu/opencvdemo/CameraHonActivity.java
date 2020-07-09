package com.liu.opencvdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.liu.opencvdemo.widget.MyCvCameraView;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class CameraHonActivity extends CameraBaseActivity implements CvCameraViewListener2, View.OnClickListener {
    private static final String TAG = "CameraVerActivity";

    private RadioButton mBtnFrontCamera;
    private RadioButton mBtnBackCamera;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_hon);

        mOpenCvCameraView = (MyCvCameraView) findViewById(R.id.cv_camera_id);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mBtnFrontCamera = (RadioButton) findViewById(R.id.btn_frontCamera);
        mBtnFrontCamera.setOnClickListener(this);
        mBtnBackCamera = (RadioButton) findViewById(R.id.btn_backCamera);
        mBtnBackCamera.setOnClickListener(this);
        mBtnBackCamera.setChecked(true);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (view.getId()) {
            case R.id.btn_frontCamera:
                mOpenCvCameraView.setFontCamare();
                break;
            case R.id.btn_backCamera:
                mOpenCvCameraView.setBackCamare();
                break;
            default:
                break;

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
        Log.i(TAG, "child------onCameraFrame");
        super.onCameraFrame(inputFrame);

        Mat frame = inputFrame.rgba();

        Log.i("CVCamera", "横屏显示...");

        if (mOpenCvCameraView.isFrontCamare()) {
            Core.flip(frame, frame, 1);
        }

        return frame;
    }
}
