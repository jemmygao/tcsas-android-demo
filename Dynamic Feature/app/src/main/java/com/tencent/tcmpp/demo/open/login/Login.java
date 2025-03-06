package com.tencent.tcmpp.demo.open.login;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.tcmpp.demo.TCMPPDemoApplication;
import com.tencent.tcmpp.demo.open.ResultCallback;
import com.tencent.tcmpp.demo.sp.BaseSp;

import java.util.List;

public class Login extends BaseSp {

    private static Login instance;

    public static Login g(Context context) {
        if (instance == null) {
            instance = new Login(context);
        }
        return instance;
    }

    private final String mAppId;
    LoginApi.UserInfo mUserInfo;

    public Login(Context context) {
        mAppId = getEnvAppId();
        mSharedPreferences = context.getSharedPreferences("tcmpp_auth_data_" + mAppId, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        getUserInfo();
    }

    public LoginApi.UserInfo getUserInfo() {
        if (mUserInfo == null) {
            String userInfo = getString(mSharedPreferences, "userInfo", "");
            if (!TextUtils.isEmpty(userInfo)) {
                mUserInfo = new Gson().fromJson(userInfo, LoginApi.UserInfo.class);
            }
        }
        return mUserInfo;
    }

    public boolean isMainProcess(Context context) {
        String processName = getProcessName(context);
        return processName != null && processName.equals(context.getPackageName());
    }

    private String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = manager.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.pid == pid) {
                    return process.processName;
                }
            }
        }
        return null;
    }

    public void saveUserInfo(LoginApi.UserInfo userInfo) {
        mUserInfo = userInfo;

        if (isMainProcess(TCMPPDemoApplication.sApp)) {
            putString(mEditor, "userInfo", new Gson().toJson(userInfo));
        }
    }

    public void deleteUserInfo() {
        mUserInfo = null;
        remove(mEditor, "userInfo");
    }

    public void login(String userId, String passwd, ResultCallback<LoginApi.UserInfo> callback) {
        LoginApi.INSTANCE.login(mAppId, userId, passwd, (i, s, userInfo) -> {
            if (i == 0) {
                saveUserInfo(userInfo);
            }
            callback.value(i, s, userInfo);
        });
    }

    public void getAuthCode(String miniAppId, ResultCallback<String> callback) {
        if (mUserInfo != null) {
            LoginApi.INSTANCE.getAuthCode(mAppId, miniAppId, mUserInfo.token, (i, s, s2) -> {
                if (i == LoginApi.ERROR_TOKEN) {
                    deleteUserInfo();
                }
                callback.value(i, s, s2);
            });
        } else {
            callback.value(-2, "login first", "");
        }
    }

    private String getEnvAppId(){
        return "app-83ye8rtj5j";
    }
}
