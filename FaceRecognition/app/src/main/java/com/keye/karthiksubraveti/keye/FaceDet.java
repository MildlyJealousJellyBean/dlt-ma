package com.keye.karthiksubraveti.keye;

import android.graphics.Bitmap;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by houzhi on 16-10-20.
 * Modified by tzutalin on 16-11-15
 */
public class FaceDet {
    private static final String TAG = "dlib";
    private String mRecogPath = "";

    // accessed by native methods
    @SuppressWarnings("unused")
    private long mNativeDetContext;
    private long mNativeRecogContext;
    private String mLandMarkPath = "";

    static {
        try {
            System.loadLibrary("android_dlib");
            jniNativeClassInit();
            Log.d(TAG, "jniNativeClassInit success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "library not found");
        }
    }

    @SuppressWarnings("unused")
    public FaceDet() {
        jniInit(mLandMarkPath, mRecogPath);
    }

    public FaceDet(String landMarkPath, String recogPath) {
        mLandMarkPath = landMarkPath;
        mRecogPath = recogPath;
        jniInit(mLandMarkPath, mRecogPath);
    }

    @Nullable
    @WorkerThread
    public int saveFaceChips(@NonNull String srcPath, String dstPath) {
        int size = jniSaveFaceChips(srcPath, dstPath);
        return size;
    }

    @Nullable
    @WorkerThread
    public int[] recognizeFaceChips(@NonNull String srcPath) {
        return jniRecognizeFaceChips(srcPath);
    }
    @Nullable
    @WorkerThread
    public int[] recognizeFaceChipsBitmap(@NonNull Bitmap bmp) {
        return jniRecognizeFaceChipsBitmap(bmp);
    }

    @Nullable
    @WorkerThread
    public void trainClassifier(@NonNull String srcPath) {
        jniTrainClassifier(srcPath);
    }

    @Nullable
    @WorkerThread
    public void storeModel(@NonNull String srcPath) {
        jniStoreModel(srcPath);
    }



    @Nullable
    @WorkerThread
    public List<VisionDetRet> detect(@NonNull Bitmap bitmap) {
        VisionDetRet[] detRets = jniBitmapDetect(bitmap);
        return Arrays.asList(detRets);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    public void release() {
        jniDeInit();
    }

    @Keep
    private native static void jniNativeClassInit();

    @Keep
    private synchronized native int jniInit(String landmarkModelPath, String recogPath);

    @Keep
    private synchronized native int jniDeInit();

    @Keep
    private synchronized native int jniSaveFaceChips(String srcPath, String dstPath);

    @Keep
    private synchronized native int[] jniRecognizeFaceChips(String srcPath);

    @Keep
    private synchronized native int[] jniRecognizeFaceChipsBitmap(Bitmap bmp);

    @Keep
    private synchronized native int jniTrainClassifier(String srcPath);

    @Keep
    private synchronized native int jniStoreModel(String srcPath);

    @Keep
    private synchronized native VisionDetRet[] jniBitmapDetect(Bitmap bitmap);

    @Keep
    private synchronized native VisionDetRet[] jniDetect(String path);
}