package com.yarolegovich.wellsample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.yarolegovich.wellsql.DefaultWellConfig;
import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.WellTableManager;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.mapper.SQLiteMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yarolegovich on 26.11.2015.
 */
public class WellConfig extends DefaultWellConfig {

    private static final List<Class<? extends Identifiable>> TABLES = new ArrayList<Class<? extends Identifiable>>() {{
        add(AntiHero.class);
        add(SuperHero.class);
        add(Villain.class);
    }};

    public WellConfig(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db, WellTableManager helper) {
        for (Class<? extends Identifiable> table : TABLES) {
            helper.createTable(table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, WellTableManager helper, int newVersion, int oldVersion) {
        for (Class<? extends Identifiable> table : TABLES) {
            helper.dropTable(table);
        }
        onCreate(db, helper);
    }

    /*
     * Here you can map classes to custom mappers. If you register here a mapper for class that already
     * has generated mapper - WellSql will use your mapper, but not generated.
     * No need to call super.registerMappers()
     */
    @Override
    protected Map<Class<?>, SQLiteMapper<?>> registerMappers() {
        return super.registerMappers();
    }

    @Override
    public int getDbVersion() {
        return 4;
    }

    @Override
    public String getDbName() {
        return "my_db";
    }
}
