package com.tgp.routerapp;

import android.util.Log;

import com.tgp.base.TestService;
import com.tgp.router_annotation.Route;


@Route(path = "/main/service1")
public class TestServiceImpl1 implements TestService {


    @Override
    public void test() {
        Log.i("Service", "我是app模块测试服务通信1");
    }
}
