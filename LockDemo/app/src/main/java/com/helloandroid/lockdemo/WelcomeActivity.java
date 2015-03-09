package com.helloandroid.lockdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;


public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
                String passwordStr = sp.getString("password", "");
                //没有设置密码
                if (TextUtils.isEmpty(passwordStr)) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                } else {
                    //检查密码
                    getFragmentManager().beginTransaction().replace(
                            android.R.id.content, PasswordFragment.newInstance(PasswordFragment.TYPE_CHECK)).commit();
                }

            }
        }, 1000);
    }

}
