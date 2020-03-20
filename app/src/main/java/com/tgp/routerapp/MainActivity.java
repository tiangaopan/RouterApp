package com.tgp.routerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tgp.base.TestService;
import com.tgp.router_core.TgpRouter;
import com.tgp.routerapp.parcelable.TestParcelable;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Map<String, RouteMeta> map = new HashMap<>();
//        DNRouter$$Group$$main dnRouter$$Group$$main = new DNRouter$$Group$$main();
//        dnRouter$$Group$$main.loadInto(map);
//
//        //map 就是 路由表
//        Intent intent = new Intent(this, map.get("/main/test").getDestination());
//        startActivity(intent);


        /**
         * 组件服务共享 通信
         */
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


    // 应用内跳转
    public void innerJump(View view) {
        ArrayList<Integer> integers = new ArrayList<Integer>();
        integers.add(1);
        integers.add(2);

        ArrayList<String> strings = new ArrayList<String>();
        strings.add("1");
        strings.add("2");

        ArrayList<TestParcelable> ps = new ArrayList<TestParcelable>();

        TestParcelable testParcelable = new TestParcelable(1, "a");
        TestParcelable testParcelable2 = new TestParcelable(2, "d");
        ps.add(testParcelable);
        ps.add(testParcelable2);

        TgpRouter.getInstance().build("/main/test").withString("a",
                "从MainActivity").withInt("b", 1).withShort("c", (short) 2).withLong("d", 3)
                .withFloat("e", 1.0f).withDouble("f", 1.1).withByte("g", (byte) 1).withBoolean
                ("h", true).withChar("i", '好').withParcelable("j", testParcelable)
                .withStringArray("aa",
                        new String[]{"1", "2"}).withIntArray("bb", new int[]{1, 2}).withShortArray
                ("cc", new short[]{(short) 2, (short) 2}).withLongArray("dd", new long[]{1, 2})
                .withFloatArray("ee", new float[]{1.0f, 1.0f}).withDoubleArray("ff", new
                double[]{1.1, 1.1}).withByteArray("gg",
                new byte[]{(byte) 1, (byte) 1}).withBooleanArray
                ("hh", new boolean[]{true, true}).withCharArray("ii", new char[]{'好', '好'})
                .withParcelableArray("jj", new TestParcelable[]{testParcelable, testParcelable2})
                .withParcelableArrayList("k1", ps).withParcelableArrayList("k2", ps)
                .withStringArrayList("k3", strings).withIntegerArrayList("k4", integers)
                .withInt("hhhhhh", 1)
                .navigation(this, 100);
    }

    // 跳转模块1
    public void module1Jump(View view) {
        TgpRouter.getInstance().build("/module1/test").withString("msg",
                "从MainActivity").navigation();
    }

    // 跳转模块2
    public void module2Jump(View view) {
        TgpRouter.getInstance().build("/module2/test").withString("msg",
                "从MainActivity").navigation();

//        Map a= new HashMap();
//        DNRouter$$Root$$app dnRouter$$Group$$main = new DNRouter$$Root$$app();
//        dnRouter$$Group$$main.loadInto(a);
//
//        a.get("main");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Main", requestCode + ":" + resultCode + ":" + data);
    }
}
