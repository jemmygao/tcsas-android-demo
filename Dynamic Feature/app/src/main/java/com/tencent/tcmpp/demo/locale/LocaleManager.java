package com.tencent.tcmpp.demo.locale;

import com.tencent.tcmpp.demo.sp.impl.CommonSp;

import java.util.Locale;


public class LocaleManager {
    public static final Locale[] SUPPORTED_LOCALES = {
            Locale.SIMPLIFIED_CHINESE,
            Locale.US,
            Locale.FRANCE,
            new Locale("id","ID"),
            new Locale("ar")
    };

    public static Locale current() {
        int index = CommonSp.getInstance().getMiniLanguage() % SUPPORTED_LOCALES.length;
        return (Locale) (SUPPORTED_LOCALES[index] == null
                ? Locale.getDefault()
                : SUPPORTED_LOCALES[index]).clone();
    }

    public static void setCurrentLocale(String tag) {
        for (int i = 0; i < SUPPORTED_LOCALES.length; i++) {
            if (SUPPORTED_LOCALES[i].getLanguage().equals(tag)) {
                CommonSp.getInstance().putMiniLanguage(i);
                break;
            }
        }
    }
}
