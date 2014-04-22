/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.interfaces.IStepSequence;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.util.Iterator;
import java.util.Properties;

/**
 * @author Denis N. Antonioli
 */
public class ExecuteStepValidator implements IValidator {
    private final IStepSequence fStepSequence;

    public static final String KEY_DEPTH = "Depth";
    public static final String KEY_VERIFY = "Verify";

    public ExecuteStepValidator(final Context context, final IStepSequence stepSequence) {
        fStepSequence = stepSequence;
    }

    public Properties validate(final int depth, final HtmlPage htmlPage, final HtmlAnchor link) {
        final StringBuffer sb = new StringBuffer();
        int i = 0;
        for (final Iterator iter = fStepSequence.getSteps().iterator(); iter.hasNext();) {
            final Task step = (Task) iter.next();
            sb.append(i++);
            sb.append(", ");
            sb.append(getDescription(step));
            sb.append(": ");
            try {
                step.perform();
                sb.append("ok");
            } catch (final BuildException e) {
                sb.append(e.getMessage());
            }
            sb.append("\t");
        }

        final Properties linkInfo = new Properties();
        linkInfo.put(KEY_DEPTH, Integer.toString(depth));
        linkInfo.put(KEY_VERIFY, sb.toString());
        return linkInfo;
    }

    /**
     * Gets the description of the Step
     * @param step probably an {@link org.apache.tools.ant.UnknownElement}
     * @return the description
     */
	private String getDescription(final Task step) {
       	return (String) step.getRuntimeConfigurableWrapper().getAttributeMap().get("description");
	}
}
