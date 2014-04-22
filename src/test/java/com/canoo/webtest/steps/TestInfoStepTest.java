package com.canoo.webtest.steps;


/**
 * Tests for {@link TestInfoStep}. In fact nothing to test as this step does nothing ;-)
 * @author Marc Guillemot
 */
public class TestInfoStepTest extends BaseStepTestCase {

	protected Step createStep() 
	{
		return new TestInfoStep();
	}
	
	public void testSimple()
	{
		final TestInfoStep step = (TestInfoStep) getStep();
		step.setType("foo");
		assertEquals("foo", step.getType());
		
		step.setInfo("foo1");
		assertEquals("foo1", step.getInfo());
		
		step.setInfo("foo1");
		assertEquals("foo1", step.getInfo());
		
		step.addText("bla");
		assertEquals("foo1bla", step.getInfo());
	}

	public void testVerifyParameters()
	{
		assertStepRejectsEmptyParam("type", getExecuteStepTestBlock());
	}
}
