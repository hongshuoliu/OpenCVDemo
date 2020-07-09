package com.liu.opencvdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.liu.opencvdemo.adapter.ImageAdapter;
import com.liu.opencvdemo.image.ImageFileInterface;
import com.liu.opencvdemo.utils.ImageConvolutionUtils;
import com.liu.opencvdemo.utils.ImageUtils;
import com.liu.opencvdemo.utils.ToastUtils;

import java.io.File;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ImageActivity extends BaseActivity implements ImageFileInterface {
    private static final String TAG = "ImageActivity";

    private ImageView mImgSrc;
    private ImageView mImgDst;

    private RecyclerView mRecyclerView;
    private ImageAdapter imageAdapter;

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
        mImgDst = (ImageView) findViewById(R.id.img_dst);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        imageAdapter = new ImageAdapter(this);
        mRecyclerView.setAdapter(imageAdapter);
        imageAdapter.setOnItemClickListener(new ImageAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ImageConvolutionUtils.blurImage(fileUri.getPath(), mImgDst, position);
            }
        });
    }

    @Override
    public void onImageFileChanged(String filePath) {

    }


    @Override
    protected void loadOpenCVSuccess() {

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
