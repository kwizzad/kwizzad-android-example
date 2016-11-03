package com.kwizzad.example;

import android.app.Application;

import com.kwizzad.Configuration;
import com.kwizzad.Kwizzad;
import com.kwizzad.model.Gender;

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

        // Optional: here you can set known user data that will improve KWIZZAD's targeting capabilities.
        Kwizzad.getUserData().setUserId("12345");
        Kwizzad.getUserData().setGender(Gender.MALE);
        Kwizzad.getUserData().setName("Horst Guenther");
    }
}