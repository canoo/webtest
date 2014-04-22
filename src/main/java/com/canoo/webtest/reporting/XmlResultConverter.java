// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.xerces.util.XMLChar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.canoo.webtest.boundary.PackageBoundary;
import com.canoo.webtest.engine.NameValuePair;
import com.canoo.webtest.engine.WebTestException;
import com.canoo.webtest.steps.HtmlParserMessage;

/**
 * The class converts a given {@link com.canoo.webtest.reporting.StepResult} into its XML representation. 
 * It is used to decouple report file handling ({@link XmlReporter}) and report content generation (this class).
 *
 * @author Marc Guillemot
 */
public class XmlResultConverter {

    // Constants for all used XML elements
    public static final String ROOT_ELEMENT = "summary";
    public static final String TESTRESULT_ELEMENT = "testresult";
    public static final String RESULTS_ELEMENT = "results";
    public static final String CONFIG_ELEMENT = "config";
    public static final String PARAMETER_ELEMENT = "parameter";
    public static final String STEP_ELEMENT = "step";
    public static final String FAILURE_ELEMENT = "failure";
    public static final String ERROR_ELEMENT = "error";
    public static final String STEP_RESULT_ATTRIBUTE = "result";
    public static final String STEP_COMPLETED_ELEMENT = "completed";
    public static final String STEP_FAILED_ELEMENT = "failed";
    public static final String STEP_NOTEXECUTED_ELEMENT = "notexecuted";

    private RootStepResult fResult;

    public XmlResultConverter(final RootStepResult result) {
        fResult = result;
    }

    /**
     * This method actually adds the XML representation of the test result
     * to the provided XML document. If the document is new (empty), it
     * creates the required root element. Then it appends a new
     * child to the root containing this result representation.
     *
     * @param doc An XML document into which the report for the current
     *            test result shall be added
     * @throws ReportCreationException Signals an inconsistency in the
     *                                 XML document
     */
    public void addToDocument(final Document doc) throws ReportCreationException {
        final NodeList nodeList = doc.getElementsByTagName(ROOT_ELEMENT);
        final Element rootElement;
        if (nodeList.getLength() == 0) {
            if (doc.getChildNodes().getLength() > 0) {
                throw new ReportCreationException("Another root already exists!");
            }
            rootElement = doc.createElement(ROOT_ELEMENT);
            doc.appendChild(rootElement);
            addSummaryAttribute(rootElement);
        } else {
            rootElement = (Element) nodeList.item(0);
        }
        final Element testResult = addNewElement(rootElement, TESTRESULT_ELEMENT);
        addResultAttributes(testResult);
        final String description = fResult.getWebtestDescription();
        if (!StringUtils.isEmpty(description)) {
            final Element descriptionElt = addNewElement(testResult, "description");
            addText(descriptionElt, description); // as node with text because extensions will come
        }
        addConfig(testResult);
        addResults(testResult);
    }

    private void addSummaryAttribute(final Element rootElement) {
        addNewAttribute(rootElement, "Implementation-Title",
            PackageBoundary.getImplementationTitle());
        addNewAttribute(rootElement, "Implementation-Version",
            PackageBoundary.getImplementationVersion());
    }

    private void addResults(final Element parent) {
        final Element resultsElement = addNewElement(parent, RESULTS_ELEMENT);

        addSteps(resultsElement, fResult.getChildren());
        addFailureIfNeeded(resultsElement);
        addErrorIfNeeded(resultsElement);
    }

    private void addErrorIfNeeded(final Element parent) {
        if (fResult.isError()) {
            final Element errorElement = addNewElement(parent, ERROR_ELEMENT);
            final Throwable error = fResult.getException();
            addNewAttribute(errorElement, "exception", error.getClass().getName());
            addFailureDetails(errorElement);
            
            // stacktrace
            final Element stackTrace = addNewElement(errorElement, "stacktrace");
            final StringWriter s = new StringWriter();
            error.printStackTrace(new PrintWriter(s));
            addText(stackTrace, s.toString());
        }
    }

    private void addFailureIfNeeded(final Element parent) {
        if (fResult.isFailure()) {
            final Element failureElement = addNewElement(parent, FAILURE_ELEMENT);
            addFailureDetails(failureElement);
        }
    }
    
    private void addFailureDetails(final Element failure) 
    {
        final Throwable throwable = fResult.getException();

        final List<Location> locations = new ArrayList<Location>();
        Throwable rootCause = throwable;
        Throwable ex = throwable;
        WebTestException webtestException = null;
        while (ex != null)
        {
        	rootCause = ex;
        	if (rootCause instanceof BuildException)
        		locations.add(((BuildException) rootCause).getLocation());
        	if (rootCause instanceof WebTestException)
        		webtestException = (WebTestException) rootCause;
        	ex = ex.getCause();
        }

        if (!locations.isEmpty())
        {
        	// call stack of ant calls (if any)
    		final Element antCallStack = addNewElement(failure, "antStack");
    		for (int i=locations.size()-1; i>=0; --i)
    		{
    			final Location loc = (Location) locations.get(i);
    			final Element antCall = addNewElement(antCallStack, "call");
    			antCall.setAttribute("filename", loc.getFileName());
    			antCall.setAttribute("line", String.valueOf(loc.getLineNumber()));
    		}
        }
        if (webtestException != null)
        {
        	addNewAttribute(failure, "message", removeInvalidChars(webtestException.getShortMessage()));

            for (final Iterator iter = webtestException.getDetails().iterator(); iter.hasNext();) {
				final NameValuePair detail = (NameValuePair) iter.next();
	            final Element detailElt = addNewElement(failure, "detail");
	            addNewAttribute(detailElt, "name", detail.getName());
	            addText(detailElt, detail.getValue());
			}
            addNewAttribute(failure, "currentResponse", webtestException.getUrlCurrentResponse());
        }
        else
        {
            addNewAttribute(failure, "message", removeInvalidChars(throwable.getMessage()));
        }
    }


