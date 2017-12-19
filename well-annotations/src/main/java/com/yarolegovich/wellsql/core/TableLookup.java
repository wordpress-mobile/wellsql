package com.yarolegovich.wellsql.core;

import java.util.Set;

/**
 * Created by yarolegovich on 25.11.2015.
 */
public interface TableLookup {
    <T>Mapper<T> getMapper(Class<T> token);
    TableClass getTable(Class<? extends Identifiable> token);

    Set<Class<? extends Identifiable>> getMapperTokens();
    Set<Class<? extends Identifiable>> getTableTokens();
}
