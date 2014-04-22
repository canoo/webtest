// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.UnknownElement;

import com.canoo.webtest.ant.TestStepSequence;
import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.xpath.SimpleXPathVariableResolver;
import com.canoo.webtest.self.StepStub;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 */
public class RepeatStepTest extends BaseWrappedStepTestCase {
	private static int sInvocationCounter;
    private RepeatStep fStep;

    public static class RepeatTestStub extends StepStub {
        public void doExecute() {
            sInvocationCounter++;
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (RepeatStep) getStep();
    }

	protected Step createStep() {
		return new RepeatStep();
	}

	public void testVerifyParameterValid() throws Exception {
		executeInContext(createSimpleRepeat(2, 0));
		executeInContext(createSimpleRepeat(2, 1));
		executeInContext(createSimpleRepeat(0, 0));
	}

	public void testZeroCount() throws Exception {
		sInvocationCounter = 0;
		executeInContext(createSimpleRepeat(1, 0));
		assertEquals("Should not have executed step", 0, sInvocationCounter);
	}

	public void testVerifyParameterInvalid() {
		final Throwable e = ThrowAssert.assertThrows("", BuildException.class, new TestBlock() {
			public void call() throws Exception {
				executeInContext(createSimpleRepeat(2, -1));
			}
		});
		
		assertInstanceOf(StepExecutionException.class, e);
	}

    public void testStepExansionWithEndCount() throws Exception {
        checkStepExpansion(new String[]{"0", "0", "0", "1", "1", "1", "2", "2","2","3","3","3","4","4","4","5","5","5"}, 3, null, "5", null);
    }

    public void testStepExansionWithStartAndEndCount() throws Exception {
        checkStepExpansion(new String[]{"4","4","4","4","5","5","5","5","6","6","6","6"}, 4, "4", "6", null);
    }

    public void testStepExansionWithStep() throws Exception {
        checkStepExpansion(new String[]{"4","4","6","6"}, 2, "4", "6", "2");
        checkStepExpansion(new String[]{"0"}, 1, null, "2", "3");
    }

    private void checkStepExpansion(final String[] expectedStepValues, final int stepCount, 
    		final String startCount, final String endCount, final String step) throws Exception {
        final RuntimeConfigurable wrapper = createRepeatStep(stepCount);
        if (startCount != null) {
            wrapper.setAttribute("startCount", startCount);
        }
        wrapper.setAttribute("endCount", endCount);
        if (step != null) {
            wrapper.setAttribute("step", step);
        }
        checkStepExpansion(wrapper, expectedStepValues);
    }

    public void testStepExpansion() throws Exception {
        final RuntimeConfigurable wrapper = createSimpleRepeat(2, 2);
        checkStepExpansion(wrapper, new String[]{ "0", "0", "1", "1"});
    }

