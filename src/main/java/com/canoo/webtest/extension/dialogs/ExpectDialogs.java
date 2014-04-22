// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.boundary.AntBoundary;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractStepContainer;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.*;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;

import java.util.Iterator;

/**
 * Saves away one or more JavaScript dialog expectations in preparation for subsequent automatic user response(s).<p>
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="expectDialogs"
 * description="Provides the ability to set expectations in relation to user responses to <key>javascript</key> Dialog boxes (Alert, Confirm and Prompt dialogs).  Supports nested dialog steps such as <stepref name='alert'/>, <stepref name='confirm'/> and <stepref name='prompt'/>. See also the <stepref name='verifyNoDialogs'/> step."
 */
public class ExpectDialogs extends AbstractStepContainer
{
    private static final Logger LOG = Logger.getLogger(ExpectDialogs.class);

    public void doExecute() {
        final Context context = getContext();
        DialogHelper.clearExpectedDialogs(context);
        final WebClient wc = context.getWebClient();

        for (final Iterator iter = getSteps().iterator(); iter.hasNext();) {
        	final Step step = (Step) iter.next();
            DialogHelper.addExpectedDialog(context, (AbstractDialogStep) step);
            executeContainedStep(step);
        }
        final CheckingDialogHandler handler = new CheckingDialogHandler(context, this);

        wc.setAlertHandler(handler);
        wc.setConfirmHandler(handler);
        wc.setPromptHandler(handler);
        LOG.debug("Dialog expectation saved - now expecting "
                + DialogHelper.getExpectedDialogsCount(context) + " dialog(s).");
    }

	public void addTask(final Task newTask) {
        final Task task = AntBoundary.maybeConfigure(newTask);
        if (task instanceof AbstractDialogStep) {
            super.addTask(task);
        } else {
        	throw new StepFailedException("Not a dialog step: " + task);
        }
    }

    static class CheckingDialogHandler implements AlertHandler, ConfirmHandler, PromptHandler
    {
        private final Context fContext;
        private final Step fOuter;

        CheckingDialogHandler(final Context context, final Step outer) {
            this.fContext = context;
            this.fOuter = outer;
        }

        public void handleAlert(final Page page, final String message) {
        	checkContext();
            checkDialog(message, "AlertDialog");
        }

        /**
         * Checks that the context information is available for this thread
         * indeed dialog handler may be called back from HtmlUnit in a different thread
         */
        private void checkContext()
		{
			if (WebtestTask.getThreadContext() == null)
			{
				WebtestTask.setThreadContext(fContext);
			}
		}

		public boolean handleConfirm(final Page page, final String message) {
        	checkContext();
            final AbstractDialogStep thisDialog = checkDialog(message, "ConfirmDialog");
            return ConversionUtil.convertToBoolean(thisDialog.getResponse(), true);
        }

        public String handlePrompt(final Page page, final String message) {
        	checkContext();
            final AbstractDialogStep thisDialog = checkDialog(message, "PromptDialog");
            return thisDialog.getResponse();
        }

        private AbstractDialogStep checkDialog(final String message, final String dialogType) {
            checkExpectationsSet(fContext);
            final AbstractDialogStep thisDialog = DialogHelper.getNextExpectedDialog(fContext);
            saveResponse(thisDialog, message);
            checkResponseType(thisDialog, dialogType);
            thisDialog.verify(message);
            return thisDialog;
        }

        private void checkExpectationsSet(final Context context) {
            if (DialogHelper.getExpectedDialogsCount(context) == 0) {
                throw new StepFailedException("Expected dialogs but none found!", fOuter);
            }
        }

        private void saveResponse(final AbstractDialogStep thisDialog, final String message) {
            if (thisDialog.getSaveProperty() != null) {
                fOuter.setWebtestProperty(thisDialog.getSaveProperty(), message,
                        thisDialog.getSavePropertyType());
            }
        }

        private void checkResponseType(final AbstractDialogStep thisDialog, final String dialogType) {
            final String name = ClassUtils.getShortClassName(thisDialog.getClass());
            final String prefix = name.substring(0, name.length() - 4);
            if (!prefix.equals(dialogType)) {
                throw new StepFailedException("Incorrect dialog type", dialogType, prefix, fOuter);
            }
        }
    }
}
