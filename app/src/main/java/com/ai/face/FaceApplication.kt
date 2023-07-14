package com.ai.face

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
        CACHE_SEARCH_FACE_DIR = filesDir.path + "/faceSearch"    //1:N 人脸搜索目录

        val file= File(CACHE_SEARCH_FACE_DIR) //提前建目录方便导入数据演示
        if (!file.exists()) file.mkdirs()
    }


    companion object {

        lateinit var CACHE_SEARCH_FACE_DIR: String  //1:N 人脸搜索目录
    }



}