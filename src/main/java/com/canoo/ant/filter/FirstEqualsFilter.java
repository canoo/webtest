package com.canoo.ant.filter;


/**
 * A Filter that matches the first Property object where the
 * propertyName matches the findValue
 */
public class FirstEqualsFilter extends ATableFilter {

    public FirstEqualsFilter() {
    }

    public FirstEqualsFilter(String foreignName) {
        super(foreignName);
    }

    protected boolean matches(String expected, String runningValue) {
        if (null == expected){
            return false;
        }
        return expected.equals(runningValue);
    }

    protected boolean stopOnMatch() {
        return true;
    }
}
