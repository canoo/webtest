package com.canoo.ant.table;

import com.canoo.ant.filter.ITableFilter;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IPropertyTable {
    /**
     * @return List of Property objects
     * @see java.util.Properties
     */
    public List getPropertiesList(String filterValue, String prefix);

    Map getColumnInfo();

    File getContainer();

    void setContainer(File container);

    String getTable();

    void setTable(String table);

    ITableFilter getFilter();

    void setFilter(ITableFilter filter);

    String getPrefix();

    void setPrefix(String prefix);
}
