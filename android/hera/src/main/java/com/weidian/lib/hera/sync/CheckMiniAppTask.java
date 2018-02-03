package com.weidian.lib.hera.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.weidian.lib.hera.config.HeraConfig;
import com.weidian.lib.hera.trace.HeraTrace;
import com.weidian.lib.hera.utils.FileUtil;
import com.weidian.lib.hera.utils.IOUtil;
import com.weidian.lib.hera.utils.OkHttpUtil;
import com.weidian.lib.hera.utils.StorageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zengwh on 2018/2/3.
 * 检测小程序是否有新版本，有则下载
 */
public class CheckMiniAppTask extends AsyncTask<String, Void, Boolean> {
    protected static final String TAG = "CheckMiniAppTask";
    private Context mContext;
    private CheckCallback mCallback;

    public CheckMiniAppTask(Context context,CheckCallback callback) {
        mContext = context;
        mCallback = callback;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length < 2) {
            mCallback.onResult(null);
            return false;
        }
        final String appId = "10000";
        String appPath = params[1];
        String outputPath = StorageUtil.getMiniAppSourceDir(mContext, appId).getAbsolutePath();
        String url = HeraConfig.APP_SERVICE_URI+"/api/om/mapp/version/"+appId;
        HeraTrace.d(TAG,"开始后台发起小程序检查任务:"+url);
        try {
            final Request request = new Request.Builder().url(url).build();
            OkHttpUtil.enqueue(request, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    HeraTrace.e(TAG,"小程序检查任务失败:"+e.getMessage());
                    mCallback.onResult(null);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String strResult = response.body().string();

                        HeraTrace.d(TAG,"小程序检查任务结果:"+strResult);
                        JSONObject resultJson = new JSONObject(strResult);
                        if("0".equals(resultJson.getString("state"))){
                            Toast.makeText(mContext,resultJson.getString("message"),Toast.LENGTH_LONG);
                            mCallback.onResult(null);
                        }else{
                            JSONObject version = resultJson.getJSONObject("row").getJSONObject("version");
                            //小程序路路径
                            final String minAppPath = version.getString("minAppPath");
                            HeraTrace.d(TAG,minAppPath);

                            downloadMinApp(appId,minAppPath,mCallback);
                        }
                    } catch (JSONException e) {
                        HeraTrace.e(TAG,"小程序检查任务解析失败,"+e.getMessage());
                        mCallback.onResult(null);
                    }


                }
            });
        } catch (Exception e) {
            mCallback.onResult(null);
        }
        return true;
    }

    private void downloadMinApp(final String appId,final String minAppPath,final CheckCallback callback){
        try {
            String minAppFullPath = HeraConfig.APP_SERVICE_URI+minAppPath;
            Request request = new Request.Builder().url(minAppFullPath).build();

            OkHttpUtil.enqueue(request, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    HeraTrace.w(TAG, "下载小程序失败，可能网络问题!");
                    callback.onResult(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    File tempFile = null;
                    InputStream is = null;
                    FileOutputStream os = null;

                    String mTempDir = StorageUtil.getMiniAppTempDir(mContext, appId).getAbsolutePath() + File.separator;

                    String mTempFullPath = null;
                    try {
                        String urlPath = response.request().url().encodedPath();
                        String tempFileName = StorageUtil.PREFIX_TMP + System.currentTimeMillis()
                                + FileUtil.getFileSuffix(urlPath);
                        tempFile = new File(mTempDir, tempFileName);
                        mTempFullPath = tempFile.getAbsolutePath();
                        is = response.body().byteStream();
                        os = new FileOutputStream(tempFile);
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, len);
                        }
                        os.flush();
                    } catch (IOException e) {
                        tempFile = null;
                    } finally {
                        IOUtil.closeAll(is, os);
                    }
                    HeraTrace.d(TAG,"小程序下载到："+mTempFullPath);

                    callback.onResult(mTempFullPath);
                }
            });
        } catch (Exception e) {
            HeraTrace.e(TAG,"小程序下载失败："+e.getMessage());
            callback.onResult(null);
        }
    }


    public interface CheckCallback {
        void onResult(final String appPath);
    }
}
