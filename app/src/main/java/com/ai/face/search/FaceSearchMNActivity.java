package com.ai.face.search;

import static com.ai.face.FaceApplication.CACHE_SEARCH_FACE_DIR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.EMGINE_INITING;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.FACE_DIR_EMPTY;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.MASK_DETECTION;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_LIVE_FACE;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.NO_MATCHED;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;

import com.ai.face.base.view.CameraXFragment;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.RectLabel;
import com.ai.facesearch.demo.R;
import com.ai.facesearch.demo.databinding.ActivityFaceSearchMnBinding;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 应多位用户要求，默认使用java 版本演示怎么快速接入SDK
 */
public class FaceSearchMNActivity extends AppCompatActivity {
    private ActivityFaceSearchMnBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceSearchMnBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tips.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceImageEditActivity.class));
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);

        // 1. Camera 的初始化
        int cameraLens = sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 0));
        CameraXFragment cameraX = CameraXFragment.newInstance(cameraLens, 0.3f); //参数1，前后摄像头 2是焦距
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_camerax, cameraX)
                .commit();


        cameraX.setOnAnalyzerListener(imageProxy -> {
            //可以加个红外检测之类的，有人靠近再启动检索服务，不然机器老化快
            if (!isDestroyed() && !isFinishing()) {
                //MN 检索，第二个参数为0 就不要裁剪了
                FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
            }
        });


        // 2.各种参数的初始化设置 （M：N 建议阈值放低）
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(this)
                .setLifecycleOwner(this)
                .setThreshold(0.8f)            //识别成功阈值设置，范围仅限 0.75 , 0.95 ！默认0.8
                .setLicenceKey("yourLicense")   //申请的License
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .setSearchType(SearchProcessBuilder.SearchType.N_SEARCH_M) //1:N 搜索
                .setImageFlipped(cameraLens == CameraSelector.LENS_FACING_FRONT) //手机的前置摄像头imageProxy 拿到的图可能左右翻转
                .setProcessCallBack(new SearchProcessCallBack() {

                    //坐标框和对应的 搜索匹配到的图片标签
                    //人脸检测成功后画白框，此时还没有标签字段Label 字段为空
                    //人脸搜索匹配成功后白框变绿框，并标记出对应的Label
                    @Override
                    public void onFaceMatched(List<RectLabel> rectLabels) {
                        binding.graphicOverlay.drawRect(rectLabels, cameraX);

                        if(!rectLabels.isEmpty()) {
                            binding.searchTips.setText("");
                        }
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


        //3.初始化r引擎
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);


        // 4.简单的单张图片搜索，不用摄像头的形式
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
        binding.searchTips.setText("提示码：$code");

        switch (code) {

            case THRESHOLD_ERROR:
                binding.searchTips.setText("识别阈值Threshold范围为0.75-0.95");
                break;

            case MASK_DETECTION:
                binding.searchTips.setText("请摘下口罩"); //默认无
                break;

            case NO_LIVE_FACE:
                binding.searchTips.setText("未检测到人脸");
                break;

            case EMGINE_INITING:
                binding.searchTips.setText("初始化中");
                break;

            case FACE_DIR_EMPTY:
                binding.searchTips.setText("人脸库为空");
                break;

            case NO_MATCHED: {
                binding.searchTips.setText("没有匹配项");
                break;
            }


        }
    }


    /**
     * 销毁，停止
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FaceSearchEngine.Companion.getInstance().stopSearchProcess();
    }


}