package com.bailun.ocrmodule.entity;

/**
 * Created by ousiyuan on 2019/3/28 0028.
 * description:
 */
public class NativeConstants {
    /**
     * 本地模型授权，加载成功
     */
    public static final int NATIVE_AUTH_INIT_SUCCESS = 0;

    /**
     * 本地模型授权，缺少SO
     */
    public static final int NATIVE_SOLOAD_FAIL = 10;

    /**
     * 本地模型授权，授权失败，token异常
     */
    public static final int NATIVE_AUTH_FAIL = 11;

    /**
     * 本地模型授权，模型加载失败
     */
    public static final int NATIVE_INIT_FAIL = 12;
}
