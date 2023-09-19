## FaceSearchSDK_Android
应用户要求，人脸识别1：N ,M:N检索独立成库，以便减少体积和快速接入SDK, 这是独立版本的人脸识别搜索，
离线版Android 1:N,M:N 人脸识别检索，速度快，精度高。On-device Offline Android Face 1:N Search

建议第一次接入的用户先了解本SDK 的基本功能，服务与政策，下载本Demo 跑一跑流程熟悉基础接入

(含有1:1 人脸识别活体检测完整版本的工程地址：https://github.com/AnyLifeZLB/FaceVerificationSDK)

![image](https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/df598b77-52c1-4461-9500-3aa5617bbc5d)


## 简要介绍
目前检索速度千张人脸约60 毫秒，精确度 >99.5% ,人脸检索的速度和硬件配置，人脸质量和识别threshold（阈值）设置有关

演示的时候点击导航页[增删改人脸] 后进入编辑页面，点击右上角[拍照]启动系统相机自拍一张.也可以点击[App 内置200张Ai人脸头像] 
模拟N较大的情况，理论上N支持万张以上，更多素材可以自行导入项目Assert目录验证大容量场景

**特殊DIY系统 或 特殊定制硬件（基于RK3288平台等），外接USB摄像头等**如有问题请先提Issues附带系统版本、设备型号、错误log等信息；
或发邮件到anylife.zlb@gmail.com ，VIP用户添加微信ID：18707611416



## 使用场景

【1:N】 智能门锁，考勤机，通缉人员行踪搜索，智慧校园、景区、工地、社区、酒店等，（千张人脸仅仅耗时60 Ms ，三星N9700测试）
【M:N】 公安布控等
 人脸测试验证数据集可用哥伦比亚大学公众人物脸部数据库  链接：http://m6z.cn/5DlIR9

## 接入使用

    //1.首先Gradle 中引入依赖 
    implementation 'io.github.anylifezlb:FaceRecognition:1.x.y'

    //2.Camera相机的初始化。第一个参数0/1 指定前后摄像头；第二个参数linearZoom [0.1f,1.0f] 指定焦距，默认0.1
    CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLensFacing,0.12f);

    ``` 
    //3.人脸识别过程中各种参数的初始化。（更多说明请Github Clone代码体验,）
    
        FaceProcessBuilder faceProcessBuilder = new FaceProcessBuilder.Builder(this)
                .setThreshold(0.8f)                 //threshold（阈值）设置，范围仅限 [0.8-0.9]，默认0.8
                .setLicenceKey("yourLicense key")   //申请的License
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个图片库的目录
                .create();

        faceDetectorUtils.setDetectorParams(faceProcessBuilder);
    ```
   
    更多使用说明下载参考本Repo和下载Demo体验，里面有比较详尽的使用方法，其中
  
    * NaviActivity  Demo 演示导航页面
    * /search/目录   1:N/M:N 人脸识别搜索页面，人脸库管理


## Demo 下载体验

最新版体验下载地址： https://www.pgyer.com/FaceSearchSDK
或者直接扫码安装

<div align=center>
<img src="https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/119f33ec-5525-4943-ad35-ef400a408172" width = 30% height = 30% />
</div>


### This is Demo Video


https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/46cca423-1cc9-4861-bec9-7457f68ad986



