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


        //1:N （M：N）人脸搜索目录
        //增删改人脸 参考@FaceImageEditActivity 中的方式，需要使用SDK 中的API 进行操作不能直接插入图片
        CACHE_SEARCH_FACE_DIR = filesDir.path + "/faceSearch"

        val file= File(CACHE_SEARCH_FACE_DIR) //提前建目录方便导入数据演示
        if (!file.exists()) file.mkdirs()
    }


    companion object {

        lateinit var CACHE_SEARCH_FACE_DIR: String  //1:N 人脸搜索目录
    }



}