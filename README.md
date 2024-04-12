## FaceSearchSDK_Android
应用户要求，人脸识别1：N ,M:N检索独立成库，以便减少体积和快速接入SDK, 这是独立版本的人脸识别搜索，
离线版Android 1:N,M:N 人脸识别检索，速度快，精度高。On_device Offline Android Face 1:N，M:N Search

建议第一次接入的用户先下载DemoAPK安装了解本SDK 的基本功能，服务与政策，下载本Demo 跑一跑流程熟悉基础接入.

(含有1:1 人脸识别活体检测完整版本的工程地址：https://github.com/AnyLifeZLB/FaceVerificationSDK)


<img width="1354" alt="image" src="https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/b61d6ab9-b695-4d75-a9cd-42c27ecacc67">


## 简要介绍
目前检索速度千张人脸约60 毫秒，精确度 >99.5% ,人脸检索的速度和硬件配置，人脸质量和识别threshold（阈值）设置有关

演示的时候点击导航页[增删改人脸] 后进入编辑页面，点击右上角[拍照]启动系统相机自拍一张.也可以点击[App 内置200张Ai人脸头像] 
模拟N较大的情况，理论上N支持万张以上，更多素材可以自行导入项目Assert目录验证大容量场景

**特殊DIY系统 或 特殊定制硬件（基于RK3288平台等），外接USB摄像头等**如有问题请先提Issues附带系统版本、设备型号、错误log等信息；
或发邮件到anylife.zlb@gmail.com ，VIP用户添加微信ID：18707611416 （建议先熟悉人脸识别相关基础）


## 使用场景

- 【1:N】 智能门锁，考勤机，通缉人员行踪搜索，智慧校园、景区、工地、社区、酒店等，（千张人脸仅仅耗时60 Ms ，三星N9700测试）
- 【M:N】 公安布控，等
 
 人脸测试验证数据集可用工程目录Assert 验证 或 哥伦比亚大学公众人物脸部数据库  链接：http://m6z.cn/5DlIR9

## 人脸搜索接入使用

    //1.首先Gradle 中引入依赖 , 请升级到1.6.0以上。老版本不再维护
    implementation 'io.github.anylifezlb:FaceSearchSDK:1.8.0'

    //2.相机的初始化。第一个参数0/1 指定前后摄像头； 第二个参数linearZoom [0.1f,1.0f] 指定焦距，默认0.1
    //这步骤非必须，自定义摄像头管理，双目摄像头参考 ### 自定义相机，双目相机初始化大概步骤
    CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLensFacing,0.12f);

    ``` 
    //3.人脸搜索过程中各种参数的初始化。（更多说明请Github Clone代码体验,）
    
        FaceProcessBuilder faceProcessBuilder = new FaceProcessBuilder.Builder(this)
                .setNeedMultiValidate(false)      //是否需要筛选结果防止误识别，需要硬件CPU配置高，Android 8+
                .setThreshold(0.85f)              //阈值设置，范围限 [0.8 , 0.95] 识别可信度，也是识别灵敏度
                .setNeedNirLiveness(false)        //是否需要红外活体能力，只有1:N VIP 有
                .setNeedRGBLiveness(false)        //是否需要普通RGB活体检测能力，只有1:N VIP 有
                .setLicenceKey("yourLicense")     //合作的VIP定制客户群体需要
                .create();

        faceDetectorUtils.setDetectorParams(faceProcessBuilder);


    //4.子线程调用引擎执行人脸搜索
    //runSearch(）参数有Bitmap 或 imageProxy
    FaceSearchEngine.Companion.getInstance().runSearch(）

    ```

    
   
    更多使用说明下载参考本Repo和下载Demo体验，里面有比较详尽的使用方法，其中
  
    * NaviActivity   Demo 演示导航页面
    * /search/目录   1:N/M:N 人脸识别搜索页面，人脸库管理
    * Assets  目录   测试/验证/演示 的人脸图片

### 自定义相机，双目相机初始化大概步骤

1. gradle 引入 implementation 'io.github.anylifezlb:FaceSearchSDK:2.x.x'
2. 调整项目compileSdk 33 和Kotlin 支持 （人脸管理后期会全部用Java 重构）
3. 支持自己管理摄像头仅仅使用SDK API 进行搜索
   //1.在摄像头回调预览中循环调用runSearch()方法
   //自行保证Bitmap 的方向角度正确无旋转，清晰度。runSearch必须在子线程运行
   FaceSearchEngine.Companion.getInstance().runSearch(realTimeFaceBmp);
4. 各种搜索参数的初始化
   // 2.各种参数的初始化设置
   SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(getApplication())
5. 初始化引擎开始搜索
   //3.初始化引擎
   FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);
6. 所有的人脸都必须通过SDK 的API 插入到人脸管理目录，而不是File 文件放入到目录就行，SDK API 还会提取人脸特征操作
7. Demo 中有Assert 有250 张测试人脸；你可以在人脸管理页面中右上角通过自拍添加一张个人清晰人脸照进行测试验证
8. 了解Demo 后还有问题需要定制解决请联系微信 18707611416 或邮件anylife.zlb@gmail.com


## Demo 下载体验

最新版体验下载地址： https://www.pgyer.com/FaceSearchSDK
或者直接扫码安装

<div align=center>
<img src="https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/119f33ec-5525-4943-ad35-ef400a408172" width = 30% height = 30% />
</div>




### This is Demo Video

https://github.com/AnyLifeZLB/FaceSearchSDK_Android/assets/15169396/46cca423-1cc9-4861-bec9-7457f68ad986



