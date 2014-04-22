package com.canoo.ant.filter;


/**
 * A Filter that matches all Property objects where the
 * propertyName matches the findValue
 */
public class AllEqualsFilter extends ATableFilter {

    public AllEqualsFilter() {
    }

    public AllEqualsFilter(String foreignName) {
        super(foreignName);
    }

    protected boolean matches(String expected, String runningValue) {
        if (null==expected){
            return false;
        }
        return expected.equals(runningValue);
    }

    protected boolean stopOnMatch() {
        return false;
    }
}
