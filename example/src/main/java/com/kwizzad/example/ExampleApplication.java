package com.kwizzad.example;

import android.app.Application;

import com.kwizzad.Configuration;
import com.kwizzad.Kwizzad;
import okhttp3.OkHttpClient;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Kwizzad.init(
                new Configuration.Builder()
                        .applicationContext(this)
                        .apiKey("6137f9e8248c6099be8e22224b2dd3444a5c58da88b34cf864cc22a7ea8f5b7d")
                        .okHttpClient(new OkHttpClient())
                        .debug(true)
                        .build()
        );
    }
}