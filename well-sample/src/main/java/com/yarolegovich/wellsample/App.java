package com.yarolegovich.wellsample;

import android.app.Application;

import com.yarolegovich.wellsql.WellSql;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yarolegovich on 26.11.2015.
 */
public class App extends Application {
    public final static Set<String> ENABLED_ADDONS = new HashSet<String>() {{
            add(ConfigUtils.ADDON_NAME_ALT);
    }};

    @Override
    public void onCreate() {
        super.onCreate();
        WellSql.init(new WellConfig(this, ENABLED_ADDONS));
    }
}
