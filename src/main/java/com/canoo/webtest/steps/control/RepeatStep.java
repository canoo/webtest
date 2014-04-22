// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import java.util.List;
import java.util.ListIterator;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.xpath.XPathHelper;

/**
 * A RepeatStep accepts one or more nested step elements and executes
 * them as many times as defined in the count attribute.
 * <p/>
 * Before it actually starts the execution of contained steps, the contained
 * steps are "expanded", i.e. cloned so that each invocation has
 * a dedicated step object. This is required since the step object serves
 * also as history element for logging results and execution times.
 * <p/>
 * As soon as one of the nested steps fails, the RepeatStep fails as
 * well (simply by propagating the TestStepFailed exception).
 * <p/>
 * It also updates a property #{count} with the current number of the
 * current repetition which can be accessed as a dynamic property if desired.
 *
 *
 * @author Carsten Seibert
 * @webtest.step
 *   category="Core"
 *   name="repeat"
 *   description="This step encapsulates one or more test steps that shall be repeated a certain number of times. 
 *   Any kind of step can be nested."
 */
public class RepeatStep extends MultipleExecutionStepContainer {
	private static final Logger LOG = Logger.getLogger(RepeatStep.class);
	private static final String DEFAULT_COUNTERNAME = "count";
	private Integer fCount;
	private int fStartCount;
    private Integer fEndCount;
    private int fStep = 1;
    private String fCounterName = DEFAULT_COUNTERNAME;
    private String fXpath;

    /**
     * @param count
     * @webtest.parameter
     *   required="yes/no"
     *   description="The number of times that the included steps shall be executed. Specify either count or endCount. Counter values start at <em>startCount</em> and go up to <em>startCount + count - 1</em>."
     */
    public void setCount(final Integer count) {
        fCount = count;
    }

    public Integer getCount() {
		return fCount;
	}

	/**
	 * @param count
	 * @webtest.parameter
	 *   required="no"
     *   default="0"
     *   description="The initial value of the count. Doesn't affect the number of iterations, just the value appearing in the counter."
     */
    public void setStartCount(final int count) {
        fStartCount = count;
    }

	public int getStartCount() {
		return fStartCount;
	}

    /**
	 * @param count
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The final value of the count. 
	 *   Specify either count or endCount.  Counter values start at <em>startCount</em> and go up to <em>endCount</em>."
	 */
	public void setEndCount(final Integer count) {
        fEndCount = count;
	}

	public Integer getEndCount() {
		return fEndCount;
	}

    /**
	 * @param step
	 * @webtest.parameter
	 *   required="no"
     *   default="1"
	 *   description="The step size of the count. 
	 *   Doesn't affect the number of iterations, just the value appearing in the counter."
	 */
	public void setStep(final int step) {
        fStep = step;
	}

	public int getStep() {
		return fStep;
	}

	/**
	 * @param counterName
	 * @webtest.parameter
	 *   required="no"
	 *   default="count"
	 *   description="The name that shall be used to reference the current repetition counter."
	 */
	public void setCounterName(final String counterName) {
		fCounterName = counterName;
	}

	public String getCounterName() {
		return fCounterName;
	}

    protected void verifyParameters() {
        super.verifyParameters();
        if (getStep() < 1) {
            throw new StepExecutionException("Step must be greater than or equal to 1!", this);
        }
        if (getCount() != null) {
            if (getCount().intValue() < 0) {
                throw new StepExecutionException("Repeat count must be greater than or equal to 0!", this);
            }
        }
        else if (getEndCount() != null) {
            if (getEndCount().intValue() < fStartCount) {
                throw new StepExecutionException("endCount ("+fEndCount+") must be greater than or equal to startCount (" + fStartCount + ")!", this);
            }
        }
        else if (getXpath() == null) {
            throw new StepExecutionException("You must specify a count, a endCount or a XPath attribute.", this);
        }
    }

	public void doExecute() throws XPathException
	{
		if (getXpath() != null)
			doExecuteWithXPath();
		else
		{
			final int first = getStartCount();
			final int last;
			if (getCount() != null)
				last = first + getCount().intValue();
			else
				last = getEndCount().intValue() + 1;
			
			for (int i=first; i<last; i+=getStep())
			{
	            setWebtestProperty(getCounterName(), Integer.toString(i));
	            executeContainedTasks(String.valueOf(i));
			}
		}
		// TODO: generate report for not executed steps too
	}
	
	protected List getNodesByXPath() throws XPathException
	{
		return getContext().getXPathHelper().selectNodes(getContext().getCurrentResponse(), getXpath());
	}

	protected void doExecuteWithXPath() throws XPathException
	{
		LOG.debug("repeat with xpath " + getXpath());
		final XPathHelper xpathHelper = getContext().getXPathHelper();
		final List nodes = getNodesByXPath();

		final int nbNodes = nodes.size();
		LOG.debug("Iterating over " + nbNodes + " nodes");
		for (final ListIterator iter = nodes.listIterator(); iter.hasNext();)
		{
			final Object node = iter.next();
			final String loopLabel = iter.nextIndex() + "/" + nbNodes;
			LOG.debug("Iteration " + loopLabel + ": placing current node >" 
					+ node + "< as >" + getCounterName() + "< in variable context");
			xpathHelper.getVariableContext().setVariableValue(new QName(getCounterName()), node);

			executeContainedTasks(loopLabel);
		}
	}

	protected void executeContainedTasks(final String loopLabel)
	{
        LOG.debug("creating wrapper for current iteration (" + getCounterName() + "): " + loopLabel);
		final Task iterationWrapper = createIterationWrapper("Iteration " + loopLabel);

		LOG.debug("execution wrapper for current iteration (" + getCounterName() + "): " + loopLabel);
		iterationWrapper.perform();
	}

	public String getXpath() {
		return fXpath;
	}

	/**
	 * @webtest.parameter
	 * 	required="yes/no"
	 *  description="Specifies the <key>XPATH</key> expression which evaluation gives the results on which to iterate"
	 */
	public void setXpath(final String xpath) 
	{
		fXpath = xpath;
	}

}
