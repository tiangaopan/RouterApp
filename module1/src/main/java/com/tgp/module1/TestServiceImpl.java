package com.tgp.module1;

import android.util.Log;

import com.tgp.base.TestService;
import com.tgp.router_annotation.Route;


@Route(path = "/module1/service")
public class TestServiceImpl implements TestService {

    @Override
    public void test() {
        Log.i("Service", "我是Module1模块测试服务通信");
    }
}
