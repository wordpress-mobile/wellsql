package com.yarolegovich.wellsql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.yarolegovich.wellsql.core.Binder;
import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.TableClass;
import com.yarolegovich.wellsql.core.TableLookup;
import com.yarolegovich.wellsql.mapper.MapperAdapter;
import com.yarolegovich.wellsql.mapper.SQLiteMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yarolegovich on 26.11.2015.
 */
@SuppressWarnings("unchecked")
public abstract class DefaultWellConfig implements WellConfig,
        WellConfig.OnUpgradeListener, WellConfig.OnCreateListener, WellConfig.OnDowngradeListener,
        WellConfig.OnConfigureListener {

    private Context mContext;
    private Set<TableLookup> mGeneratedLookups = new HashSet<>();
    private Map<Class<?>, SQLiteMapper<?>> mMappers;

    protected Set<Class<? extends Identifiable>> mTables = new HashSet<>();

    public DefaultWellConfig(Context context) {
        this(context, Collections.<String>emptySet());
    }

    public DefaultWellConfig(Context context, Set<String> addOns) {
        mContext = context.getApplicationContext();
        mMappers = new HashMap<>();
        try {
            Class<? extends TableLookup> clazz = (Class<? extends TableLookup>)
                    Class.forName(Binder.PACKAGE + "." + Binder.LOOKUP_CLASS);
            mGeneratedLookups.add(clazz.newInstance());
        } catch (ClassNotFoundException e) {
            throw new WellException(mContext.getString(R.string.classes_not_found));
        } catch (Exception e) {
            /* This can't be thrown, because Binder.LOOKUP_CLASS always will be instantiated successfully */
        }

        for (String addOn : addOns) {
            String className = Binder.PACKAGE + "." + Binder.LOOKUP_CLASS + addOn;
            try {
                Class<? extends TableLookup> clazz = (Class<? extends TableLookup>) Class.forName(className);
                mGeneratedLookups.add(clazz.newInstance());
            } catch (ClassNotFoundException e) {
                throw new WellException(mContext.getString(R.string.addon_classes_not_found));
            } catch (Exception e) {
                throw new WellException("Unable to instantiate " + className);
            }
        }

        for (TableLookup lookup : mGeneratedLookups) {
            for (Class<?> token : lookup.getMapperTokens()) {
                mMappers.put(token, new MapperAdapter<>(lookup.getMapper(token)));
                mTables.add((Class<? extends Identifiable>) token);
            }
        }

        mMappers.putAll(registerMappers());
    }

    protected Map<Class<? extends Identifiable>, SQLiteMapper<?>> registerMappers() {
        return Collections.emptyMap();
    }

    @Override
    public OnUpgradeListener getOnUpgradeListener() {
        return this;
    }

    @Override
    public OnCreateListener getOnCreateListener() {
        return this;
    }

    @Override
    public OnDowngradeListener getOnDowngradeListener() {
        return this;
    }

    @Override
    public OnConfigureListener getOnConfigureListener() {
        return this;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public <T> SQLiteMapper<T> getMapper(Class<T> token) {
        return (SQLiteMapper<T>) mMappers.get(token);
    }

    @Override
    public TableClass getTable(Class<? extends Identifiable> token) {
        for (TableLookup lookup : mGeneratedLookups) {
            TableClass table = lookup.getTable(token);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, WellTableManager helper, int oldVersion, int newVersion) {
        throw new SQLiteException(mContext.getString(R.string.downgrade, oldVersion, newVersion));
    }

    @Override
    public void onConfigure(SQLiteDatabase db, WellTableManager helper) {
    }
}
