package com.dn_alan.module2;

import android.app.Application;

import com.dn_alan.router_core.DNRouter;



public class Module2Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TgpRouter.init(this);
    }
}
