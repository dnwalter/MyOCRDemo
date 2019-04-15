package com.ousy.myocrdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.bailun.ocrmodule.RecognizeUtil;
import com.bailun.ocrmodule.camera.CameraNativeHelper;
import com.bailun.ocrmodule.entity.NativeConstants;
import com.bailun.ocrmodule.entity.RecConstants;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String mPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPath = new File(getFilesDir(), "pic.jpg").getAbsolutePath();

        RecognizeUtil.getInstance().initAccessTokenWithAkSk(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecognizeUtil.getInstance().release(this);
        // 释放本地质量控制模型
//        CameraNativeHelper.release();
    }

    public void onCardOne(View view){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(RecConstants.KEY_OUTPUT_FILE_PATH,
                mPath);
        intent.putExtra(RecConstants.KEY_CONTENT_TYPE, RecConstants.CONTENT_TYPE_ID_CARD_FRONT);
//        startActivityForResult(intent, REQUEST_CODE_CAMERA);
        startActivity(intent);
    }

    public void onCardTwo(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(RecConstants.KEY_OUTPUT_FILE_PATH,
                mPath);
        intent.putExtra(RecConstants.KEY_NATIVE_ENABLE, true);
        // KEY_NATIVE_MANUAL设置了之后CameraActivity中不再自动初始化和释放模型
        // 请手动使用CameraNativeHelper初始化和释放模型
        // 推荐这样做，可以避免一些activity切换导致的不必要的异常
        intent.putExtra(RecConstants.KEY_NATIVE_MANUAL, true);
        intent.putExtra(RecConstants.KEY_CONTENT_TYPE, RecConstants.CONTENT_TYPE_ID_CARD_FRONT);
//        startActivityForResult(intent, REQUEST_CODE_CAMERA);
        startActivity(intent);
    }

    public void onCardThree(View view){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(RecConstants.KEY_OUTPUT_FILE_PATH, mPath);
        intent.putExtra(RecConstants.KEY_CONTENT_TYPE,
                RecConstants.CONTENT_TYPE_BANK_CARD);
        startActivityForResult(intent, RecConstants.REQUEST_CODE_BANKCARD);
    }

    public void onScan(View view){
        if (!RecognizeUtil.getInstance().checkTokenStatus(this))
            return;
        //  初始化本地质量控制模型,释放代码在onDestory中
        //  调用身份证扫描必须加上 intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL, true); 关闭自动初始化和释放本地模型
//        CameraNativeHelper.init(this, OCR.getInstance(this).getLicense(),
//                new CameraNativeHelper.CameraNativeInitCallback() {
//                    @Override
//                    public void onError(int errorCode, Throwable e) {
//                        String msg;
//                        switch (errorCode) {
//                            case NativeConstants.NATIVE_SOLOAD_FAIL:
//                                msg = "加载so失败，请确保apk中存在ui部分的so";
//                                break;
//                            case NativeConstants.NATIVE_AUTH_FAIL:
//                                msg = "授权本地质量控制token获取失败";
//                                break;
//                            case NativeConstants.NATIVE_INIT_FAIL:
//                                msg = "本地质量控制";
//                                break;
//                            default:
//                                msg = String.valueOf(errorCode);
//                        }
//                        //                        mRecCameraView.setInitNativeStatus(errorCode);
//                        Toast.makeText(MainActivity.this, "本地质量控制初始化错误，错误原因： " + msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
    }
}
