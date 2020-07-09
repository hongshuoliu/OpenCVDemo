package com.liu.opencvdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liu.opencvdemo.cameracalibration.CameraCalibrationActivity;
import com.liu.opencvdemo.colorblobdetect.ColorBlobDetectionActivity;
import com.liu.opencvdemo.image.ImageManipulationsActivity;
import com.liu.opencvdemo.puzzle15.Puzzle15Activity;
import com.liu.opencvdemo.tutorial.Tutorial1Activity;
import com.liu.opencvdemo.tutorial.Tutorial2Activity;
import com.liu.opencvdemo.tutorial.Tutorial3Activity;

import org.opencv.android.OpenCVLoader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity";
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_STORAGE = 102;

    private Button mBtnImage;
    private Button mBtnTutorial;
    private Button mBtnTutorial2;
    private Button mBtnTutorial3;
    private Button mBtnPuzzle;
    private Button mBtnColorDetection;
    private Button mBtnCameracalibration;

    private Button mBtnCustomCameraVer;
    private Button mBtnCustomCameraHon;
    private Button mBtnImageCus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mBtnImage = (Button) findViewById(R.id.btn_image);
        mBtnImage.setOnClickListener(this);

        mBtnTutorial = (Button) findViewById(R.id.btn_tutorial);
        mBtnTutorial.setOnClickListener(this);

        mBtnTutorial2 = (Button) findViewById(R.id.btn_tutorial2);
        mBtnTutorial2.setOnClickListener(this);
        mBtnTutorial3 = (Button) findViewById(R.id.btn_tutorial3);
        mBtnTutorial3.setOnClickListener(this);


        mBtnPuzzle = (Button) findViewById(R.id.btn_puzzle);
        mBtnPuzzle.setOnClickListener(this);
        mBtnColorDetection = (Button) findViewById(R.id.btn_color_detection);
        mBtnColorDetection.setOnClickListener(this);
        mBtnCameracalibration = (Button) findViewById(R.id.btn_cameracalibration);
        mBtnCameracalibration.setOnClickListener(this);

        mBtnCustomCameraVer = (Button) findViewById(R.id.btn_custom_camera_ver);
        mBtnCustomCameraVer.setOnClickListener(this);
        mBtnCustomCameraHon = (Button) findViewById(R.id.btn_custom_camera_hon);
        mBtnCustomCameraHon.setOnClickListener(this);

        mBtnImageCus = (Button) findViewById(R.id.btn_image_cus);
        mBtnImageCus.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {

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

    private void iniLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(TAG, "OpenCV Libraries loaded...");
        } else {
            Toast.makeText(this.getApplicationContext(), "WARNING: Could not load OpenCV Libraries!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_image:
                startActivity(ImageManipulationsActivity.class);
                break;
            case R.id.btn_tutorial:
                startActivity(Tutorial1Activity.class);
                break;
            case R.id.btn_tutorial2:
                startActivity(Tutorial2Activity.class);
                break;
            case R.id.btn_tutorial3:
                startActivity(Tutorial3Activity.class);
                break;
            case R.id.btn_puzzle:
                startActivity(Puzzle15Activity.class);
                break;
            case R.id.btn_cameracalibration:
                startActivity(CameraCalibrationActivity.class);
                break;
            case R.id.btn_color_detection:
                startActivity(ColorBlobDetectionActivity.class);
                break;
            case R.id.btn_custom_camera_ver:
                startActivity(CameraVerActivity.class);
                break;
            case R.id.btn_custom_camera_hon:
                startActivity(CameraHonActivity.class);
                break;
            case R.id.btn_image_cus:
                startActivity(ImageActivity.class);
                break;
            default:
                break;
        }
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
    }
}
