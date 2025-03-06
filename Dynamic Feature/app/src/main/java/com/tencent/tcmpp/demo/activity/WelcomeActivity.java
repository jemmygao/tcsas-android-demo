package com.tencent.tcmpp.demo.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.tencent.tcmpp.demo.R;
import com.tencent.tcmpp.demo.bean.GlobalConfigure;
import com.tencent.tcmpp.demo.open.ResultCallback;
import com.tencent.tcmpp.demo.open.login.Login;
import com.tencent.tcmpp.demo.open.login.LoginApi;
import com.tencent.tcmpp.demo.utils.GlobalConfigureUtil;
import com.tencent.tcmpp.demo.locale.LocaleManager;
import com.tencent.tcmpp.demo.locale.LocaleHelper;


public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "LOGIN";
    public static final String TCMPPDF = "tcmppdf";

    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    SplitInstallManager splitInstallManager;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, LocaleManager.current()));
        SplitCompat.installActivity(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splitInstallManager = SplitInstallManagerFactory.create(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.applet_activity_welcome);

        addLoginBtnListener();
        checkLogin();
        loadUIByGlobalConfigure();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            // Handle the user's decision. For example, if the user selects "Cancel",
            // you may want to disable certain functionality that depends on the module.
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private boolean isLogin(Context context) {
        return Login.g(context).getUserInfo() != null;
    }

    private void startMain() {
        hideLoading();
        Intent in = new Intent();
        in.setComponent(new ComponentName(getPackageName(), "com.tencent.tcmpp.demo.activity.MainContentActivity"));
        startActivity(in);
        finish();
    }

    private void addLoginBtnListener() {
        findViewById(R.id.btn_tcmpp_login_confirm).setOnClickListener(v -> loginWithUserName());
    }

    private ProgressDialog mProgressDialog;

    private void showLoading() {
        runOnUiThread(() -> {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(WelcomeActivity.this);
            }
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle("installing TCSAS dynamic feature...");
            mProgressDialog.show();
        });
    }

    private void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private boolean isTCMPPInstalled() {
        return splitInstallManager.getInstalledModules().contains(TCMPPDF);
    }

    private int installSession;
    private void installTCMPP() {
        if (isTCMPPInstalled()) {
            startMain();
        } else {
            showLoading();
            // Creates a request to install a module.
            SplitInstallRequest request = SplitInstallRequest.newBuilder()
                    // You can download multiple on demand modules per
                    // request by invoking the following method for each
                    // module you want to install.
                    .addModule(TCMPPDF).build();

            splitInstallManager
                    // Submits the request to install the module through the
                    // asynchronous startInstall() task. Your app needs to be
                    // in the foreground to submit the request.
                    .startInstall(request)
                    // You should also be able to gracefully handle
                    // request state changes and errors. To learn more, go to
                    // the section about how to Monitor the request state.
                    .addOnSuccessListener(sessionId -> {
                        installSession = sessionId;
                    }).addOnFailureListener(exception -> {
                        hideLoading();
                        Toast.makeText(WelcomeActivity.this, "tcmpp install failed:" + exception, Toast.LENGTH_SHORT).show();
                    });

            splitInstallManager.registerListener(splitInstallSessionState -> {
                if (splitInstallSessionState.sessionId() == installSession) {
                    if (splitInstallSessionState.status() == SplitInstallSessionStatus.INSTALLED) {
                        Log.e(TAG, "tcmpp install success");
                        startMain();
                    } else if (splitInstallSessionState.status() == SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION) {
                        splitInstallManager.startConfirmationDialogForResult(splitInstallSessionState,
                                // an activity result launcher registered via registerForActivityResult
                                activityResultLauncher);
                    }
                }
            });
        }
    }

    private void loginWithUserName() {
        String userName = ((EditText) findViewById(R.id.et_tcmpp_login_username)).getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "user name is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Login.g(this).login(userName, "123456", new ResultCallback<LoginApi.UserInfo>() {
            @Override
            public void value(int i, String s, LoginApi.UserInfo userInfo) {
                Log.d(TAG, "login return " + i + " s " + s + " userInfo " + userInfo);
                if (i == LoginApi.LOGIN_SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            installTCMPP();
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(WelcomeActivity.this, "login failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fillUpUserName() {
        LoginApi.UserInfo userInfo = Login.g(getApplicationContext()).getUserInfo();
        if (null != userInfo) {
            EditText editText = findViewById(R.id.et_tcmpp_login_username);
            editText.setText(userInfo.userName);
        }
    }

    private void checkLogin() {
            fillUpUserName();
            if (isLogin(this)) {
                installTCMPP();
            }
    }

    private void loadUIByGlobalConfigure() {
        GlobalConfigure globalConfigure = GlobalConfigureUtil.getGlobalConfig(getApplicationContext());
        if (null != globalConfigure) {
            if (null != globalConfigure.icon) {
                ImageView iconView = findViewById(R.id.iv_tcmpp_login_icon);
                iconView.setImageBitmap(globalConfigure.icon);
            }
            if (!TextUtils.isEmpty(globalConfigure.appName)) {
                TextView appNameTextView = findViewById(R.id.tv_tcmpp_login_title);
                appNameTextView.setText(globalConfigure.appName);
            }
            if (!TextUtils.isEmpty(globalConfigure.description)) {
                TextView appNameTextView = findViewById(R.id.tv_tcmpp_login_title_desc);
                appNameTextView.setText(globalConfigure.description);
            }
        }
    }
}















