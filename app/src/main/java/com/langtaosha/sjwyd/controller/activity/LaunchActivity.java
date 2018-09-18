package com.langtaosha.sjwyd.controller.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.langtaosha.sjwyd.Client;
import com.langtaosha.sjwyd.Config;
import com.langtaosha.sjwyd.R;
import com.langtaosha.sjwyd.models.LoginProcess;
import com.langtaosha.sjwyd.models.Response;

public class LaunchActivity extends AppCompatActivity {

    private String userName;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // 验证用户保存的登录信息
        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
        userName = preferences.getString(Config.PRE_USER_NAME, "");
        password = preferences.getString(Config.PRE_PASSWORD, "");
        new UserLoginTask().execute();
    }


    // 用户登录验证
    private class UserLoginTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Response<LoginProcess> response = Client.getInstance().loginProcess(userName, password);
            if (response.getErrno() != 1) { // 验证失败，要求用户重新输入登录信息
                Intent intent = new Intent(LaunchActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {                        // 验证成功，进入应用程序
                SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Config.PRE_AVATAR_FILE, response.getRsm().getAvatar_file());
                editor.putString(Config.PRE_EMAIL, response.getRsm().getEmail());
                editor.apply();
                Intent intent = new Intent(LaunchActivity.this, DrawerActivity.class);
                startActivity(intent);
                finish();
            }
            return null;
        }
    }

}
