package com.ai.facesearch.demo.onlytest

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.facesearch.FaceApplication.Companion.STORAGE_FACE_DIR
import com.ai.facesearch.demo.NaviActivity.Companion.copyManyTestFaceImages
import com.ai.facesearch.demo.R
import com.ai.facesearch.search.FaceImagesManger
import com.ai.facesearch.utils.BitmapUtils
import com.ai.facesearch.utils.FaceFileProviderUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.Date
import java.util.Locale

/**
 * 增删改 编辑人脸图片
 * 仅仅是测试验证和演示，不是SDK 接入的一部分
 *
 */
class FaceImageEditActivity : AppCompatActivity() {
    private val faceImageList: MutableList<String> = ArrayList()
    private lateinit var faceImageListAdapter: FaceImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_image_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val gridLayoutManager: LinearLayoutManager = GridLayoutManager(this, 2)
        mRecyclerView.layoutManager = gridLayoutManager
        loadImageList()

        faceImageListAdapter = FaceImageListAdapter(faceImageList)
        mRecyclerView.adapter = faceImageListAdapter
        faceImageListAdapter.setOnItemLongClickListener { _, _, i ->
            AlertDialog.Builder(this@FaceImageEditActivity)
                .setTitle("确定要删除" + File(faceImageList[i]).name)
                .setMessage("删除后对应的人将无法被程序识别")
                .setPositiveButton("确定") { dialog: DialogInterface?, which: Int ->
                    //删除一张照片
                    FaceImagesManger.Companion().getInstance(application)
                        ?.deleteFaceImage(faceImageList[i])
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }
                .show()
            false
        }
        faceImageListAdapter.setEmptyView(R.layout.empty_layout)

        faceImageListAdapter.emptyLayout?.setOnClickListener { v: View? ->

            Toast.makeText(baseContext, "复制中...", Toast.LENGTH_LONG).show()

            CoroutineScope(Dispatchers.IO).launch {
                copyManyTestFaceImages(application)
                delay(800)
                MainScope().launch {
                    //Kotlin 混淆操作后协程操作失效了，因为是异步操作只能等一下
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
            }

        }
    }

    /**
     * 加载图片
     */
    private fun loadImageList() {
        faceImageList.clear()
        val file = File(STORAGE_FACE_DIR)
        val subFaceFiles = file.listFiles()
        if (subFaceFiles != null) {
            Arrays.sort(subFaceFiles, object : Comparator<File> {
                override fun compare(f1: File, f2: File): Int {
                    val diff = f1.lastModified() - f2.lastModified()
                    return if (diff > 0) -1 else if (diff == 0L) 0 else 1
                }

                override fun equals(obj: Any?): Boolean {
                    return true
                }
            })
            for (index in subFaceFiles.indices) {
                // 判断是否为文件夹
                if (!subFaceFiles[index].isDirectory) {
                    val filename = subFaceFiles[index].name
                    val lowerCaseName = filename.trim { it <= ' ' }.lowercase(Locale.getDefault())
                    if (lowerCaseName.endsWith(".jpg")
                        || lowerCaseName.endsWith(".png")
                        || lowerCaseName.endsWith(".jpeg")
                    ) {
                        faceImageList.add(subFaceFiles[index].path)
                    }
                }
            }
        }
    }


    class FaceImageListAdapter(results: MutableList<String>) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_face_image_list_item, results) {
        override fun convert(helper: BaseViewHolder, item: String) {
            Glide.with(context).load(item).into((helper.getView<View>(R.id.image) as ImageView))
        }
    }

    /**
     * 处理自拍
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val bitmap = BitmapUtils.Companion().getFixedBitmap(currentPhotoPath!!, contentResolver)
            Toast.makeText(baseContext, "处理中...", Toast.LENGTH_LONG).show()
            //Kotlin 混淆操作后协程操作失效了，因为是异步操作只能等一下
            CoroutineScope(Dispatchers.IO).launch {
                FaceImagesManger.Companion().getInstance(application)
                    ?.insertOrUpdateFaceImage(bitmap, "$STORAGE_FACE_DIR/It is you.jpg")
                delay(300)
                MainScope().launch {
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                dispatchTakePictureIntent()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FaceFileProviderUtils.getUriForFile(
                    this,
                    FaceFileProviderUtils.getAuthority(this),
                    photoFile
                )
                //前置摄像头 1:1
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
    }
}