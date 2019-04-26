# 百度AI智能识别
- ## 首先
  项目先导入ocrmodule，确认项目加入权限（注意使用文字识别的代码前，记得向app申请摄像头权限）

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
- ## 第一步
  去百度AI开放平台文字识别功能那里创建应用
![image](https://ai.bdstatic.com/file/E0FE42DB27494CBC895C6F24DBC1FE54)
![image](https://ai.bdstatic.com/file/36B5703778884B73AE6E9241730B1772)

- ## 第二步
  (I)第一种初始化token的方式，在管理应用那里获取对应项目的API Key和Secret Key。

  (II)第二种初始化token的方式,进入该应用管理，下载其aip.license文件，把这文件替换ocrmodule里assets文件下原来的aip.license文件
![image](https://ai.bdstatic.com/file/6E928A2EBAE744E59D8D0CE2984AAC57)

- ## 第三步
- ### 使用（下面以OneActivity和RecCameraActivity为例，OneActivity是RecCameraActivity的上一个界面，RecCameraActivity为进行识别的界面）
  (1). 使用智能文字识别前，先初始化，代码如下。

  (I)第一种初始的方法是调用RecognizeUtil的initAccessTokenWithAkSk方法，在方法里面填入项目的两个key即可
  
```
public void initAccessTokenWithAkSk(Context context) {
        OCR.getInstance(context).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                mHasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                mGotTokenError = true;
                Log.e("Recoginize", "licence方式获取token失败\n" + error.getMessage());
            }
        }, context.getApplicationContext(),  "API Key", "Secret Key");
    }
```
  
  第二种初始化的方法是调用RecognizeUtil的initAccessToken，这个方法只要把
  aip.license文件的位置放对，直接调用方可成功。
  
```
public void initAccessToken(final Context context) {
        OCR.getInstance(context).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                mHasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                mGotTokenError = true;
                Log.e("Recoginize", "licence方式获取token失败\n" + error.getMessage());
            }
        }, context.getApplicationContext());
    }
```

  
  (II)初始化Token的代码我一般放在OneActivity
```
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecognizeUtil.getInstance().initAccessToken(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecognizeUtil.getInstance().release(this);
    }

```
  其实初始化代码可以放在RecCameraActivity初始化，但initAccessToken的方法是百度AI的sdk内部调用了网络请求去认证我们的token，认证成功就可以使用识别功能，获取失败就自己做相应的处理。我一般都是放在OneActivity初始化，然后跳转识别界面时做一个判断。这个checkTokenStatus方法会返回提示token是还在获取中，还是获取失败。
```
if (RecognizeUtil.getInstance().checkTokenStatus(this)){
     startActivity(...);
}
```
  注意：两种初始化的区别，第二种方法的官方说明是，为了不在代码里面明文设置两个key，百度AI的sdk会根据我们的aip.license文件去网络下载我们对应的token下来，但弊端就是初始化的时候要依靠网络，网络不好或弱网时就会初始失败。虽然不管哪种方法认证token的时候都是要请求网络，但第一种方法少了一个下载token的网络请求，在弱网的情况下认证的请求还是可以成功的。

  (2). 使用识别很简单，只要调用RecognizeUtil类里面的recIDCard（识别身份证）和recBankCard（识别银行卡）,只要传入要识别的图片的路径就可以了，其他参数可以参考注释。
  
  (3). 至于怎么编写识别图片的界面，可以参考我们项目的RecCameraActivity类，也可自己实现一个识别界面，其实只要实现一个拍照片或选择照片的界面，把拍到的照片的路径传到识别的方法就已经可以了。
  
- ## 额外
- 该module也接入实时识别身份证的方法。
- 
  (1). 在识别界面初始化实时识别

```
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
         //  初始化本地质量控制模型,释放代码在onDestory中
            CameraNativeHelper.init(this, OCR.getInstance(this).getLicense(),
                    new CameraNativeHelper.CameraNativeInitCallback() {
                        @Override
                        public void onError(int errorCode, Throwable e) {
                            String msg;
                            switch (errorCode) {
                                case NativeConstants.NATIVE_SOLOAD_FAIL:
                                    msg = "加载so失败，请确保apk中存在ui部分的so";
                                    break;
                                case NativeConstants.NATIVE_AUTH_FAIL:
                                    msg = "授权本地质量控制token获取失败";
                                    break;
                                case NativeConstants.NATIVE_INIT_FAIL:
                                    msg = "本地质量控制";
                                    break;
                                default:
                                    msg = String.valueOf(errorCode);
                            }
                            Toast.makeText(CameraActivity.this, "本地质量控制初始化错误，错误原因： " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         IDcardQualityProcess.getInstance().releaseModel();
        // 释放本地质量控制模型
        CameraNativeHelper.release();
    }
```
- 
  (2). 实时识别的代码有点复杂，详情可参考百度AI的demo。大概原理是，使用多线程不断拿到摄像头预览的bitmap放进以下方法。status为0则识别成功，然后关掉其他识别的多线程，把这个bitmap保存到本地文件，然后就跟上面正常拍照识别的流程一样。
  
```
// 调用本地质量控制请求
        int status = IDcardQualityProcess.getInstance().idcardQualityDetectionImg(bitmap, true);
```
