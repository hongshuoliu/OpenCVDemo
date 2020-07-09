package com.liu.opencvdemo.utils;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.SIFT;
import org.opencv.xfeatures2d.SURF;

import java.util.ArrayList;

/**
 * 功能描述：
 *
 * @author liuhongshuo
 * @date 2020-07-06
 */
public class ImageFeatureUtils {

    private void extractFeatureImage(String imagePath, ImageView imageView, int section) {
        Mat src = Imgcodecs.imread(imagePath);
        if (src.empty()) {
            return;
        }
        Mat dst = new Mat();
        if (section == 0) {
            harrisCornerDemo(src, dst);
        } else if (section == 1) {
            shiTomasicornerDemo(src, dst);
        } else if (section == 2) {
            surfDemo(src, dst, imagePath);
        } else if (section == 3) {
            siftDemo(src, dst, imagePath);
        } else if (section == 4) {
            detectorDemo(src, dst, 1);
        } else if (section == 5) {
            descriptorDemo(src, dst, imagePath);
        } else if (section == 6) {
            findKnownObject(src, dst, imagePath);
        }

        // 转换为Bitmap，显示
        Bitmap bm = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);

        // show
        imageView.setImageBitmap(bm);

        // release memory
        src.release();
        dst.release();
        result.release();
    }

    /**
     * 基于已知对象在图像中对象检测
     *
     * @param src
     * @param dst
     * @param path
     */
    private void findKnownObject(Mat src, Mat dst, String path) {
        String boxFile = path.replaceAll("box_in_scene", "box");
        Mat boxImage = Imgcodecs.imread(boxFile, Imgcodecs.IMREAD_GRAYSCALE);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);


        SURF surf_detector = SURF.create(400, 4, 3, false, false);
        MatOfKeyPoint keyPoints_box = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints_scene = new MatOfKeyPoint();

        // 特征检测-关键点
        surf_detector.detect(boxImage, keyPoints_box);
        surf_detector.detect(gray, keyPoints_scene);

        // 获取描述子
        Mat descriptor_box = new Mat();
        Mat descriptor_scene = new Mat();
        surf_detector.compute(boxImage, keyPoints_box, descriptor_box);
        surf_detector.compute(gray, keyPoints_scene, descriptor_scene);

        // 匹配
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        descriptorMatcher.match(descriptor_box, descriptor_scene, matches);

        // find min max distance
        DMatch[] dm_arrays = matches.toArray();
        double max_dist = 0;
        double min_dist = 100;
        for (int i = 0; i < descriptor_box.rows(); i++) {
            double dist = dm_arrays[i].distance;
            max_dist = Math.max(dist, max_dist);
            min_dist = Math.min(dist, min_dist);
        }
        Log.i("Find Known Object", "max distance : " + max_dist);
        Log.i("Find Known Object", "min distance : " + min_dist);

        ArrayList<DMatch> goodMatches = new ArrayList<DMatch>();
        double t = 3.0 * min_dist;
        for (int i = 0; i < descriptor_box.rows(); i++) {
            if (dm_arrays[i].distance <= t) {
                goodMatches.add(dm_arrays[i]);
            }
        }
        Features2d.drawMatches(boxImage, keyPoints_box, gray, keyPoints_scene, new MatOfDMatch(goodMatches.toArray(new DMatch[0])),
                dst, Scalar.all(-1), Scalar.all(-1), new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);

        // 得到匹配程度高的关键点对
        Point[] boxes = new Point[goodMatches.size()];
        Point[] scenes = new Point[goodMatches.size()];
        KeyPoint[] kp_boxes = keyPoints_box.toArray();
        KeyPoint[] kp_scenes = keyPoints_scene.toArray();
        for (int i = 0; i < goodMatches.size(); i++) {
            boxes[i] = (kp_boxes[goodMatches.get(i).queryIdx].pt);
            scenes[i] = (kp_scenes[goodMatches.get(i).trainIdx].pt);
        }

        // 寻找位置
        Mat H = Calib3d.findHomography(new MatOfPoint2f(boxes), new MatOfPoint2f(scenes), Calib3d.RANSAC, 3);
        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);
        obj_corners.put(0, 0, new double[]{0, 0});
        obj_corners.put(1, 0, new double[]{boxImage.cols(), 0});
        obj_corners.put(2, 0, new double[]{boxImage.cols(), boxImage.rows()});
        obj_corners.put(3, 0, new double[]{0, boxImage.rows()});
        Core.perspectiveTransform(obj_corners, scene_corners, H);

        // 绘制直线，矩形外接框
        Imgproc.line(dst, new Point(scene_corners.get(0, 0)[0] + boxImage.cols(), scene_corners.get(0, 0)[1]),
                new Point(scene_corners.get(1, 0)[0] + boxImage.cols(), scene_corners.get(1, 0)[1]),
                new Scalar(0, 255, 0), 4);

        Imgproc.line(dst, new Point(scene_corners.get(1, 0)[0] + boxImage.cols(), scene_corners.get(1, 0)[1]),
                new Point(scene_corners.get(2, 0)[0] + boxImage.cols(), scene_corners.get(2, 0)[1]),
                new Scalar(0, 255, 0), 4);

        Imgproc.line(dst, new Point(scene_corners.get(2, 0)[0] + boxImage.cols(), scene_corners.get(2, 0)[1]),
                new Point(scene_corners.get(3, 0)[0] + boxImage.cols(), scene_corners.get(3, 0)[1]),
                new Scalar(0, 255, 0), 4);

        Imgproc.line(dst, new Point(scene_corners.get(3, 0)[0] + boxImage.cols(), scene_corners.get(3, 0)[1]),
                new Point(scene_corners.get(0, 0)[0] + boxImage.cols(), scene_corners.get(0, 0)[1]),
                new Scalar(0, 255, 0), 4);


        // 释放内存
        keyPoints_box.release();
        keyPoints_scene.release();

        descriptor_box.release();
        descriptor_scene.release();
        matches.release();
    }

    /**
     * AKAZE检测器
     *
     * @param src
     * @param dst
     * @param path
     */
    private void descriptorDemo(Mat src, Mat dst, String path) {
        String boxFile = path.replaceAll("box_in_scene", "box");
        Mat boxImage = Imgcodecs.imread(boxFile);
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.AKAZE);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);

        // 关键点检测
        MatOfKeyPoint keyPoints_box = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints_scene = new MatOfKeyPoint();
        detector.detect(boxImage, keyPoints_box);
        detector.detect(src, keyPoints_scene);

        // 描述子生成
        Mat descriptor_box = new Mat();
        Mat descriptor_scene = new Mat();
        descriptorExtractor.compute(boxImage, keyPoints_box, descriptor_box);
        descriptorExtractor.compute(src, keyPoints_scene, descriptor_scene);

        // 特征匹配
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        descriptorMatcher.match(descriptor_box, descriptor_scene, matches);
        Features2d.drawMatches(boxImage, keyPoints_box, src, keyPoints_scene, matches, dst);

        // 释放内存
        keyPoints_box.release();
        keyPoints_scene.release();

        descriptor_box.release();
        descriptor_scene.release();
        matches.release();
    }

    private void detectorDemo(Mat src, Mat dst, int type) {
        FeatureDetector detector = null;
        if (type == 1) {
            detector = FeatureDetector.create(FeatureDetector.ORB);
        } else if (type == 2) {
            detector = FeatureDetector.create(FeatureDetector.BRISK);
        } else if (type == 3) {
            detector = FeatureDetector.create(FeatureDetector.FAST);
        } else if (type == 4) {
            detector = FeatureDetector.create(FeatureDetector.AKAZE);
        } else {
            detector = FeatureDetector.create(FeatureDetector.HARRIS);
        }
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        detector.detect(src, keyPoints);
        Features2d.drawKeypoints(src, keyPoints, dst);
    }

    /**
     * SIFT 特征检测
     *
     * @param src
     * @param dst
     * @param path
     */
    private void siftDemo(Mat src, Mat dst, String path) {
        String boxFile = path.replaceAll("box_in_scene", "box");
        Mat boxImage = Imgcodecs.imread(boxFile);

        SIFT sift_detector = SIFT.create();
        MatOfKeyPoint keyPoints_box = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints_scene = new MatOfKeyPoint();

        // 特征检测-关键点
        sift_detector.detect(boxImage, keyPoints_box);
        sift_detector.detect(src, keyPoints_scene);

        // 获取描述子
        Mat descriptor_txt = new Mat();
        Mat descriptor_scene = new Mat();
        sift_detector.compute(boxImage, keyPoints_box, descriptor_txt);
        sift_detector.compute(src, keyPoints_scene, descriptor_scene);

        // 匹配
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
        descriptorMatcher.match(descriptor_txt, descriptor_scene, matches);
        Features2d.drawMatches(boxImage, keyPoints_box, src, keyPoints_scene, matches, dst);

        // 释放内存
        keyPoints_box.release();
        keyPoints_scene.release();

        descriptor_txt.release();
        descriptor_scene.release();
        matches.release();
    }

    /**
     * SURF 特征检测
     *
     * @param src
     * @param dst
     * @param path
     */
    private void surfDemo(Mat src, Mat dst, String path) {
        String textFile = path.replaceAll("box_in_scene", "box");
        Mat textImage = Imgcodecs.imread(textFile);

        SURF surf_detector = SURF.create(100, 4, 3, false, false);
        MatOfKeyPoint keyPoints_txt = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints_scene = new MatOfKeyPoint();

        // 特征检测-关键点
        surf_detector.detect(textImage, keyPoints_txt);
        surf_detector.detect(src, keyPoints_scene);

        // 获取描述子
        Mat descriptor_txt = new Mat();
        Mat descriptor_scene = new Mat();
        surf_detector.compute(textImage, keyPoints_txt, descriptor_txt);
        surf_detector.compute(src, keyPoints_scene, descriptor_scene);

        // 匹配
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
        descriptorMatcher.match(descriptor_txt, descriptor_scene, matches);
        Features2d.drawMatches(textImage, keyPoints_txt, src, keyPoints_scene, matches, dst);

        // 释放内存
        keyPoints_txt.release();
        keyPoints_scene.release();

        descriptor_txt.release();
        descriptor_scene.release();
        matches.release();
    }

    /**
     * shi-Tomasi角点检测
     *
     * @param src
     * @param dst
     */
    private void shiTomasicornerDemo(Mat src, Mat dst) {
        // 变量定义
        double k = 0.04;
        int blockSize = 3;
        double qualityLevel = 0.01;
        boolean useHarrisCorner = false;

        // 角点检测
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(gray, corners, 100, qualityLevel, 10, new Mat(), blockSize, useHarrisCorner, k);

        // 绘制角点
        dst.create(src.size(), src.type());
        src.copyTo(dst);
        Point[] points = corners.toArray();
        for (int i = 0; i < points.length; i++) {
            Imgproc.circle(dst, points[i], 5, new Scalar(0, 0, 255), 2, 8, 0);
        }
        gray.release();
    }

    /**
     * Harris 角点检测
     *
     * @param src
     * @param dst
     */
    private void harrisCornerDemo(Mat src, Mat dst) {
        // 定义阈值T
        int threshold = 100;
        Mat gray = new Mat();
        Mat response = new Mat();
        Mat response_norm = new Mat();

        // 角点检测
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cornerHarris(gray, response, 2, 3, 0.04);
        Core.normalize(response, response_norm, 0, 255, Core.NORM_MINMAX, CvType.CV_32F);

        // 绘制角点
        dst.create(src.size(), src.type());
        src.copyTo(dst);
        float[] data = new float[1];
        for (int j = 0; j < response_norm.rows(); j++) {
            for (int i = 0; i < response_norm.cols(); i++) {
                response_norm.get(j, i, data);
                if ((int) data[0] > 100) {
                    Imgproc.circle(dst, new Point(i, j), 5, new Scalar(0, 0, 255), 2, 8, 0);
                    Log.i("Harris Corner", "find corner point...");
                }
            }
        }
        gray.release();
        response.release();
    }

}
