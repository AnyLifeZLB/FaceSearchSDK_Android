## FaceSearchSDK\_Android

应用户要求，人脸识别1：N ,M:N检索独立成库，以便减少体积和快速接入SDK, 这是独立版本的人脸识别搜索，
离线版Android 1:N,M:N 人脸识别检索，速度快，不用联网就能工作，离线授权激活后不限使用时长和设备量。
On\_device Offline Android Face 1:N，M:N Search
(含有1:1人脸识别活体检测各种功能完整版本的工程地址：<https://github.com/AnyLifeZLB/FaceVerificationSDK>)

**如果对APK 包体积不敏感强烈建议使用完整版本SDK.精度更高**
**如果对APK 包体积不敏感强烈建议使用完整版本SDK.精度更高**


<img width="1354" alt="image" src="https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/b61d6ab9-b695-4d75-a9cd-42c27ecacc67">


**首次接入SDK建议先下载DemoAPK安装了解本SDK 的基本功能，服务与政策，下载本Demo 跑一跑流程熟悉基础接入.**

** SDK支持Android 5+，建议设备配置 CPU为八核64位2.0 GHz以上  摄像头RGB 宽动态镜头分辨率720p以上，帧率大于30，无拖影 **

## 简要介绍

简单使用场景(接入正式项目请先充分熟悉SDK接入Demo，验证可行性！！)

* 【1:N】 小区门禁，智能门锁，考勤机，智慧校园、工地、社区、酒店等
* 【M:N】 公安布控，特定群体追踪等

目前人脸检索速度约50MS(三星N9700千张人脸验证)，人脸检索的速度和硬件配置，人脸质量和识别threshold（阈值）设置有关.
运行本Demo体验的时候点击导航页\[增删改人脸] 后进入编辑页面，点击右上角\[拍照]启动系统相机自拍一张.也可以点击\[App 内置200张Ai人脸头像]
模拟N较大的情况，理论上N支持万张以上，需要验证更多人脸素材场景可以自行将人脸导入项目Assert目录验证大容量场景

**特殊DIY Android系统 或 特殊定制硬件（基于RK3X88平台等），外接USB摄像头等**有问题请先提Issues附带Android版本、硬件配置、错误log等信息；
或发反馈邮件到<anylife.zlb@gmail.com> 或添加微信 HaoNan19990322 联系（建议先熟悉人脸识别相关基础）

SDK 演示目前仅仅托管在GitHub，其他镜像版本大概率不是最新的，请移步到GitHub 更新最新的演示代码

## SDK接入简要流程

**1.首先Gradle 中引入依赖**

implementation 'io.github.anylifezlb:FaceSearchSDK:1.8.28' //请依赖最新稳定版本

**2.检查依赖冲突等**

SDK开发的 compileSdk=33，需要Kotlin 环境支持，如果遇到依赖冲突或者SDK集成编译问题请参考升级或解决冲突
```
//若第三方依赖有冲突，或者因为compileSdk过低需要降级依赖版本可以参考修改
configurations.all {
    resolutionStrategy {
        force 'org.android.google:9.9.9' //合适不冲突版本
    }
}

```
**3.确定是否自行管理相机还是默认使用SDK相机管理摄像头**


***推荐快速接入SDK相机管理的初始化***

第一个参数0/1 指定前后摄像头； 第二个参数linearZoom \[0.01f,1.0f] 指定焦距，默认0.1
cameraXFragment = CameraXFragment.newInstance(cameraLensFacing,0.12f);

然后在相机分析回调中调用FaceSearchEngine 搜索引擎进行工作

```
cameraX.setOnAnalyzerListener(imageProxy -> {
      FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
});
```


***自定义管理相机***

如果你的项目需要高定制化相机管理或者自定义硬件相机数据格式，方向不兼容需要自行管理可以不使用SDK 中的CameraXFragment管理相机，我们有个小Demo演示双目摄像头自行管理相机你仅仅需要在子线程中持续输入图像帧Bitmap. SDK 在后续流程中会检测人脸，搜索人脸并进行状态和结果回调。
```
    //1.在摄像头回调预览中循环调用runSearch()方法
    //自行保证Bitmap 的方向角度正确无旋转，清晰度。runSearch必须在子线程运行
    FaceSearchEngine.Companion.getInstance().runSearch(realTimeFaceBmp);
```
更多的请参考 https://github.com/AnyLifeZLB/BinocularCameraFaceSearch

**4.人脸搜索过程中各种参数的初始化**

    ```  
        FaceProcessBuilder faceProcessBuilder = new FaceProcessBuilder.Builder(this)
                .setNeedMultiValidate(false)      //是否需要筛选结果防止误识别，需要硬件CPU配置高，Android 8+
                .setThreshold(0.8f)              //阈值设置，范围限 [0.8 , 0.95] 识别可信度，也是识别灵敏度
                .setNeedNirLiveness(false)        //是否需要红外活体能力，只有1:N VIP 有
                .setNeedRGBLiveness(false)        //是否需要普通RGB活体检测能力，只有1:N VIP 有
                .setLicenceKey("yourLicense")     //合作的VIP定制客户群体需要
                .create();

        faceDetectorUtils.setDetectorParams(faceProcessBuilder);

    ```



//初始化引擎，开始人脸检索
FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);

//搜索的各种状态和结果回调，更多参考Demo

```
 .setProcessCallBack(new SearchProcessCallBack() {
     @Override
     public void onMostSimilar(String similar, Bitmap bitmap) {
         binding.resultId.setText(similar);
         Glide.with(requireContext())
              .load(CACHE_SEARCH_FACE_DIR + File.separatorChar + similar)
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .transform(new RoundedCorners(12)) // 数字根据自己需求来改
              .into(binding.resultImg);
     }

     @Override
     public void onProcessTips(int i) {
          showPrecessTips(i);
     }
 }

```



### 注意事项

1.  所有的人脸都必须通过SDK 的API 插入到人脸管理目录，而不是File 文件放入到目录就行，SDK API 还会提取人脸特征操作
2.  录入的人脸底片请使用正脸无遮挡的清晰照片，平时戴眼睛的依然使用戴眼镜的人脸照片
3.  setNeedMultiValidate 和 setThreshold 根据你项目需要精确度和反应速度合理设置参数
4.  Demo 中有Assert 有250 张测试人脸；你可以在人脸管理页面中右上角通过自拍添加一张个人清晰人脸照进行测试验证
5.  请勿不经验证直接用于生产环境，特别是有严格匹配的场景


## Demo 下载体验

最新版体验下载地址： <https://www.pgyer.com/FaceSearchSDK>
或者直接扫码安装

<div align=center>
<img src="https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/119f33ec-5525-4943-ad35-ef400a408172" width = 30% height = 30% />
</div>


**更多使用说明下载参考本Repo和下载Demo代码体验，里面有比较详尽的使用方法，其中**

### 演示视频快速预览

<https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/46cca423-1cc9-4861-bec9-7457f68ad986>
