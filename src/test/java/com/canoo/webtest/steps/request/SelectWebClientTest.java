package com.canoo.webtest.steps.request;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.WebClientContext;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * Test class for {@link SelectWebClient}.
 * @author Marc Guillemot
 */
public class SelectWebClientTest extends BaseStepTestCase
{
	protected Step createStep() {
        return new SelectWebClient();
    }

    public void testVerifyNameParameterValid() {
        assertStepRejectsEmptyParam("name", getExecuteStepTestBlock());
    }
    
    public void testExecution()
    {
    	final SelectWebClient step = (SelectWebClient) getStep();

    	final WebClientContext wccDefault = step.getContext().getCurrentWebClientContext();
    	step.setName(Context.KEY_DEFAULT_WEBCLIENTCONTEXT);
    	step.execute();
    	assertSame("activating default should have no effect", 
    			wccDefault, step.getContext().getCurrentWebClientContext());
    	
    	step.setName("other");
    	step.execute();
    	final WebClientContext wccOther = step.getContext().getCurrentWebClientContext();
    	assertNotSame(wccDefault, wccOther);
    	
    	step.setName(Context.KEY_DEFAULT_WEBCLIENTCONTEXT);
    	step.execute();
    	step.execute();
    	assertSame(wccDefault, step.getContext().getCurrentWebClientContext());

    	step.setName("other");
    	step.execute();
    	assertSame(wccOther, step.getContext().getCurrentWebClientContext());
    }
}
