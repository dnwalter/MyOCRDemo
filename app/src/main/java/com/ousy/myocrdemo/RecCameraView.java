package com.ousy.myocrdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.baidu.idcardquality.IDcardQualityProcess;
import com.bailun.ocrmodule.camera.Camera1Control;
import com.bailun.ocrmodule.camera.CameraThreadPool;
import com.bailun.ocrmodule.camera.Orientation;
import com.bailun.ocrmodule.camera.interfaces.ICameraControl;
import com.bailun.ocrmodule.camera.interfaces.OnTakeCameraCallback;
import com.bailun.ocrmodule.entity.NativeConstants;
import com.bailun.ocrmodule.util.ImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ousiyuan on 2019/3/28 0028.
 * description:
 */
public class RecCameraView extends ConstraintLayout {

    private View viewFrame;
    private ICameraControl mCameraControl;
    private View viewDisplay;
    private CameraViewTakePictureCallback mCameraViewTakePictureCallback = new CameraViewTakePictureCallback();
    private byte[] mData;
    private OnTakeCameraCallback mAutoCameraCallback;

    public RecCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_rec_camera, this, true);
        initView();
    }

    private void initView() {
        viewFrame = findViewById(R.id.frame);

        mCameraControl = new Camera1Control(getContext());
        // 设置横屏
        mCameraControl.setDisplayOrientation(Orientation.ORIENTATION_HORIZONTAL);

        viewDisplay = mCameraControl.getDisplayView();
        addView(viewDisplay, 0);
    }

    public ICameraControl getCameraControl() {
        return mCameraControl;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewDisplay.layout(left, 0, right, bottom - top);
    }

    public void start() {
        mCameraControl.start();
        setKeepScreenOn(true);
    }

    public void stop() {
        mCameraControl.stop();
        setKeepScreenOn(false);
    }

    public void takePicture(final File file, final OnTakeCameraCallback callback) {
        mCameraViewTakePictureCallback.file = file;
        mCameraViewTakePictureCallback.callback = callback;
        mCameraControl.takePicture(mCameraViewTakePictureCallback);
    }

    /**
     * 拍摄后的照片。需要进行裁剪。有些手机（比如三星）不会对照片数据进行旋转，而是将旋转角度写入EXIF信息当中，
     * 所以需要做旋转处理。
     *
     * @param outputFile 写入照片的文件。
     * @param data  原始照片数据。
     * @param rotation   照片exif中的旋转角度。
     *
     * @return 裁剪好的bitmap。
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Bitmap crop(File outputFile, byte[] data, int rotation) {
        try {
            Bitmap bitmap = cropBitmap( data, rotation);

            try {
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class CameraViewTakePictureCallback implements ICameraControl.OnTakePictureCallback {
        private File file;
        private OnTakeCameraCallback callback;
        @Override
        public void onPictureTaken(final byte[] data) {
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mData = data;
                    final int rotation = ImageUtil.getOrientation(data);
                    // ousy 拍照识别2
                    // ousy 获取拍照并裁剪
//                    Bitmap bitmap = crop(file, data, rotation);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    // 下面是自己测试先把完整拍下的图片显示，再裁剪看看效果
//                    callback.onPictureAll(bitmap);
                }
            });
        }

        public void testCrop(){
            CameraThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    final int rotation = ImageUtil.getOrientation(mData);
                    // ousy 拍照识别2
                    // ousy 获取拍照并裁剪
                    Bitmap bitmap = crop(file, mData, rotation);
                    callback.onPictureTaken(bitmap);
                }
            });
        }
    }

    public void CropCrop(){
        mCameraViewTakePictureCallback.testCrop();
    }

    //region 扫描身份证
    /**
     * 是否是本地质量控制扫描
     */
    private boolean isEnableScan;

    public void setEnableScan(boolean enableScan) {
        isEnableScan = enableScan;
        if (isEnableScan) {
            // ousy 扫描4
            mCameraControl.setDetectCallback(new ICameraControl.OnDetectPictureCallback() {
                @Override
                public int onDetect(byte[] data, int rotation) {
                    return detect(data, rotation);
                }
            });
        }

        if (isEnableScan) {
            mCameraControl.setDetectCallback(new ICameraControl.OnDetectPictureCallback() {
                @Override
                public int onDetect(byte[] data, int rotation) {
                    return detect(data, rotation);
                }
            });
        }
    }

    /**
     *  本地检测初始化，模型加载标识
     */
    private int initNativeStatus  = NativeConstants.NATIVE_AUTH_INIT_SUCCESS;
    public void setInitNativeStatus(int initNativeStatus) {
        this.initNativeStatus = initNativeStatus;
    }

    private int detect(byte[] data, final int rotation) {
        if (initNativeStatus != NativeConstants.NATIVE_AUTH_INIT_SUCCESS) {
//            showTipMessage(initNativeStatus);
            return 1;
        }
        // 扫描成功阻止多余的操作
        if (mCameraControl.getAbortingScan().get()) {
            return 0;
        }

        Bitmap bitmap = null;
        try {
            bitmap = cropBitmap(data, rotation);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }

        final int status;

        // 调用本地质量控制请求
        status = IDcardQualityProcess.getInstance().idcardQualityDetectionImg(bitmap, true);

        // 当有某个扫描处理线程调用成功后，阻止其他线程继续调用本地控制代码
        if (status == 0) {
            // 扫描成功阻止多线程同时回调
            if (!mCameraControl.getAbortingScan().compareAndSet(false, true)) {
                bitmap.recycle();
                return 0;
            }
            // ousy 扫描成功
            mAutoCameraCallback.onPictureTaken(bitmap);
        }

//        showTipMessage(status);

        return status;
    }

    // 设置扫描识别身份证的回调
    public void setAutoCameraCallback(OnTakeCameraCallback callback) {
        mAutoCameraCallback = callback;
    }
    //endregion

    private Bitmap cropBitmap(byte[] data, int rotation) throws IOException{
        Rect previewFrame = mCameraControl.getPreviewFrame();

        if (previewFrame.width() == 0 || previewFrame.height() == 0) {
            return null;
        }

        // BitmapRegionDecoder不会将整个图片加载到内存。
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);



        int width = rotation % 180 == 0 ?  decoder.getWidth() : decoder.getHeight();
        int height = rotation % 180 == 0 ? decoder.getHeight() : decoder.getWidth();

        // 要裁剪的4个位置
        int left = (viewFrame.getLeft() - previewFrame.left) * width / previewFrame.width();
        int top = (viewFrame.getTop() - previewFrame.top) * height / previewFrame.height();
        int right = (viewFrame.getRight() - previewFrame.left) * width / previewFrame.width();
        int bottom = (viewFrame.getBottom() - previewFrame.top) * height / previewFrame.height();

        Rect region = new Rect();
        region.left = left;
        region.top = top;
        region.right = right;
        region.bottom = bottom;

        // 90度或者270度旋转
        if (rotation % 180 == 90) {
            int x = decoder.getWidth() / 2;
            int y = decoder.getHeight() / 2;

            int rotatedWidth = region.height();
            int rotated = region.width();

            // 计算，裁剪框旋转后的坐标
            region.left = x - rotatedWidth / 2;
            region.top = y - rotated / 2;
            region.right = x + rotatedWidth / 2;
            region.bottom = y + rotated / 2;
            region.sort();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        // 最大图片大小。
        int maxPreviewImageSize = 2560;
        int size = Math.min(decoder.getWidth(), decoder.getHeight());
        size = Math.min(size, maxPreviewImageSize);

        options.inSampleSize = ImageUtil.calculateInSampleSize(options, size, size);
        options.inScaled = true;
        options.inDensity = Math.max(options.outWidth, options.outHeight);
        options.inTargetDensity = size;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = decoder.decodeRegion(region, options);

        if (rotation != 0) {
            // 只能是裁剪完之后再旋转了。有没有别的更好的方案呢？
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            if (bitmap != rotatedBitmap) {
                // 有时候 createBitmap会复用对象
                bitmap.recycle();
            }
            bitmap = rotatedBitmap;
        }

        return bitmap;
    }
}
