package com.ai.facesearch

import android.app.Application
import java.io.File

/**
 * global param init
 *
 *
 */
class FaceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //人脸文件保存路径，建议放在内部存储目录，不要暴露出去
        STORAGE_FACE_DIR = cacheDir.path + "/faceSearch"

        var file=File(STORAGE_FACE_DIR)
        if (!file.exists()) file.mkdirs()


    }


     companion object {
        lateinit var STORAGE_FACE_DIR: String
    }


}