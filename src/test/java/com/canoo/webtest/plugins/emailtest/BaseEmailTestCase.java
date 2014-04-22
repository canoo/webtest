// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.Message;

import com.canoo.webtest.steps.BaseStepTestCase;

/**
 * @author Paul King, ASERT
 */
public abstract class BaseEmailTestCase extends BaseStepTestCase
{
    private Folder fMockFolder;
    static final EmailConfigInfo MOCK_INFO = new EmailConfigInfo("a", "b", "c", "d", "e");

    protected Message setUpGetMessageExpectations(final ISingleMessageStep step, final EmailHelper helper,
                                                  final boolean closeOnExit) throws MessagingException {
        createMockFolder();
        final Message mockMessage = (Message) intercept(Message.class, "mockMessage");
        step.getContext().put("EmailConfigInfo", MOCK_INFO);
        final String mockMid = "99";
        step.setMessageId(mockMid);
        helper.getInboxFolder(MOCK_INFO);
        modify().returnValue(fMockFolder);
        helper.getMessage(Integer.parseInt(mockMid), fMockFolder);
        modify().returnValue(mockMessage);
        return mockMessage;
    }

    protected Message[] setUpGetMessagesExpectations(final AbstractSelectStep step, final EmailHelper helper,
                                                     final int numMessages) throws MessagingException {
        createMockFolder();
        final Message[] messages = new Message[numMessages];
        for (int i = 0; i < numMessages; i++) {
            messages[i] = (Message) intercept(Message.class, "mockMessage[" + i + "]");
        }
        step.getContext().put("EmailConfigInfo", MOCK_INFO);
        helper.getInboxFolder(MOCK_INFO);
        modify().returnValue(fMockFolder);
        helper.getMessages(fMockFolder);
        modify().returnValue(messages);
        return messages;
    }

    protected void setUpMessageOperationFinaliseExpectations(final EmailHelper helper, final boolean deleteOnServer) {
        helper.logout(fMockFolder, deleteOnServer);
    }

    protected Folder createMockFolder() throws MessagingException {
        return createMockFolder(createMockStore());
    }

    protected Folder createMockFolder(final Store store) throws MessagingException {
        fMockFolder = (Folder) intercept(Folder.class, new Object[]{store}, "folder");
        fMockFolder.getFullName();
        modify().multiplicity(expect.from(0)).returnValue("folder"); // helps rmock debug trace
        return fMockFolder;
    }

    protected Store createMockStore() {
        return (Store) intercept(Store.class,
                new Object[]{createSessionStub(), createUrlNameStub()}, "store");
    }

    private static URLName createUrlNameStub() {
        return new URLName("dummy url");
    }

    private static Session createSessionStub() {
        final Properties properties = new Properties();
        return Session.getDefaultInstance(properties);
    }

}
