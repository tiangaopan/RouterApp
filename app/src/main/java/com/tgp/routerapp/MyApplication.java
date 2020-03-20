package com.tgp.routerapp;

import android.app.Application;

import com.tgp.router_core.TgpRouter;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            TgpRouter.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
