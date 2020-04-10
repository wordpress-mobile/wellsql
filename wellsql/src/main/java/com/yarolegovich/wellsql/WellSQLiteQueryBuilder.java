package com.yarolegovich.wellsql;

import android.database.sqlite.SQLiteQueryBuilder;

import static com.yarolegovich.wellsql.SelectQuery.COLLATE_BINARY;
import static com.yarolegovich.wellsql.SelectQuery.COLLATE_NOCASE;
import static com.yarolegovich.wellsql.SelectQuery.COLLATE_RTRIM;

/**
 * Simple wrapper for SQLiteQueryBuilder that enables specifying the collation to use
 * for ordered queries
 */
public class WellSQLiteQueryBuilder extends SQLiteQueryBuilder {
    @SelectQuery.Collate private int mCollation = SelectQuery.COLLATE_NONE;

    public void setCollation(@SelectQuery.Collate int collation) {
        mCollation = collation;
    }

    @Override
    public String buildQuery(
            String[] projectionIn, String selection, String groupBy,
            String having, String sortOrder, String limit) {
        String query = super.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
        if (mCollation != SelectQuery.COLLATE_NONE) {
            String collateStr;
            switch (mCollation) {
                case COLLATE_BINARY:
                    collateStr = " COLLATE BINARY";
                    break;
                case COLLATE_NOCASE:
                    collateStr = " COLLATE NOCASE";
                    break;
                case COLLATE_RTRIM:
                    collateStr = " COLLATE RTRIM";
                    break;
                default:
                    collateStr = "";
                    break;
            }
            query = query.concat(collateStr);
        }
        return query;
    }
}
