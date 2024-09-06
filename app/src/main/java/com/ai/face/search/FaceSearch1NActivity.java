package com.ai.face.search;

import static com.ai.face.FaceApplication.CACHE_SEARCH_FACE_DIR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.*;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;

import com.ai.face.base.view.CameraXFragment;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.FaceSearchResult;
import com.ai.face.utils.VoicePlayer;
import com.ai.facesearch.demo.R;
import com.ai.facesearch.demo.databinding.ActivityFaceSearchBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 应多位用户要求，默认使用java 版本演示怎么快速接入SDK。
 *
 *
 */
public class FaceSearch1NActivity extends AppCompatActivity {
    private ActivityFaceSearchBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tips.setOnClickListener((View v) -> {
            startActivity(new Intent(this, FaceImageMangerActivity.class));
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);

        // 1. Camera 的初始化。第一个参数0/1 指定前后摄像头；
        int cameraLens = sharedPref.getInt("cameraFlag", 0);

        // 第二个参数linearZoom [0.01f,1.0f] 指定焦距，默认0.1
        CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLens,0.2f);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_camerax, cameraXFragment)
                .commit();

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            //可以加个红外检测之类的，有人靠近再启动检索服务，不然机器老化快
            if (!isDestroyed() && !isFinishing()) {
                //第二个参数传0表示不裁剪
                //大于0 表示裁剪距中的正方形区域范围为人脸检测区，参数为正方形区域距离屏幕边缘的值
                FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
            }
        });


        // 2.各种参数的初始化设置
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(getApplication())
                .setLifecycleOwner(this)
                .setThreshold(0.85f)              //阈值设置，范围限 [0.8 , 0.95] 识别可信度
                .setNeedNirLiveness(false)        //是否需要红外活体能力，只有1:N VIP 有
                .setNeedRGBLiveness(false)        //是否需要普通RGB活体检测能力，只有1:N VIP 有
                .setNeedMultiValidate(false)      //是否需要筛选结果防止误识别，需要硬件CPU配置高，Android 8+
                //增删改人脸 参考@FaceImageEditActivity 中的方式，需要使用SDK 中的API 进行操作不能直接插入图片
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个人脸图片库的目录
                .setImageFlipped(cameraLens == CameraSelector.LENS_FACING_FRONT) //手机的前置摄像头imageProxy 拿到的图可能左右翻转
                .setProcessCallBack(new SearchProcessCallBack() {
                    //人脸识别检索回调，最匹配的那个
                    @Override
                    public void onMostSimilar(String similar,float value, Bitmap realTimeImg) {
                        //根据你的业务逻辑，各种提示 & 触发成功后面的操作
                        binding.searchTips.setText(similar);
                        VoicePlayer.getInstance().addPayList(R.raw.success);
                        Glide.with(getBaseContext())
                                .load(CACHE_SEARCH_FACE_DIR + File.separatorChar + similar)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .transform(new RoundedCorners(11))
                                .into(binding.image);
                    }

                    /**
                     * 大于设置阈值的搜索结果，长的像的都会搜索出来
                     * @param arrayMap 大于设置阈值对应的人脸库中的图片ID和得分
                     * @param bitmap 摄像头的画面帧数据
                     */
                    @Override
                    public void onSimilarMap(ArrayMap<String, Float> arrayMap, Bitmap bitmap) {
                        //大于setThreshold 的都在这 arrayMap里面，Key 是图片ID，Value 是相似度的值

                    }

                    /**
                     * onSimilarMap 的拓展，多了rect 和faceBitmap
                     *
                     * @param faceSearchResults 搜索结果
                     *         Rect rect;                人脸的坐标
                     *         String faceName;          搜索匹配到的人脸的图片名字
                     *         float faceScore;          搜索匹配到的人脸相似度值
                     *         final Bitmap faceBitmap   检测到的人脸（坐标rect圈出来的图像Bitmap）
                     *
                     * @param bitmap 摄像头采集的当前帧图像
                     */
                    @Override
                    public void onFaceMatched(List<FaceSearchResult> faceSearchResults,Bitmap bitmap) {
                        binding.graphicOverlay.drawRect(faceSearchResults, cameraXFragment);
                    }

                    @Override
                    public void onProcessTips(int i) {
                        showPrecessTips(i);
                    }


                    @Override
                    public void onLog(String log) {
                        binding.tips.setText(log);
                    }

                }).create();


        //3.初始化引擎
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);


        // 4.简单的单张图片帧搜索，不用摄像头的形式
        // 需要注释掉这行代码 FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //这行代码演示 传入单张图片进行人脸搜索
//                FaceSearchEngine.Companion.getInstance().runSearch("your bitmap here");
            }
        },3000);

    }


    /**
     * 显示提示
     *
     * @param code
     */
    private void showPrecessTips(int code) {
        binding.image.setImageResource(R.mipmap.ic_launcher);
        switch (code) {
            default:
                binding.searchTips.setText("提示码："+code);
                break;

            case THRESHOLD_ERROR :
                binding.searchTips.setText("识别阈值Threshold范围为0.8-0.95");
                break;

            //疫情已经过去了，默认没有这个功能
            case MASK_DETECTION:
                binding.searchTips.setText("请摘下口罩");
                break;

            case NO_LIVE_FACE:
                binding.searchTips.setText("未检测到人脸");
                binding.tips.setText("");
                break;

            case EMGINE_INITING:
                binding.searchTips.setText("初始化中");
                break;

            case FACE_DIR_EMPTY:
                //增删改人脸 参考@FaceImageEditActivity 中的方式，需要使用SDK 中的API 进行操作不能直接插入图片
                binding.searchTips.setText("人脸库为空");
                binding.tips.setText("去管理人脸库");
                break;

            case NO_MATCHED:
                //本次摄像头预览帧无匹配而已，会快速取下一帧进行分析检索
                binding.searchTips.setText("无人脸匹配");

                break;
        }
    }




}