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
    private static final long CURSOR_WINDOW_SIZE = 1024L * 1024L * 5L;

    WellCursor(SQLiteDatabase db, SelectMapper<T> mapper, Cursor cursor) {
        super(cursor);

        // Enlarge the cursor window size to avoid SQLiteBlobTooBigExceptions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, CURSOR_WINDOW_SIZE));
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
