package com.ai.face.search

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ai.face.FaceApplication
import com.ai.face.base.utils.DeviceFingerprint
import com.ai.face.faceSearch.search.FaceSearchImagesManger
import com.ai.facesearch.demo.R
import com.ai.facesearch.demo.databinding.ActivityFaceSearchNaviBinding
import com.airbnb.lottie.LottieAnimationView
import com.lzf.easyfloat.EasyFloat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 演示导航Navi，主要界面App
 *
 *
 */
class SearchNaviActivity : AppCompatActivity(), PermissionCallbacks {

    private lateinit var binding: ActivityFaceSearchNaviBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceSearchNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNeededPermission()

        binding.faceSearch1n.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity, FaceSearch1NActivity::class.java)
            )
        }

        binding.faceSearchMn.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity, FaceSearchMNActivity::class.java)
            )
        }

        //验证复制图片
        binding.copyFaceImages.setOnClickListener {
            binding.copyFaceImages.isClickable = false
            Toast.makeText(baseContext, "复制处理中...", Toast.LENGTH_LONG).show()
            showAppFloat(baseContext)

            Toast.makeText(baseContext, "复制处理中...", Toast.LENGTH_LONG).show()

            CoroutineScope(Dispatchers.Main).launch {
                copyManyTestFaceImages(application)
                EasyFloat.hide("speed")
                Toast.makeText(baseContext, "已经复制导入验证图片", Toast.LENGTH_SHORT).show()
            }
        }

        //切换摄像头
        binding.changeCamera.setOnClickListener {
            val sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE)

            if (sharedPref.getInt("cameraFlag", 0) == 1) {
                sharedPref.edit().putInt("cameraFlag", 0).apply()
                Toast.makeText(
                    baseContext,
                    "已切换前置摄像头",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sharedPref.edit().putInt("cameraFlag", 1).apply()
                Toast.makeText(
                    baseContext,
                    "已切换后置/外接摄像头",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.editFaceImage.setOnClickListener {
            startActivity(
                Intent(baseContext, FaceImageEditActivity::class.java).putExtra(
                    "isAdd",
                    false
                )
            )
        }


        binding.addFaceImage.setOnClickListener {
            startActivity(
                Intent(baseContext, FaceImageEditActivity::class.java).putExtra(
                    "isAdd",
                    true
                )
            )
        }


//        binding.deviceInfo.text="设备指纹:"+ DeviceFingerprint.getDeviceFingerprint()

    }

    /**
     * companion object 辅助验证
     *
     */
    companion object {
        fun showAppFloat(context: Context) {
            if (EasyFloat.getFloatView("speed")?.isShown == true) return
            EasyFloat.with(context)
                .setTag("speed")
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(false)
                .setLayout(R.layout.float_loading) {
                    val entry: LottieAnimationView = it.findViewById(R.id.entry)
                    entry.setAnimation(R.raw.loading2)
                    entry.loop(true)
                    entry.playAnimation()
                }
                .show()
        }

        private fun getBitmapFromAsset(assetManager: AssetManager, strName: String): Bitmap? {
            val istr: InputStream
            var bitmap: Bitmap?
            try {
                istr = assetManager.open(strName)
                bitmap = BitmapFactory.decodeStream(istr)
            } catch (e: IOException) {
                return null
            }
            return bitmap
        }


        /**
         * 拷贝人脸数据,CopyFileUtilsOnlyTest 仅仅是辅助调试，后期会去除
         *
         */
        suspend fun copyManyTestFaceImages(context: Application) = withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val subFaceFiles = context.assets.list("")
            if (subFaceFiles != null) {
                for (index in subFaceFiles.indices) {
                    FaceSearchImagesManger.c.getInstance(context)?.insertOrUpdateFaceImage(
                        getBitmapFromAsset(
                            assetManager,
                            subFaceFiles[index]
                        ),
                        FaceApplication.CACHE_SEARCH_FACE_DIR + File.separatorChar + subFaceFiles[index]
                    )
                }
            }
        }

    }








    /**
     * 统一全局的拦截权限请求，给提示
     *
     */
    private fun checkNeededPermission() {
        val perms = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
        } else {
            EasyPermissions.requestPermissions(this, "请授权相机使用权限！", 11, *perms)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    /**
     * 当用户点击了不再提醒的时候的处理方式
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}

}