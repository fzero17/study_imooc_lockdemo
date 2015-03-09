package com.helloandroid.lockdemo;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置密码
        getFragmentManager().beginTransaction().replace(
                android.R.id.content,PasswordFragment.newInstance(PasswordFragment.TYPE_SETTING)).commit();
   }
}
