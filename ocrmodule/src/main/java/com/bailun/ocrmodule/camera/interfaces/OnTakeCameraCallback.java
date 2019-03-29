package com.bailun.ocrmodule.camera.interfaces;

import android.graphics.Bitmap;

/**
 * Created by ousiyuan on 2019/3/28 0028.
 * description:
 */
public interface OnTakeCameraCallback {
    void onPictureAll(Bitmap bitmap);
    void onPictureTaken(Bitmap bitmap);
}
