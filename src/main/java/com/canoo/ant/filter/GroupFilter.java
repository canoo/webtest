package com.canoo.ant.filter;

import java.util.LinkedList;
import java.util.List;


/**
 * A Filter that matches the first Property object
 * for each distinct propValue
 */
public class GroupFilter extends ATableFilter {

    List fGroup;

    public GroupFilter() {
    }

    public GroupFilter(String foreignName) {
        super(foreignName);
    }

    protected void reset() {
        fGroup = new LinkedList();
    }

    protected boolean matches(String expected, String runningValue) {
        if (fGroup.contains(runningValue)){
            return false;
        }
        fGroup.add(runningValue);
        return true;
    }

    protected boolean stopOnMatch() {
        return false;
    }
}
