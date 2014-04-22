package com.canoo.webtest.extension.groovy;

import com.canoo.webtest.steps.Step;

/**
 * 
 * @author Dierk König
 * @author Marc Guillemot
 * @deprecated since 11.2006. Use {@link groovy.util.AntBuilder} directly
 */
public class MacroStepBuilder extends groovy.util.AntBuilder {

	public MacroStepBuilder(final Step step) {
		super(step.getProject());
	}
}
