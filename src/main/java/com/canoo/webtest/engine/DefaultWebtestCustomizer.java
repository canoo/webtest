package com.canoo.webtest.engine;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.interfaces.IWebtestCustomizer;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * 
 * @author marc
 *
 */
public class DefaultWebtestCustomizer implements IWebtestCustomizer {

	public StepExecutionListener createExecutionListener(WebtestTask wt) {
		
		return new StepExecutionListener(wt.getConfig().getContext());
	}

	public WebClient customizeWebClient(WebClient wc) {
		
		return wc;
	}

}
