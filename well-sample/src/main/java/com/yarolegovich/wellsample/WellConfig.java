package com.yarolegovich.wellsample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.yarolegovich.wellsql.DefaultWellConfig;
import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.WellTableManager;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.TableClass;
import com.yarolegovich.wellsql.mapper.SQLiteMapper;

import java.util.Map;
import java.util.Set;

/**
 * Created by yarolegovich on 26.11.2015.
 */
public class WellConfig extends DefaultWellConfig {
    private long mCursorSize = 0L;

    public WellConfig(Context context) {
        super(context);
    }

    public WellConfig(Context context, Set<String> addOns) {
        super(context, addOns);
    }

    @Override
    public void onCreate(SQLiteDatabase db, WellTableManager helper) {
        for (Class<? extends Identifiable> table : mTables) {
            helper.createTable(table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, WellTableManager helper, int newVersion, int oldVersion) {
        for (Class<? extends Identifiable> table : mTables) {
            helper.dropTable(table);
        }
        onCreate(db, helper);
    }

    @Override
    public void onConfigure(SQLiteDatabase db, WellTableManager helper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        } else {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /*
     * Here you can map classes to custom mappers. If you register here a mapper for class that already
     * has generated mapper - WellSql will use your mapper, but not generated.
     * No need to call super.registerMappers()
     */
    @Override
    protected Map<Class<? extends Identifiable>, SQLiteMapper<?>> registerMappers() {
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

    @Override
    public long getCursorWindowSize() {
        return mCursorSize;
    }

    public void setCursorWindowSize(long size) {
        mCursorSize = size;
    }

    public void reset() {
        SQLiteDatabase db = WellSql.giveMeWritableDb();
        for (Class<? extends Identifiable> clazz : mTables) {
            TableClass table = getTable(clazz);
            db.execSQL("DROP TABLE IF EXISTS " + table.getTableName());
            db.execSQL(table.createStatement());
        }
    }
}
