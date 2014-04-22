// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.reporting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;

import com.canoo.webtest.boundary.PackageBoundary;

/**
 * @author unknown
 * @author Marc Guillemot
 */
public class PlainTextReporter implements IResultReporter {

    public static String getBuildFailMessage(final RootStepResult result) {
        final StringBuffer sb = new StringBuffer();
        sb.append(PackageBoundary.versionMessage()).append("\nTest failed.");

        if (result.isError()) {
            sb.append("\nException raised: ");
            sb.append(result.getException());
            final ByteArrayOutputStream bs = new ByteArrayOutputStream();

            result.getException().printStackTrace(new PrintStream(bs));
            sb.append(bs.toString());
        }
        if (result.isFailure()) {
            final StepResult failedStepResult = result.getFailingTaskResult();

            sb.append("\nTest step ");
            sb.append(result.getTaskName());
            sb.append(" (").append(failedStepResult.getLocation()).append(") ");
                sb.append(failedStepResult.getTaskDescription());
            sb.append(" failed with message \"");
            sb.append(result.getException().getMessage());
            sb.append("\"");
        }
        return sb.toString();
    }

    public void generateReport(final RootStepResult result) throws IOException {
        final File outputFile = new File(result.getConfig().getWebTestResultDir(), "WebTestReport.txt");
        final PrintWriter out = new PrintWriter(new FileOutputStream(outputFile));

        print(out, result);
        out.close();
    }

    protected void print(final PrintWriter out, final RootStepResult result) {
        out.println(
                "Test step summary for specification: \""
                        + result.getWebtestName() + "\"");
        out.println(PackageBoundary.versionMessage());

        int count = 1;

        for (final Iterator i = result.getChildren().iterator(); i.hasNext();)
        {
            final StepResult stepResult = (StepResult) i.next();

            out.println("\t" + count++ + ". " + stepResult.getTaskName());
            if (stepResult.isCompleted()) {
                out.println("\t\t" + "Duration: " + stepResult.getDuration() + " ms");
            } else {
                out.println("\t\t==> Step was not completed!");
            }
            out.println();
        }
        if (!result.isSuccessful()) {
            out.println(getBuildFailMessage(result));
        }
    }
}
