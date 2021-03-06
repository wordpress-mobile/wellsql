package com.yarolegovich.wellsql.mapper;

import android.content.ContentValues;
import android.database.Cursor;

import com.yarolegovich.wellsql.WellException;
import com.yarolegovich.wellsql.core.Mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yarolegovich on 25.11.2015.
 * Adapter class to bind mappers generated by annotation processor to
 * android specific classes: Cursor and ContentValues.
 */
public class MapperAdapter<T> implements SQLiteMapper<T> {

    private Mapper<T> mMapper;

    public MapperAdapter(Mapper<T> mapper) {
        mMapper = mapper;
    }

    @Override
    public ContentValues toCv(T item) {
        ContentValues cv = new ContentValues();
        Map<String, Object> vals = mMapper.toContentValues(item);
        for (String key : vals.keySet()) {
            Object obj = vals.get(key);
            if (obj instanceof String) {
                cv.put(key, (String) obj);
            } else if (obj instanceof Integer) {
                cv.put(key, (Integer) obj);
            } else if (obj instanceof Double) {
                cv.put(key, (Double) obj);
            } else if (obj instanceof byte[]) {
                cv.put(key, (byte[]) obj);
            } else if (obj instanceof Boolean) {
                cv.put(key, (Boolean) obj);
            } else if (obj instanceof Byte) {
                cv.put(key, (Byte) obj);
            } else if (obj instanceof Float) {
                cv.put(key, (Float) obj);
            } else if (obj instanceof Long) {
                cv.put(key, (Long) obj);
            } else if (obj instanceof Short) {
                cv.put(key, (Short) obj);
            } else if (obj == null) {
                cv.putNull(key);
            } else {
                throw new WellException("Type " + obj.getClass().getSimpleName() + " unsupported, failed to create ContentValues. " +
                        "Write custom converter for " + item.getClass().getSimpleName() + " and register " +
                        "it in WellConfig.");
            }
        }
        return cv;
    }

    @Override
    public T convert(Cursor item) {
        Map<String, Object> map = new HashMap<>();
        for (String column : item.getColumnNames()) {
            int columnIndex = item.getColumnIndex(column);
            switch (item.getType(columnIndex)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    map.put(column, item.getLong(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    map.put(column, item.getString(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    map.put(column, item.getDouble(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    map.put(column, item.getBlob(columnIndex));
                    break;
            }
        }
        return mMapper.convert(map);
    }
}
