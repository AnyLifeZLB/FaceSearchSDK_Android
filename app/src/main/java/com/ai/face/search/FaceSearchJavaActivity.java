package com.ai.face.search;

import static com.ai.face.FaceApplication.STORAGE_FACE_DIR;
import static com.ai.facesearch.search.ProcessTipsCode.EMGINE_INITING;
import static com.ai.facesearch.search.ProcessTipsCode.FACE_DIR_EMPTY;
import static com.ai.facesearch.search.ProcessTipsCode.MASK_DETECTION;
import static com.ai.facesearch.search.ProcessTipsCode.NO_LIVE_FACE;
import static com.ai.facesearch.search.ProcessTipsCode.NO_MATCHED;
import static com.ai.facesearch.search.ProcessTipsCode.SEARCHING;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ai.facesearch.demo.R;
import com.ai.facesearch.search.FaceProcessBuilder;
import com.ai.facesearch.search.FaceSearchEngine;
import com.ai.facesearch.search.ProcessCallBack;
import com.ai.facesearch.view.CameraXAnalyzeFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.io.File;

/**
 * 应网友要求默认使用java 版本演示
 */
public class FaceSearchJavaActivity extends AppCompatActivity {
    private ActivityFaceSearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFaceSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tips.setOnClickListener(v -> {
            startActivity(new Intent(this, FaceImageEditActivity.class));
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);


        // 1. Camera 的初始化
        int cameraLens = sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 0));
        CameraXAnalyzeFragment cameraXFragment = CameraXAnalyzeFragment.newInstance(cameraLens);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_camerax, cameraXFragment)
                .commit();

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            //可以加个红外检测之类的，有人靠近再启动检索服务
            if (!isDestroyed() && !isFinishing()) {
                FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
            }
        });


        // 2.各种参数的初始化设置
        FaceProcessBuilder faceProcessBuilder = new FaceProcessBuilder.Builder(getApplication())
                .setLifecycleOwner(this)
                .setThreshold(0.81f)         //识别成功阈值设置，范围仅限 0.7-0.9！建议0.8+
                .setLicenceKey("hjhk2323")   //申请的License
                .setFaceLibFolder(STORAGE_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .setProcessCallBack(new ProcessCallBack() {
                    @Override
                    public void onMostSimilar(String similar) {
                        VoicePlayer.getInstance().play(R.raw.success);
                        binding.faceCoverView.setTipText(similar);
                        Glide.with(getBaseContext())
                                .load(STORAGE_FACE_DIR + File.separatorChar + similar)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .transform(new RoundedCorners(11))
                                .into(binding.image);
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


        //3.初始化引擎，是个耗时耗资源操作
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);

    }


    /**
     * 显示提示
     *
     * @param code
     */
    private void showPrecessTips(int code) {
        binding.image.setImageResource(R.drawable.ic_launcher_foreground);
        binding.faceCoverView.setTipText("提示码：$code");

        switch (code) {
            case MASK_DETECTION:
                binding.faceCoverView.setTipText("请摘下口罩");
                break;

            case NO_LIVE_FACE:
                binding.faceCoverView.setTipText("未检测到人脸");
                break;

            case EMGINE_INITING:
                binding.faceCoverView.setTipText("初始化中");
                break;

            case FACE_DIR_EMPTY:
                binding.faceCoverView.setTipText("人脸库为空");
                break;

            case NO_MATCHED: {
                binding.faceCoverView.setTipText("没有匹配项");
                VoicePlayer.getInstance().play(R.raw.fail);
                break;
            }

            case SEARCHING: {
                binding.faceCoverView.setTipText("");
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