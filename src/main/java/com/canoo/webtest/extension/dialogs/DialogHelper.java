// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.engine.Context;

import java.util.List;
import java.util.ArrayList;

/**
 * Used by Dialog steps
 *
 * @author Paul King
 */
public class DialogHelper
{
    private static final String EXPECTED_DIALOGS_KEY = "ExpectedDialogs";

    static int getExpectedDialogsCount(final Context context) {
        return getExpectedDialogs(context).size();
    }

    static void clearExpectedDialogs(final Context context) {
        getExpectedDialogs(context).clear();
    }

    static void addExpectedDialog(final Context context, final AbstractDialogStep abstractDialogStep) {
        getExpectedDialogs(context).add(abstractDialogStep);
    }

    static AbstractDialogStep getNextExpectedDialog(final Context context) {
        final List expectedDialogs = getExpectedDialogs(context);
        if (expectedDialogs.isEmpty()) {
            return null;
        }
        final AbstractDialogStep thisDialog = (AbstractDialogStep) expectedDialogs.get(0);
        expectedDialogs.remove(0);
        return thisDialog;
    }

    private static List getExpectedDialogs(final Context context) {
        initIfNeeded(context);
        return (List) context.get(EXPECTED_DIALOGS_KEY);
    }

    private static void initIfNeeded(final Context context) {
        if (!context.containsKey(EXPECTED_DIALOGS_KEY)) {
            context.put(EXPECTED_DIALOGS_KEY, new ArrayList());
        }
    }
}
