package com.yarolegovich.wellsql;

import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.Nullable;

import com.yarolegovich.wellsql.mapper.SelectMapper;

/**
 * Created by yarolegovich on 01.12.2015.
 */
public class WellCursor<T> extends CursorWrapper {

    private SQLiteDatabase mDb;
    private SelectMapper<T> mMapper;

    WellCursor(SQLiteDatabase db, SelectMapper<T> mapper, Cursor cursor) {
        super(cursor);

        // Enlarge the cursor window size if the configuration value is set to avoid
        // SQLiteBlobTooBigExceptions (default is 2MB). Note that memory is dynamically
        // allocated as data rows are added to the window.
        long cursorWindowSize = WellSql.mDbConfig.getCursorWindowSize();
        if (cursorWindowSize > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, cursorWindowSize));
        }

        mDb = db;
        mMapper = mapper;
    }

    @Override
    public void close() {
        super.close();
    }

    @Nullable
    public T next() {
        Cursor cursor = getWrappedCursor();
        return cursor.moveToNext() ? mMapper.convert(cursor) : null;
    }
}
