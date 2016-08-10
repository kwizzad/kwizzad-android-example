package com.kwizzad.example;

import android.app.Application;

import com.kwizzad.Configuration;
import com.kwizzad.Kwizzad;

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Kwizzad.init(
                new Configuration.Builder()
                        .applicationContext(this)
                        .apiKey("b81e71a86cf1314d249791138d642e6c4bd08240f21dd31811dc873df5d7469d")
                        .debug(true)
                        .build()
        );
    }
}