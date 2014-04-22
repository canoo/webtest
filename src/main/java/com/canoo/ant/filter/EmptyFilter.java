package com.canoo.ant.filter;

import java.util.ArrayList;
import java.util.List;

public class EmptyFilter implements ITableFilter {
    public List filter(List original, String propValue) {
        return new ArrayList(0);
    }

    public void setForeignName(String foreignName) {
        // nothing to do here
    }
}
