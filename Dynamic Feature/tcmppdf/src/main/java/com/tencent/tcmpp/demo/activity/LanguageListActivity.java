package com.tencent.tcmpp.demo.activity;

import static com.tencent.tcmpp.demo.activity.MainContentActivity.REQ_CODE_OF_LANGUAGE_LIST;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.tencent.tcmpp.demo.adapter.LanguageAdapter;
import com.tencent.tcmpp.demo.locale.LocaleHelper;
import com.tencent.tcmpp.demo.locale.LocaleManager;
import com.tencent.tcmpp.demo.sp.impl.CommonSp;
import com.tencent.tcmpp.df.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

;

public class LanguageListActivity extends AppCompatActivity {
    private String mCurrentLocale;
    private String mSelectedLocale;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        SplitCompat.installActivity(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_list);
        splitInstallManager = SplitInstallManagerFactory.create(this);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        LocaleHelper.updateLocal(this, LocaleManager.current().toLanguageTag());

        mCurrentLocale = LocaleManager.SUPPORTED_LOCALES[CommonSp.getInstance().getMiniLanguage()].getLanguage();
        mSelectedLocale = mCurrentLocale;
        setLanguageList();
        addClickListen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setLanguageList();
    }

    private void addClickListen() {
        findViewById(R.id.iv_language_back_img).setOnClickListener(v -> {
            setResult();
            finish();
        });
    }

    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra("isLanguageChange", !mCurrentLocale.equals(mSelectedLocale));
        setResult(REQ_CODE_OF_LANGUAGE_LIST, intent);
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }

    private void setLanguageList() {
        RecyclerView recyclerView = findViewById(R.id.rv_language_list);

        LanguageAdapter adapter = new LanguageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setOnLanguageChangeListener((old, newLocale) -> {
            mSelectedLocale = newLocale;
            requestInstallLanguage(mSelectedLocale);
        });
        recyclerView.setAdapter(adapter);
        adapter.update(getLanguageItems());

        TextView title = findViewById(R.id.tv_language_title);
        title.setText(getResources().getText(R.string.applet_main_tool_language));
    }

    private int installSession;
    private SplitInstallManager splitInstallManager;

    private boolean isLangInstalled(String targetLang) {
        Set<String> langSet = splitInstallManager.getInstalledLanguages();
        Log.e("Language", "installed lang = " + langSet);
        Log.e("Language", "selected lang = " + targetLang);
        return langSet.contains(targetLang);
    }

    private void requestInstallLanguage(String targetLang) {
        if (!isLangInstalled(targetLang)) {
            showLoading();
            // Creates a request to download and install additional language resources.
            SplitInstallRequest request =
                    SplitInstallRequest.newBuilder()
                            // Uses the addLanguage() method to include French language resources in the request.
                            // Note that country codes are ignored. That is, if your app
                            // includes resources for “fr-FR” and “fr-CA”, resources for both
                            // country codes are downloaded when requesting resources for "fr".
                            .addLanguage(Locale.forLanguageTag(targetLang))
                            .build();

            // Submits the request to install the additional language resources.
            splitInstallManager.startInstall(request)
                    .addOnSuccessListener(sessionId -> {
                        installSession = sessionId;
                    }).addOnFailureListener(exception -> {
                        hideLoading();
                        Toast.makeText(LanguageListActivity.this, "language install failed:" + exception, Toast.LENGTH_SHORT).show();
                    });
            ;
            splitInstallManager.registerListener(new SplitInstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(@NonNull SplitInstallSessionState splitInstallSessionState) {
                    if (installSession == splitInstallSessionState.sessionId()) {
                        if (splitInstallSessionState.status() == SplitInstallSessionStatus.INSTALLED) {
                            Toast.makeText(LanguageListActivity.this, "language install success", Toast.LENGTH_SHORT).show();
                            hideLoading();
                        }
                    }
                }
            });
        }
    }

    private ProgressDialog mProgressDialog;

    private void showLoading() {
        runOnUiThread(() -> {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(LanguageListActivity.this);
            }
            mProgressDialog.setCancelable(false);
            mProgressDialog.setTitle("installing language...");
            mProgressDialog.show();
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        });
    }

    private List<LanguageItem> getLanguageItems() {
        int current = CommonSp.getInstance().getMiniLanguage();
        ArrayList<LanguageItem> items = new ArrayList<>();
        for (int i = 0; i < LocaleManager.SUPPORTED_LOCALES.length; i++) {
            Locale locale = LocaleManager.SUPPORTED_LOCALES[i];
            LanguageItem item = new LanguageItem();
            item.locale = locale;
            item.selected = current == i;
            item.name = getResources().getStringArray(R.array.applet_language_name)[i];
            items.add(item);
        }
        return items;
    }

    public static class LanguageItem {
        public String name;
        public boolean selected = false;
        public Locale locale = null;
    }

}