    private void checkStepExpansion(final RuntimeConfigurable wrapper, final String[] expectedValues) throws Exception {
//        for(Iterator it = wrapper.getSteps().iterator(); it.hasNext();) {
//            final RepeatTestStub containedStep = (RepeatTestStub) it.next();
//            containedStep.fCountName = "#{count}";
//        }

        sInvocationCounter = 0;
        executeInContext(wrapper);

//        for(Iterator it = wrapper.getSteps().iterator(); it.hasNext();) {
//            final RepeatTestStub containedStep = (RepeatTestStub) it.next();
//            System.out.print(containedStep.fCountValue + ":");
//
//        }
        assertEquals("wrong invocation counter", expectedValues.length, sInvocationCounter);

/*        final List steps = wrapper.getSteps();

        int i = 0;
        assertEquals("Wrong number of steps after execution", expectedValues.length, steps.size());
        for (final Iterator it = steps.iterator(); it.hasNext(); i++) {
            final Step containedStep = (Step) it.next();
            if (i > 0) {
                assertTrue("elements 0 and "+i+" are identical", steps.get(0) != steps.get(i));
            }
            assertTrue("Contained step not started", containedStep.isStarted());
            assertTrue("Contained step not completed", containedStep.isCompleted());
            assertNotNull(containedStep.getProject());
            assertEquals("parameter in steps["+i+"] not correctly expanded", expectedValues[i],
               ((RepeatTestStub) steps.get(i)).fCountValue);
        }
*/    }
/*
	public void testNestedRepeatInvocation() throws Exception {
		int countInnerRepeat = 2;
		int countOuterRepeat = 3;


		final RepeatStep outerRepeat = (RepeatStep) getStep();

		outerRepeat.setCount(new Integer(countOuterRepeat));
		outerRepeat.setCounterName("y");
		buildContext(outerRepeat);

		RepeatTestStub step = new RepeatTestStub();
		step.setProject(outerRepeat.getProject());
		step.setDescription("step");
		step.fCountName = "#{x}:#{y}";

		final RepeatStep innerRepeat = new RepeatStep();
		innerRepeat.setProject(outerRepeat.getProject());
		innerRepeat.addStep(step);
		innerRepeat.setCount(new Integer(countInnerRepeat));
		innerRepeat.setCounterName("x");

		outerRepeat.addTask(innerRepeat);

		sInvocationCounter = 0;

		executeStep(outerRepeat);

		// Get the current step objects because they will have changed due to expanding of steps (multiplication)
		List outerSteps = outerRepeat.getSteps();
		List lastInnerSteps = ((AbstractStepContainer) outerSteps.get(outerSteps.size() - 1)).getSteps();
		RepeatTestStub lastStep = (RepeatTestStub) lastInnerSteps.get(lastInnerSteps.size() - 1);

		assertEquals("wrong number of steps in outer repeat", countOuterRepeat,
		   outerRepeat.getSteps().size());
		RepeatStep innerWrapper0 = (RepeatStep) outerRepeat.getSteps().get(0);
		RepeatStep innerWrapper1 = (RepeatStep) outerRepeat.getSteps().get(1);

		assertTrue("inner[0] and [1] are ==", innerWrapper0 != innerWrapper1);
		assertEquals("wrong number of steps in inner repeat[0]", countInnerRepeat,
		   innerWrapper0.getSteps().size());
		assertEquals("wrong number of steps in inner repeat[1]", countInnerRepeat,
		   innerWrapper1.getSteps().size());
		assertEquals("wrong number of invocations", countInnerRepeat * countOuterRepeat,
		   sInvocationCounter);
		assertEquals("inner parameter not expanded", "1:2", lastStep.fCountValue);
	}
*/

    public void testRejectsInvalidStepValues() {
        fStep.setStep(0);
        assertErrorOnExecute(fStep, "zero step", "Step must be greater than or equal to 1!");

    }

    public void testRejectsEndCountBeforeStartCount() {
        fStep.setStartCount(5);
        fStep.setEndCount(new Integer(4));
        assertErrorOnExecute(fStep, "end before start", "endCount (4) must be greater than or equal to startCount (5)!");
    }

    public void testCountOrEndCountRequired() {
        assertErrorOnExecute(fStep, "count or endcount", "You must specify a count, a endCount or a XPath attribute.");
    }

    private RuntimeConfigurable createSimpleRepeat(final int stepCount, final int repeatCount) {
        final RuntimeConfigurable wrapper = createRepeatStep(stepCount);
        wrapper.setAttribute("count", String.valueOf(repeatCount));
		return wrapper;
	}

    private RuntimeConfigurable createRepeatStep(final int stepCount) {
        final RuntimeConfigurable repeat = parseStep(RepeatStep.class, "");
        
        for (int i = 0; i < stepCount; i++) {
        	final RuntimeConfigurable repeatStub = parseStep(RepeatTestStub.class, "description='step " + i + "'");
        	repeat.addChild(repeatStub);
        }
        return repeat;
    }
    
    private void executeInContext(final RuntimeConfigurable step) throws Exception {
        buildContext(step);
        ((UnknownElement) step.getProxy()).perform();
    }

	private static void buildContext(final RuntimeConfigurable repeatstep) {
		final WebtestTask webtest = new WebtestTask();
		webtest.setName("testDummy");

		final Configuration testConfig = new Configuration();
		webtest.addConfig(testConfig);
		testConfig.setHost("myHost");
		
		final TestStepSequence steps = new TestStepSequence();
		steps.addTask((UnknownElement) repeatstep.getProxy());
		webtest.addSteps(steps);
		
		WebtestTask.setThreadContext(new Context(webtest));
	}
	
	public void testRepeatByXPath()
	{
		final List<Object> nodes = Arrays.asList(new Object[] { "foo", new Object(), new Integer(1) });
		final SimpleXPathVariableResolver variableContext = getContext().getXPathHelper().getVariableContext();
		
		final List<Object> actualNodes = new ArrayList<Object>();
		final RepeatStep step = new RepeatStep()
		{
			protected List getNodesByXPath()
			{
				return nodes; 
			}
			protected void executeContainedTasks(String _loopLabel)
			{
				final QName qname = new QName(getCounterName());
				actualNodes.add(variableContext.resolveVariable(qname));
			}
		};
		configureStep(step);
		step.setXpath("/my/xpath/selecting/some/nodes");
		
		step.execute();
		assertEquals(nodes, actualNodes);
	}
}
