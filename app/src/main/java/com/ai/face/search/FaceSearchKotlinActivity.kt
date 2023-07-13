package com.ai.face.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ai.face.FaceApplication.Companion.STORAGE_FACE_DIR
import com.ai.facesearch.demo.databinding.ActivityFaceSearchBinding
import com.ai.facesearch.demo.R
import com.ai.facesearch.search.FaceProcessBuilder
import com.ai.facesearch.search.FaceSearchEngine
import com.ai.facesearch.search.ProcessCallBack
import com.ai.facesearch.search.ProcessTipsCode.*
import com.ai.facesearch.view.CameraXAnalyzeFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.File

/**
 * 1：N 人脸搜索识别接入演示程序，千张人脸毫秒级别
 *
 */
class FaceSearchKotlinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tips.setOnClickListener {
            startActivity(Intent(baseContext, FaceImageEditActivity::class.java))
        }

        val sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE)


        // 1. Camera 的初始化
        val cameraLens=sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 0))
        val cameraXFragment = CameraXAnalyzeFragment.newInstance(cameraLens)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_camerax, cameraXFragment)
            .commit()
        cameraXFragment.setOnAnalyzerListener { imageProxy ->
            //可以加个红外检测之类的，有人靠近再启动检索服务
            if (!this@FaceSearchKotlinActivity.isDestroyed && !this@FaceSearchKotlinActivity.isFinishing) {
                FaceSearchEngine.Companion().instance.runSearch(imageProxy, 0)
            }
        }


        // 2.各种参数的初始化设置
        val faceProcessBuilder = FaceProcessBuilder
            .Builder(applicationContext)
            .setLifecycleOwner(this)
            .setThreshold(0.81f)         //识别成功阈值设置，范围仅限 0.7-0.9！建议0.8+
            .setLicenceKey("hjhk2323")   //申请的License
            .setFaceLibFolder(STORAGE_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
            .setProcessCallBack(object : ProcessCallBack() {
                override fun onMostSimilar(similar: String?) {
//                    VoicePlayer.getInstance().play(R.raw.success)
                    binding.faceCoverView.setTipText(similar)
                    Glide.with(baseContext)
                        .load(STORAGE_FACE_DIR + File.separatorChar + similar)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transform(RoundedCorners(11))
                        .into(binding.image)
                }

                override fun onProcessTips(code: Int) {
                    showPrecessTips(code)
                }

                override fun onLog(log: String?) {
                    binding.tips.text = log
                }
            })
            .create()


        //3.初始化引擎，是个耗时耗资源操作
        FaceSearchEngine.Companion().instance.initSearchParams(faceProcessBuilder)

    }

    /**
     *  显示提示
     *
     * @param code
     */
    private fun showPrecessTips(code: Int) {
        binding.image.setImageResource(R.drawable.ic_launcher_foreground)

        when (code) {
            MASK_DETECTION -> binding.faceCoverView.setTipText("请摘下口罩")
            NO_LIVE_FACE -> binding.faceCoverView.setTipText("未检测到人脸")
            EMGINE_INITING -> binding.faceCoverView.setTipText("初始化中")
            FACE_DIR_EMPTY -> binding.faceCoverView.setTipText("人脸库为空")
            NO_MATCHED -> {
                binding.faceCoverView.setTipText("没有匹配项")
//                VoicePlayer.getInstance().play(R.raw.fail)
            }

            SEARCHING -> {
                binding.faceCoverView.setTipText("")
            }

            else -> binding.faceCoverView.setTipText("提示码：$code")
        }
    }


    /**
     * 销毁，停止
     *
     */
    override fun onDestroy() {
        super.onDestroy()
        FaceSearchEngine.Companion().instance.stopSearchProcess()
    }


}