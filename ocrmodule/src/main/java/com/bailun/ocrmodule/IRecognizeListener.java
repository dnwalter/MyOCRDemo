package com.bailun.ocrmodule;

/**
 * Created by ousiyuan on 2019/3/27 0027.
 * description:
 */
public interface IRecognizeListener<T> {
    void onResult(T result);
    void onError(String error);
}
