package com.bailun.ocrmodule.camera;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.bailun.ocrmodule.camera.Orientation.ORIENTATION_HORIZONTAL;
import static com.bailun.ocrmodule.camera.Orientation.ORIENTATION_INVERT;
import static com.bailun.ocrmodule.camera.Orientation.ORIENTATION_PORTRAIT;

/**
 * Created by ousiyuan on 2019/3/27 0027.
 * description:
 */
@IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
@Retention(RetentionPolicy.SOURCE)
public @interface Orientation {
    /**
     * 垂直方向
     */
    int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    int ORIENTATION_HORIZONTAL = 90;
    /**
     * 水平翻转方向
     */
    int ORIENTATION_INVERT = 270;
}
