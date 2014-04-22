// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.NoSuchProviderException;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.self.TestBlock;

/**
 * Test for {@link EmailHelper}.
 *
 * @author Paul King, ASERT
 */
public class EmailHelperTest extends BaseEmailTestCase
{
    private static final boolean DELETE_ON_SERVER = true;

    public void testLogout() throws Exception {
        final EmailHelper helper = new EmailHelper();
        final Folder folder = createMockFolder();
        folder.close(DELETE_ON_SERVER);
        startVerification();
        helper.logout(folder, DELETE_ON_SERVER);
    }

    public void testLogoutMessagingException() throws Exception {
        final EmailHelper helper = new EmailHelper();
        final Folder folder = createMockFolder();
        folder.close(DELETE_ON_SERVER);
        modify().throwException(new MessagingException("Dummy Messaging Exception"));
        startVerification();
        helper.logout(folder, DELETE_ON_SERVER);
    }

    public void testLogoutNull() throws Exception {
        final EmailHelper helper = new EmailHelper();
        helper.logout(null, DELETE_ON_SERVER);
    }

    public void testMarkForDelete() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        final Message message = (Message) intercept(Message.class, "message");
        message.setFlag(Flags.Flag.DELETED, true);
        startVerification();
        helper.markForDelete(message);
    }

    public void testGetMessages() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        final Folder folder = createMockFolder();
        folder.getMessages();
        modify().returnValue(new Message[]{});
        startVerification();
        helper.getMessages(folder);
    }

    public void testGetMessageNull() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        final Folder folder = createMockFolder();
        folder.getMessages();
        modify().returnValue(new Message[]{});
        startVerification();
        assertNull(helper.getMessage(0, folder));
    }

    public void testConfigInfo() {
        assertEquals("a", MOCK_INFO.getServer());
        assertEquals("b", MOCK_INFO.getType());
        assertEquals("c", MOCK_INFO.getUsername());
        assertEquals("d", MOCK_INFO.getPassword());
        assertEquals("e", MOCK_INFO.getDelay());
    }

    public void testGetMessageNonNull() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        final Folder folder = createMockFolder();
        final Message mockMessage1 = (Message) intercept(Message.class, "mockMessage1");
        final Message mockMessage2 = (Message) intercept(Message.class, "mockMessage2");
        mockMessage1.getMessageNumber();
        modify().returnValue(35);
        mockMessage2.getMessageNumber();
        modify().returnValue(42);
        folder.getMessages();
        modify().returnValue(new Message[]{mockMessage1, mockMessage2});
        startVerification();
        assertNotNull(helper.getMessage(42, folder));
    }

    public void testGetInboxFolderBadDelay() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        ThrowAssert.assertThrows("Delay must be numeric, illegal value: e",
                MessagingException.class,
                new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getInboxFolder(MOCK_INFO);
                    }
                });
    }

    public void testGetInboxFolderBadPort() throws MessagingException {
        final EmailHelper helper = new EmailHelper();
        ThrowAssert.assertThrows("Port must be numeric, illegal value: x",
                MessagingException.class,
                new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getInboxFolder(new EmailConfigInfo("a:x", null, null, null, null));
                    }
                });
    }

    public void testGetInboxFolderNoDefaultFolder() throws MessagingException {
        final EmailConfigInfo info = new EmailConfigInfo("a:25", "pop3", "c", "d", "0");
        final Store mockStore = createMockStore();
        mockStore.connect("a", 25, info.getUsername(), info.getPassword());
        mockStore.getDefaultFolder();
        modify().returnValue(null);
        final EmailHelper helper = prepareHelper(mockStore);
        startVerification();
        ThrowAssert.assertThrows(MessagingException.class, "No default folder",
                new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getInboxFolder(info);
                    }
                });
    }

    public void testGetInboxFolderNoInbox() throws MessagingException {
        final EmailConfigInfo info = new EmailConfigInfo("a", "pop3", "c", "d", null);
        final Store mockStore = createMockStore();
        final Folder mockFolder = createMockFolder(mockStore);
        mockFolder.getFolder("INBOX");
        modify().returnValue(null);
        mockStore.connect("a", -1, info.getUsername(), info.getPassword());
        mockStore.getDefaultFolder();
        modify().returnValue(mockFolder);
        final EmailHelper helper = prepareHelper(mockStore);
        startVerification();
        ThrowAssert.assertThrows(MessagingException.class, "No INBOX",
                new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getInboxFolder(info);
                    }
                });
    }

    public void testGetInboxFolder() throws MessagingException {
        final EmailConfigInfo info = new EmailConfigInfo("a", "pop3", "c", "d", null);
        final Store mockStore = createMockStore();
        final Folder mockFolder = createMockFolder(mockStore);
        mockFolder.getFolder("INBOX");
        modify().returnValue(mockFolder);
        mockFolder.open(Folder.READ_WRITE);
        mockStore.connect("a", -1, info.getUsername(), info.getPassword());
        mockStore.getDefaultFolder();
        modify().returnValue(mockFolder);
        final EmailHelper helper = prepareHelper(mockStore);
        startVerification();
        helper.getInboxFolder(info);
    }

    public void testHelperMethods() { // mainly for coverage
        final EmailHelper helper = new EmailHelper();
        ThrowAssert.assertThrows(NullPointerException.class, new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getSessionInstance(null);
                    }
                });
        ThrowAssert.assertThrows(NullPointerException.class, new TestBlock()
                {
                    public void call() throws Throwable {
                        helper.getStore(null, null);
                    }
                });
    }

    private static EmailHelper prepareHelper(final Store store) {
        return new EmailHelper()
        {
            protected Session getSessionInstance(final Properties properties) {
                return null;
            }
            protected Store getStore(final Session session, final String type) throws NoSuchProviderException {
                return store;
            }
        };
    }

    protected Step createStep() {
        return new EmailSetConfig(); // any dummy step
    }
}
