// Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.NoSuchProviderException;

import org.apache.log4j.Logger;

import com.canoo.webtest.util.ConversionUtil;

/**
 * Helper class for processing email messages.
 *
 * @author Paul King
 * @author Luca Scheuring
 * @author lofi@mountproc.org
 */
public class EmailHelper
{
    private static final Logger LOG = Logger.getLogger(EmailHelper.class);

    /**
     * log into Email server and fetch mails (only headers)
     *
     * @return message array
     */
    Message[] getMessages(final Folder folder) throws MessagingException {
        // Get the message wrappers
        final Message[] msgs = folder.getMessages();
        LOG.debug("Login to Email server successful, " + msgs.length + " message(s) on server");
        return msgs;
    }

    Message getMessage(final int id, final Folder folder) throws MessagingException {
        // Get the message wrappers
        final Message[] msgs = getMessages(folder);
        for (int i = 0; i < msgs.length; i++) {
            if (msgs[i].getMessageNumber() == id) {
                return msgs[i];
            }
        }
        return null;
    }

    /**
     * Mark a message for deletion (message gets deleted when closing inbox)
     *
     * @param m message to be deleted
     */
    void markForDelete(final Message m) throws MessagingException {
        m.setFlag(Flags.Flag.DELETED, true);
        LOG.debug("Message " + m.getMessageNumber() + " marked for delete");
    }

    Folder getInboxFolder(final EmailConfigInfo info) throws MessagingException {
        int port = -1;
        String server = info.getServer();
        final int colonPosn = server.indexOf(":");
        if (colonPosn != -1) {
            port = extractPort(server, colonPosn);
            server = server.substring(0, colonPosn);
        }
        processDelayIfNeeded(info.getDelay());

        // Get the default session
        final Session session = getSessionInstance(System.getProperties());

        // Get an email message store, and connect to it
        final Store store = getStore(session, info.getType());
        store.connect(server, port, info.getUsername(), info.getPassword());

        // Try to get the default folder
        Folder folder = store.getDefaultFolder();
        if (folder == null) {
            throw new MessagingException("No default folder");
        }

        // ...and its INBOX
        folder = folder.getFolder("INBOX");
        if (folder == null) {
            throw new MessagingException("No INBOX");
        }

        // Open the folder with r/w-access
        folder.open(Folder.READ_WRITE);
        return folder;
    }

    private static int extractPort(final String server, final int colonPosn) throws MessagingException {
        final String portStr = server.substring(colonPosn + 1);
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new MessagingException("Port must be numeric, illegal value: " + portStr);
        }
    }

    private static void processDelayIfNeeded(final String delayStr) throws MessagingException {
        if (delayStr != null) {
            try {
                final int delay = ConversionUtil.convertToInt(delayStr, 0);
                Thread.sleep(delay * 1000);
            } catch (NumberFormatException nfe) {
                throw new MessagingException("Delay must be numeric, illegal value: " + delayStr);
            } catch (InterruptedException ie) {/* ignore */
            }
        }
    }

    protected Store getStore(final Session session, final String type) throws NoSuchProviderException {
        return session.getStore(type);
    }

    protected Session getSessionInstance(final Properties properties) {
        return Session.getInstance(properties);
    }

    /**
     * log out of email server. expunge if deleteOnServer is true
     */
    void logout(final Folder folder, final boolean deleteOnServer) {
        try {
            if (folder != null) {
                LOG.debug("closing INBOX...");
                folder.close(deleteOnServer);
            }
        } catch (MessagingException e) {
            LOG.warn("Failed to close INBOX folder.", e);
        }
    }
}

