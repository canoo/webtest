package com.canoo.ant.filter;

import java.util.List;

public interface ITableFilter {

    /**
     * @return a new List of Properties that matches the criteria of this specific filter
     */
    public List filter(List original, String propValue);

    void setForeignName(String foreignName);
}
