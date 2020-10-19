package com.opencv.detect.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 功能描述：
 *
 * @author liuhongshuo
 * @date 2020-06-24
 */
public class ToastUtils {

    public static void show(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }

}
