package com.ousy.myocrdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idcardquality.IDcardQualityProcess;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.bailun.ocrmodule.IRecognizeListener;
import com.bailun.ocrmodule.RecognizeUtil;
import com.bailun.ocrmodule.camera.CameraNativeHelper;
import com.bailun.ocrmodule.camera.CameraThreadPool;
import com.bailun.ocrmodule.camera.interfaces.OnTakeCameraCallback;
import com.bailun.ocrmodule.entity.NativeConstants;
import com.bailun.ocrmodule.entity.RecConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ousiyuan on 2019/3/28 0028.
 * description:
 */
public class CameraActivity extends Activity {
    private RecCameraView mRecCameraView;
    private ImageView ivPreview;
    private ImageView ivAll;
    private TextView tvHint;

    private boolean isNativeEnable;
    private boolean isNativeManual;
    private String mContentType;
    private File mOutputFile;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mRecCameraView = findViewById(R.id.recview);
        ivPreview = findViewById(R.id.iv_preview);
        ivAll = findViewById(R.id.iv_all);
        tvHint = findViewById(R.id.tv_hint);

        // 隐藏状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //设置全屏
        }

        initParams();
        if (isNativeManual){
            tvHint.setVisibility(View.VISIBLE);

            //  初始化本地质量控制模型,释放代码在onDestory中
            //  调用身份证扫描必须加上 intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL, true); 关闭自动初始化和释放本地模型
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
                            //                        mRecCameraView.setInitNativeStatus(errorCode);
                            Toast.makeText(CameraActivity.this, "本地质量控制初始化错误，错误原因： " + msg, Toast.LENGTH_SHORT).show();
                        }
                    });

            mRecCameraView.setAutoCameraCallback(mAutoTakeCameraCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecCameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecCameraView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.doClear();
        // 释放本地质量控制模型
        CameraNativeHelper.release();
    }

    /**
     * 扫描的时候要做释放
     */
    private void doClear() {
        CameraThreadPool.cancelAutoFocusTimer();
        if (isNativeEnable && !isNativeManual) {
            IDcardQualityProcess.getInstance().releaseModel();
        }
    }

    public void onCrop(View view){
        mRecCameraView.CropCrop();
    }

    public void onTake(View view){
        mRecCameraView.takePicture(mOutputFile, mTakeCameraCallback);
    }

    public void onSure(View view){
        doConfirmResult();
    }

    private void doConfirmResult() {
        CameraThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(mOutputFile);
                    Bitmap bitmap = ((BitmapDrawable) ivPreview.getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Intent intent = new Intent();
//                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, contentType);
//                setResult(Activity.RESULT_OK, intent);
//                finish();
                if (mContentType.equals( RecConstants.CONTENT_TYPE_BANK_CARD)){
                    RecognizeUtil.getInstance().recBankCard(CameraActivity.this, mOutputFile.getAbsolutePath(), new IRecognizeListener<BankCardResult>() {
                        @Override
                        public void onResult(BankCardResult result) {
                            if (result != null){
                                Log.e("ousyxx", result.getBankCardNumber());
                                Toast.makeText(CameraActivity.this, result.getBankCardNumber(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("ousyxx", error);
                        }
                    });
                }else{
                    RecognizeUtil.getInstance().recIDCardFront(CameraActivity.this, mOutputFile.getAbsolutePath(), new IRecognizeListener<IDCardResult>() {
                        @Override
                        public void onResult(IDCardResult result) {
                            if (result != null){
                                Log.e("ousyxx", result.getIdNumber().getWords());
                                Toast.makeText(CameraActivity.this, result.getIdNumber().getWords(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("ousyxx", error);
                        }
                    });
                }

            }
        });
    }

    private OnTakeCameraCallback mTakeCameraCallback = new OnTakeCameraCallback() {
        @Override
        public void onPictureAll(final Bitmap bitmap) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ivAll.setVisibility(View.VISIBLE);
                    ivAll.setImageBitmap(bitmap);
                }
            });
        }

        @Override
        public void onPictureTaken(final Bitmap bitmap) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // ousy 拍照识别3
                    ivPreview.setImageBitmap(bitmap);
                    showResultConfirm();
                }
            });
        }
    };

    private OnTakeCameraCallback mAutoTakeCameraCallback = new OnTakeCameraCallback() {
        @Override
        public void onPictureAll(Bitmap bitmap) {

        }

        @Override
        public void onPictureTaken(final Bitmap bitmap) {
            // ousy 扫描成功回调
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(mOutputFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        bitmap.recycle();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    Intent intent = new Intent();
//                    intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, contentType);
//                    setResult(Activity.RESULT_OK, intent);
//                    finish();

                    RecognizeUtil.getInstance().recIDCardFront(CameraActivity.this, mOutputFile.getAbsolutePath(), new IRecognizeListener<IDCardResult>() {
                        @Override
                        public void onResult(IDCardResult result) {
                            if (result != null){
                                Log.e("ousyxx", result.getIdNumber().getWords());
                                Toast.makeText(CameraActivity.this, result.getIdNumber().getWords(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("ousyxx", error);
                        }
                    });
                }
            });
        }
    };


    public void onReTake(View view){
        showTakePicture();
    }

    private void initParams() {
        String outputPath = getIntent().getStringExtra(RecConstants.KEY_OUTPUT_FILE_PATH);
        isNativeEnable = getIntent().getBooleanExtra(RecConstants.KEY_NATIVE_ENABLE, true);
        isNativeManual = getIntent().getBooleanExtra(RecConstants.KEY_NATIVE_MANUAL, false);
        mContentType = getIntent().getStringExtra(RecConstants.KEY_CONTENT_TYPE);

        if (outputPath != null) {
            mOutputFile = new File(outputPath);
        }

        mRecCameraView.setEnableScan(isNativeEnable);
    }

    private void showTakePicture() {
        mRecCameraView.getCameraControl().resume();
        mRecCameraView.setVisibility(View.VISIBLE);
        ivAll.setVisibility(View.INVISIBLE);
        ivPreview.setVisibility(View.INVISIBLE);
    }

    private void showResultConfirm() {
        mRecCameraView.getCameraControl().pause();
        ivPreview.setVisibility(View.VISIBLE);
        mRecCameraView.setVisibility(View.INVISIBLE);
    }
}