    /**
     * Remove chars that will cause problems when written to an XML file.
     * This was occurring as of WebTest R1810 in the message of the Exception thrown by pdfVerifyText. 
     */
    private String removeInvalidChars(final String s) {
    	final StringBuilder sb = new StringBuilder(s);
    	for (int i=sb.length()-1; i>=0; --i) {
            int ch = sb.charAt(i);
            if (!XMLChar.isValid(ch)) {
            	sb.deleteCharAt(i);
            }
    	}
    	return sb.toString();
    }

	private void addSteps(final Element parent, final List steps) {
        for (final Iterator iter = steps.iterator(); iter.hasNext();) {
            addSingleTask(parent, (StepResult) iter.next());
        }
    }


    private void addSingleTask(final Element parent, final StepResult stepResult)
    {
        final Element stepElement = addNewElement(parent, STEP_ELEMENT);
        stepElement.setAttribute("taskName", stepResult.getTaskName());
        if (stepResult.getTaskDescription() != null) {
        	stepElement.setAttribute("description", stepResult.getTaskDescription());
        }
        final Location location = stepResult.getLocation();
        if (location != null) {
    		stepElement.setAttribute("_filename", location.getFileName());
    		stepElement.setAttribute("_line", String.valueOf(location.getLineNumber()));
        }

        addStepResult(stepElement, stepResult);

        final Map<String, String> attributes = new TreeMap<String, String>(stepResult.getAttributes());

        attributes.remove("description");
        final String resultFile = (String) attributes.remove("resultFilename");
        addParameters(stepElement, attributes); // add attributes bevor resultFile
        if (resultFile != null)
        {
            final Element resultFileElement = addNewElement(stepElement, "resultFile");
            resultFileElement.setAttribute("name", resultFile);
        }

        // html errors and warnings
        if (!stepResult.getHtmlParserMessages().isEmpty()) {
            final Element htmlParser = addNewElement(stepElement, "htmlparser");
            addParserMessages(htmlParser, stepResult.getHtmlParserMessages());
        }
        addSteps(stepElement, stepResult.getChildren());
    }

    /**
     * creates the subnodes for the parser messages
     *
     * @param htmlParser the parent node
     * @param messages   the list of {@link HtmlParserMessage}
     */
    private static void addParserMessages(final Element htmlParser, final List messages) {
        for (Iterator iter = messages.iterator(); iter.hasNext();) {
            final HtmlParserMessage msg = (HtmlParserMessage) iter.next();
            final Element elt = addNewElement(htmlParser,
                msg.getType().toString());
            elt.setAttribute("url", msg.getURL().toString());
            elt.setAttribute("line", String.valueOf(msg.getLine()));
            elt.setAttribute("col", String.valueOf(msg.getColumn()));
            elt.appendChild(htmlParser.getOwnerDocument().createTextNode(msg.getMessage()));
        }
    }

    private static void addStepResult(final Element parent, final StepResult step) {
        final long duration;
        final String result;
        if (step.isCompleted()) {
        	result = step.isSuccessful() ? STEP_COMPLETED_ELEMENT : STEP_FAILED_ELEMENT;
            duration = step.getDuration();
        } 
        else {
        	result = STEP_NOTEXECUTED_ELEMENT;
            duration = 0;
        }
        parent.setAttribute(STEP_RESULT_ATTRIBUTE, result);
        parent.setAttribute("duration", Long.toString(duration));
    }

    private void addConfig(final Element resultElement) {
        final Element configElement = addNewElement(resultElement, CONFIG_ELEMENT);
        addParameters(configElement,
            fResult.getConfig().getParameterDictionary());
    }

    private void addParameters(final Element parent, final Map parameterDictionary) 
    {
        for (final Iterator iter = parameterDictionary.entrySet().iterator(); iter.hasNext();) 
        {
            final Map.Entry parameter = (Map.Entry) iter.next();
            final Element parameterElement = addNewElement(parent, PARAMETER_ELEMENT);
            addNewAttribute(parameterElement, "name", parameter.getKey().toString());
            final String value = String.valueOf(parameter.getValue());
            addNewAttribute(parameterElement, "value", value);
        }
    }

    /**
     * PRE: there must be at least one user test step. 
     * This is asserted in {@link com.canoo.webtest.ant.TestStepSequence#doExecute()}.
     */
    private void addResultAttributes(final Element resultElement) {
        addNewAttribute(resultElement, "testspecname",
            fResult.getWebtestName());
        addNewAttribute(resultElement, "location", fResult.getWebtestLocation().toString());
        
        addNewAttribute(resultElement, "starttime", fResult.getStartDate().toString());
        if (fResult.isCompleted()) {
            addNewAttribute(resultElement, "endtime", fResult.getEndDate().toString());
        }
        addNewAttribute(resultElement, "successful", fResult.isSuccessful() ? "yes" : "no");
    }

    private static void addNewAttribute(final Element parent, final String name, final String value) {
        parent.setAttribute(name, value);
    }

    /**
     * Creates an element with the given name and adds it to the parent
     * @param parent the parent node
     * @param name the name of the element to create
     * @return the newly created element
     */
    private static Element addNewElement(final Node parent, final String name) {
        final Element e = parent.getOwnerDocument().createElement(name);
        parent.appendChild(e);
        return e;
    }

    /**
     * Creates a CDATA section with the provided text and adds it to the parent node
     * @param parent the parent node
     * @param text the text to add
     */
    private static void addText(final Node parent, final String text) {
        parent.appendChild(parent.getOwnerDocument().createCDATASection(text));
    }
}
