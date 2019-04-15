package com.bailun.ocrmodule;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;

import java.io.File;

/**
 * Created by ousiyuan on 2019/3/27 0027.
 * description:
 */
public class RecognizeUtil {

    private static RecognizeUtil sInstance;
    private boolean mHasGotToken = false;
    private boolean mGotTokenError = false;

    public static synchronized RecognizeUtil getInstance() {
        if (sInstance == null) {
            sInstance = new RecognizeUtil();
        }
        return sInstance;
    }

    /**
     * 用明文ak，sk初始化
     */
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
        }, context.getApplicationContext(),  "e76accfxp5LU0AKmTA9F05XD", "nu329KyFoIZX3lTsLj53rcOhFlcMWw45");
    }

    /**
     * 以license文件方式初始化
     */
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

    public boolean checkTokenStatus(Context context) {
        if (mGotTokenError){
            Toast.makeText(context, "系统繁忙，请再次进入页面点击重试", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!mHasGotToken) {
            Toast.makeText(context, "token还未成功获取，请稍等", Toast.LENGTH_LONG).show();
        }
        return mHasGotToken;
    }

    // 解析银行卡
    public void recBankCard(Context context, String filePath, final IRecognizeListener<BankCardResult> listener) {
        if (!checkTokenStatus(context)) {
            return;
        }
        BankCardParams param = new BankCardParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance(context).recognizeBankCard(param, new OnResultListener<BankCardResult>() {
            @Override
            public void onResult(BankCardResult result) {
                listener.onResult(result);
            }

            @Override
            public void onError(OCRError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // 识别身份证正面，横向拍照
    public void recIDCardFront(Context context, String filePath, final IRecognizeListener<IDCardResult> listener){
        recIDCard(context, IDCardParams.ID_CARD_SIDE_FRONT, false, filePath, listener);
    }

    /**
     * 识别身份证
     * @param context
     * @param idCardSide IDCardParams.ID_CARD_SIDE_FRONT或IDCardParams.ID_CARD_SIDE_BACK
     * @param filePath
     * @param direction true为纵向，false为横向
     */
    public void recIDCard(Context context, String idCardSide, boolean direction, String filePath, final IRecognizeListener<IDCardResult> listener) {
        if (!checkTokenStatus(context)) {
            return;
        }
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(direction);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance(context).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                listener.onResult(result);
            }

            @Override
            public void onError(OCRError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // 释放资源
    public void release(Context context){
        // 释放内存资源
        OCR.getInstance(context).release();
    }
}
