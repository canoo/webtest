package com.canoo.ant.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public abstract class ATableFilter implements ITableFilter {
    private String fForeignName;

    protected ATableFilter() {
    }

    protected ATableFilter(String foreignName) {
        setForeignName(foreignName);
    }

    public void setForeignName(String foreignName) {
    	if ("".equals(foreignName))
    		fForeignName = null;
    	else
    		fForeignName = foreignName;
    }

    public List filter(final List original, final String expected) {
        reset();
        final List result = new LinkedList();
        for (Iterator iter = original.iterator(); iter.hasNext();) {
            final Properties props = (Properties) iter.next();

            if (fForeignName != null) {
	            final String runningValue = props.getProperty(fForeignName, null);
	            if (matches(expected, runningValue)) {
	                result.add(props);
	                if (stopOnMatch()) 
	                	return result;
	            }
            }
            else {
                result.add(props);
            }
        }
        return result;
    }

    protected void reset(){
        // empty per default
    }

    protected abstract boolean matches(String expected, String runningValue);

    protected abstract boolean stopOnMatch();
}
