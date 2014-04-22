// Copyright 2005-2006. Release under the Canoo WebTest license
package com.canoo.webtest.self;

import com.agical.rmock.extension.junit.RMockTestCase;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Convenience class for webtest test cases which need to check log information.
 *
 * @author Marc Guillemot
 * @author Denis N. Antonioli
 */
public class LogCatchingTestCase extends RMockTestCase {
    private BufferingAppender fSpoofAppender;
    private Level fOldlevel;
    private Logger fLogger;
    private List fOldAppenders;

    protected void tearDownCatchLoggerMessages() {
        fSpoofAppender.close();
        fLogger.removeAppender(fSpoofAppender);

        for (Iterator iter = fOldAppenders.iterator(); iter.hasNext();) {
            fLogger.addAppender((Appender) iter.next());
        }
        fLogger.setLevel(fOldlevel);
    }

    protected void setUpCatchLoggerMessages() {
        fSpoofAppender = new BufferingAppender();
        fLogger = Logger.getRootLogger();

        fOldAppenders = new LinkedList();
        for (Enumeration allAppenders = fLogger.getAllAppenders(); allAppenders.hasMoreElements();) {
            final Appender appender = (Appender) allAppenders.nextElement();

            // remove without closing!
            fLogger.removeAppender(appender);
            fOldAppenders.add(appender);
        }
        fLogger.addAppender(fSpoofAppender);

        fOldlevel = fLogger.getLevel();
        fLogger.setLevel(Level.ERROR);
    }

    /**
     * Gets the spoof appender configured in {@link #setUpCatchLoggerMessages()}
     * to catch the log messages
     *
     * @return <code>null</code> if {@link #setUpCatchLoggerMessages()} was not called
     */
    protected BufferingAppender getSpoofAppender() {
        return fSpoofAppender;
    }

}
