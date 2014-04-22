// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.control;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractStepContainer;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Carsten Seibert
 * @author Aatish Arora
 * @author Jeanie Graham
 * @author Paul King
 * @author Gerald Klopp
 * @webtest.step category="Extension"
 * name="ifStep"
 * description="Step which allows conditional execution of inner steps."
 */
public class IfStep extends AbstractStepContainer {
    private static final Logger LOG = Logger.getLogger(IfStep.class);

    private String fTest;
    private String fUnless;
    private GroupStep fCondition;
    private GroupStep fThen;
    private GroupStep fElse;

    /**
     * @param test
     * @webtest.parameter required="yes/no"
     * description="Expression which if <em>true</em> should cause inner steps to be executed. One of <em>test</em> or <em>unless</em> should be set unless nested <em>condition</em> is used."
     */
    public void setTest(final String test) {
        fTest = test;
    }

    public String getTest() {
        return fTest;
    }

    /**
     * @param unless
     * @webtest.parameter required="yes/no"
     * description="Expression which if <em>true</em> should cause inner steps NOT to be executed. One of <em>test</em> or <em>unless</em> should be set unless nested <em>condition</em> is used."
     */
    public void setUnless(final String unless) {
        fUnless = unless;
    }

    public String getUnless() {
        return fUnless;
    }

    /**
     * Handles the nested 'condition' tag.<p>
     *
     * If all the steps contained in the 'condition' tag succeed, the 'ifStep' tag inner steps are executed
     *
     * @param condition The group of steps to execute to evaluate the test condition
     * @webtest.nested.parameter required="yes/no"
     * description="Group of steps which if <em>successful</em> should cause inner steps to be executed. Use instead of <em>test</em> or <em>unless</em> for more complex conditions."
     */
    public void addCondition(final GroupStep condition) {
        fCondition = condition;
    }

    public GroupStep getCondition() {
        return fCondition;
    }

    /**
     * Handles the nested 'then' tag.<p>
     *
     * If the 'ifStep' condition passes, execute all the steps in the 'then' inner group.
     *
     * @param then The group of steps to execute if the condition passes
     * @webtest.nested.parameter required="no"
     * description="Group of steps which execute if the 'ifStep' condition passes. If neither 'then' or 'else' are explicitly set, any inner steps are treated as if they were in an implicit 'then' step."
     */
    public void addThen(final GroupStep then) {
        paramCheck(fThen != null, "Only one nested 'then' step is supported.");
        fThen = then;
    }

    public GroupStep getThen() {
        return fThen;
    }

    /**
     * Handles the nested 'else' tag.<p>
     *
     * If the 'ifStep' condition fails, execute all the steps in the 'else' inner group.
     *
     * @param elseStep The group of steps to execute if the condition fails
     * @webtest.nested.parameter required="no"
     * description="Group of steps which execute if the 'ifStep' condition fails. If neither 'then' or 'else' are explicitly set, any inner steps are treated as if they were in an implicit 'then' step."
     */
    public void addElse(final GroupStep elseStep) {
        paramCheck(fElse != null, "Only one nested 'else/otherwise' step is supported.");
        fElse = elseStep;
    }

    public GroupStep getElse() {
        return fElse;
    }

    /**
     * Handles the nested 'otherwise' tag.<p>
     *
     * If the 'ifStep' condition fails, execute all the steps in the 'otherwise' inner group.
     * An alias for 'else'; sometimes more convenient name when used from scripting languages,
     * e.g. Groovy, where else is a keyword and must otherwise have single quotes around it.
     *
     * @param otherwiseStep The group of steps to execute if the condition fails
     * @webtest.nested.parameter required="no"
     * description="Alias for else step."
     */
    public void addOtherwise(final GroupStep otherwiseStep) {
        addElse(otherwiseStep);
    }

    public GroupStep getOtherwise() {
        return fElse;
    }


    /**
     * Execute all of the nested steps according to some condition.<p>
     *
     * The nested steps will be executed only if one of these conditions is true:<br/>
     * The 'test' property evaluates to true<br/>
     * All the steps contained in the nested 'test' tag succeed<br/>
     * The 'unless' property evaluates to false<br/>
     * One of the steps contained in the nested 'unless' tag fails<br/>
     * specifically:
     * <p/>
     * <table>
     * <tr><th>'test' property</th><th>'unless' property</th><th>nested 'condition' tag</th><th>execute?</th></tr>
     * <tr><td>true</td>          <td>missing</td>          <td>missing</td>                <td>yes</td></tr>
     * <tr><td>false</td>         <td>missing</td>          <td>missing</td>                <td>no</td></tr>
     * <tr><td>missing</td>       <td>missing</td>          <td>all tests succeed</td>      <td>yes</td></tr>
     * <tr><td>missing</td>       <td>missing</td>          <td>one test fails</td>         <td>no</td></tr>
     * <tr><td>missing</td>       <td>true</td>             <td>missing</td>                <td>no</td></tr>
     * <tr><td>missing</td>       <td>false</td>            <td>missing</td>                <td>yes</td></tr>
     * </table>
     * In other cases, an error occurs
     *
     * @throws com.canoo.webtest.engine.StepFailedException
     *          Raises this exception if one of the wrapped steps fails
     */
    public void doExecute() throws CloneNotSupportedException {
        boolean shouldRunSteps = runNestedTests();
        if (!shouldRunSteps) {
            if (getElse() != null) {
                executeContainedStep(getElse());
            }
            return;
        }
        if (getThen() != null) {
            executeContainedStep(getThen());
        } else if (getElse() == null) {
            executeContainedSteps();
        }
    }

    protected void verifyParameters() {
        super.verifyParameters();
        int testParamsNr = 0;
        if (StringUtils.isNotEmpty(getTest())) {
            testParamsNr++;
        }
        if (StringUtils.isNotEmpty(getUnless())) {
            testParamsNr++;
        }
        if (getCondition() != null) {
            testParamsNr++;
        }

        paramCheck(testParamsNr == 0, "One of the 'test' or the 'unless' attributes or nested tags is required.");
        paramCheck(testParamsNr > 1, "Only one of the 'test' or the 'unless' attributes or nested tags allowed.");
        if (fThen != null || fElse != null) {
            paramCheck(getSteps().size() > 0,
                    "When using 'then' and 'else/otherwise', nested steps most only be grouped inside the 'then' or 'else/otherwise' steps.");
        }
    }

    protected boolean runNestedTests() {
        if (StringUtils.isNotEmpty(getTest())) {
            return ConversionUtil.convertToBoolean(getTest(), false);
        }
        if (StringUtils.isNotEmpty(getUnless())) {
            return !ConversionUtil.convertToBoolean(getUnless(), false);
        }
        try {
            executeContainedStep(getCondition());
        } 
        catch (final BuildException e) {
        	if (StepFailedException.isCausedByStepFailedException(e))
        	{
	            LOG.debug("test failed");
	            return false;
        	}
        	else
        	{
        		LOG.debug("BuildException not caused by a StepFailedException. Rethrowing.");
        		throw e;
        	}
        }

        return true;
    }

}
