package com.tgp.module2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tgp.base.TestService;
import com.tgp.router_annotation.Extra;
import com.tgp.router_annotation.Route;
import com.tgp.router_core.TgpRouter;

import androidx.annotation.Nullable;


@Route(path = "/module2/test")
public class Module2Activity extends Activity {

    @Extra
    String msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);

        TgpRouter.getInstance().inject(this);
        Log.i("module2", "我是模块2:" + msg);

        //当处于组件模式的时候 BuildConfig.isModule
        if (true){
            TestService testService = (TestService) TgpRouter.getInstance().build("/main/service1")
                    .navigation();
            testService.test();

            TestService testService1 = (TestService) TgpRouter.getInstance().build("/main/service2")
                    .navigation();
            testService1.test();

            TestService testService2 = (TestService) TgpRouter.getInstance().build("/module1/service")
                    .navigation();
            testService2.test();

            TestService testService3 = (TestService) TgpRouter.getInstance().build("/module2/service")
                    .navigation();
            testService3.test();
        }

    }

    public void mainJump(View view) {
        if (true){
            TgpRouter.getInstance().build("/main/test").withString("a",
                    "从Module2").navigation(this);
        }else{
            Toast.makeText(this,"当前处于组件模式,无法使用此功能",
                    Toast.LENGTH_SHORT).show();
        }



    }

    public void module1Jump(View view) {
        TgpRouter.getInstance().build("/module1/test").withString("msg",
                "从Module2").navigation(this);
    }
}
