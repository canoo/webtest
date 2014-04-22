package com.canoo.ant.table;

import com.canoo.ant.filter.ITableFilter;

import java.io.File;
import java.util.Properties;

public class TableFactory {
    public static final String KEY_TABLE = "table";
    public static final String KEY_NAME = "name";
    public static final String KEY_TABLE_CLASS = "tableClass";
    public static final String KEY_CONTAINER = "container";
    public static final String KEY_FOREIGN_TABLE = "foreignTable";
    public static final String KEY_FOREIGN_NAME = "foreignName";
    public static final String KEY_FILTER_CLASS = "filterClass";
    public static final String KEY_PREFIX = "prefix";

    private static final String TABLE_PACKAGE = "com.canoo.ant.table.";
    private static final String TABLE_SUFFIX = "PropertyTable";
    private static final String FILTER_PACKAGE = "com.canoo.ant.filter.";
    private static final String FILTER_SUFFIX = "Filter";
    private static final String DEFAULT_FILTER = "FirstEquals";

    public static void initOrDefault(IPropertyTable table, ITableFilter filter, Properties colInfo, final File container, String key) {
        // foreign Name defaults to the current name since it is usual to name
        // columns identically that are used for the "join"
        String foreignName = colInfo.getProperty(KEY_FOREIGN_NAME, key);
        if ("".equals(foreignName)) {
        	foreignName = key;
        }
        // if foreignName is of format table.col, use table as default
        String foreignTable = null;
        if (foreignName != null) {
            int delimPos = foreignName.indexOf(".");
            if (delimPos > -1) {
                foreignTable = foreignName.substring(0, delimPos);
            }
        }
        foreignTable = colInfo.getProperty(KEY_FOREIGN_TABLE, foreignTable);
        table.setPrefix(colInfo.getProperty(KEY_PREFIX, null));
        table.setContainer(container);
        table.setTable(foreignTable);
        filter.setForeignName(foreignName);
        table.setFilter(filter);
    }

    public static IPropertyTable createTable(Properties colInfo, String defaultTableClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String tableClass = colInfo.getProperty(KEY_TABLE_CLASS, defaultTableClass);
        if ("".equals(tableClass)) {
        	tableClass = defaultTableClass;
        }
        else if (!isFullyQualified(tableClass)) {
            tableClass = TABLE_PACKAGE + tableClass + TABLE_SUFFIX;
        }
        return (IPropertyTable) Class.forName(tableClass).newInstance();
    }


    public static ITableFilter createFilter(Properties colInfo) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String filterClass = colInfo.getProperty(KEY_FILTER_CLASS, DEFAULT_FILTER);
        if (!isFullyQualified(filterClass)) {
            filterClass = FILTER_PACKAGE + filterClass + FILTER_SUFFIX;
        }
        return (ITableFilter) Class.forName(filterClass).newInstance();
    }

    private static boolean isFullyQualified(String possibleClassName) {
        return possibleClassName.indexOf(".") > -1;
    }
}
