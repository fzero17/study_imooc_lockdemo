package com.helloandroid.lockdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * 密码碎片
 */
public class PasswordFragment extends Fragment implements LockDemoView.OnPatterChangeListener {
    private static final String ARG_TYPE = "type";

    public static final String TYPE_SETTING = "setting";
    public static final String TYPE_CHECK = "check";

    private LockDemoView mLockDemoView;
    private TextView mHint;
    //private LinearLayout mBtnLayout;
    private String passwordStr;
    private Button mBtnOk;


    public static PasswordFragment newInstance(String typeStr) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, typeStr);
        fragment.setArguments(args);
        return fragment;
    }

    public PasswordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.fragment_password, container, false);
        mHint = (TextView) contentView.findViewById(R.id.id_fragment_text_hint);

        mBtnOk = (Button) contentView.findViewById(R.id.id_btn_ok);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存密码
                SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                sp.edit().putString("password", passwordStr).commit();
                getActivity().finish();
            }
        });
        //设置密码
        if (getArguments() != null) {
            if (TYPE_SETTING.equals(getArguments().getString(ARG_TYPE))) {
                //mBtnLayout.setVisibility(View.VISIBLE);
                mBtnOk.setVisibility(View.VISIBLE);
                mHint.setText("请设置密码");
            }
        }
        mLockDemoView = (LockDemoView) contentView.findViewById(R.id.id_fragment_lock);
        mLockDemoView.setOnPatterChangeListener(this);

        return contentView;
    }

    @Override
    public void onPatterChange(String passwordStr) {
        this.passwordStr = passwordStr;
        if (TextUtils.isEmpty(passwordStr)) {
            mHint.setText("请至少绘制5个以上的图案");
        } else {
            mHint.setText(passwordStr);
            //密码检查
            if (getArguments() != null) {
                if (TYPE_CHECK.equals(getArguments().getString(ARG_TYPE))) {
                    SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                    if (passwordStr.equals(sp.getString("password", ""))) {
                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    } else {
                        mHint.setText("密码错误");
                        mLockDemoView.resetPoint();
                    }
                }
            }
        }
    }

    @Override
    public void onPatterStart(boolean isStart) {
        if (isStart) {
            mHint.setText("请绘制图案");
        }
    }
}
