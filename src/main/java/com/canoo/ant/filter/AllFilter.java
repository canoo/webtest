package com.canoo.ant.filter;

import java.util.ArrayList;
import java.util.List;

public class AllFilter implements ITableFilter {
    public List filter(List original, String propValue) {
        return new ArrayList(original);
    }

    public void setForeignName(String foreignName) {
        // nothing to do here
    }
}
