package com.weidian.lib.hera.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.weidian.lib.hera.main.HeraService;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        final String userId = "123";//标识宿主App业务用户id
        final String appId = "demoapp";//小程序的id
        final String appPath = "";//小程序的本地存储路径
        HeraService.launchMain(getApplicationContext(), userId, appId, appPath);
        this.finish();
    }

}
