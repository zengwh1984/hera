package com.weidian.lib.hera.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.weidian.lib.hera.trace.HeraTrace;
import com.weidian.lib.hera.utils.StorageUtil;
import com.weidian.lib.hera.utils.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zengwh on 2018/2/4.
 */
public class UnZipMiniAppTask extends AsyncTask<String, Void, Boolean> {
    private final String TAG = "UnZipMiniAppTask";
    private Context mContext;
    private HeraAppManager.SyncCallback mCallback;

    public UnZipMiniAppTask(Context context, HeraAppManager.SyncCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length < 2) {
            return false;
        }
        String appId = params[0];
        String appPath = params[1];
        String outputPath = StorageUtil.getMiniAppSourceDir(mContext, appId).getAbsolutePath();
        boolean unzipResult = false;
        if (!TextUtils.isEmpty(appPath)) {
            unzipResult = ZipUtil.unzipFile(appPath, outputPath);
        }
        if (!unzipResult) {
            try {
                InputStream in = mContext.getAssets().open(appId + ".zip");
                unzipResult = ZipUtil.unzipFile(in, outputPath);
            } catch (IOException e) {
                HeraTrace.e(TAG, e.getMessage());
            }
        }
        return unzipResult && new File(outputPath, "service.html").exists();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mCallback.onResult(aBoolean);
    }
}