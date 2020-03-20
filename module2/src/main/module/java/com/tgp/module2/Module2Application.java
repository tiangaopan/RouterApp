package com.tgp.module2;

import android.app.Application;

import com.tgp.router_core.TgpRouter;


public class Module2Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TgpRouter.init(this);
    }
}
