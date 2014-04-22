// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import java.io.File;

import junit.framework.TestCase;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.extension.dialogs.AlertDialogStep;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for file utilities.
 *
 * @author Paul King
 */
public class FileUtilTest extends TestCase
{
    public void testPrepareDirsNoParent() {
        assertTrue("should call mkdirs if parent file does not exist", checkPrepareDirs(false, 1));
        assertTrue("should recursively call mkdirs if parent file(s) do not exist", checkPrepareDirs(false, 2));
    }

    public void testPrepareDirsWithParent() {
        assertFalse("shouldn't call mkdirs if parent file does exist", checkPrepareDirs(true, 1));
    }

    private boolean checkPrepareDirs(final boolean parentExists, final int numLevels) {
        final boolean[] mkdirs = new boolean[numLevels];
        final boolean[] exists = new boolean[numLevels + 1];
        for (int i = 0; i < numLevels; i++) {
            mkdirs[i] = false;
            exists[i] = false;
        }
        final File fileUnderTest = new File("")
        {
            private int fLevel;
            private boolean fDone;

            public boolean exists() {
                exists[fLevel] = true;
                if (fLevel == numLevels) {
                    fDone = true;
                } else {
                    fLevel++;
                }
                return fDone || parentExists;
            }
            public File getParentFile() {
                return this;
            }
            public boolean mkdirs() {
                fLevel--;
                mkdirs[fLevel] = true;
                return true;
            }
        };
        FileUtil.prepareDirs(fileUnderTest);
        for (int i = 0; i < numLevels; i++) {
            if (!exists[i] || !mkdirs[i]) {
                return false;
            }
        }
        return exists[numLevels];
    }

    public void testWriteStringToFileErrorHandling() {
        final Step dummyStep = new AlertDialogStep(); // anything will do
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                FileUtil.writeStringToFile(new File("."), "x", dummyStep);
            }
        });
    }

    public void testTryWriteObjectToFileErrorHandlingForStep() {
        // when given a step should throw exception
        final Step dummyStep = new AlertDialogStep(); // anything will do
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                FileUtil.tryWriteObjectToFile(new File("."), "x", dummyStep);
            }
        });
        // when given no step should return false
        assertFalse(FileUtil.tryWriteObjectToFile(new File("."), "x", null));
    }

    public void testReadFileToStringErrorHandling() {
        final Step dummyStep = new AlertDialogStep(); // anything will do
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                FileUtil.readFileToString(new File("wOntbefOund"), dummyStep);
            }
        });
    }

    public void testTryReadFileToStringErrorHandling() {
        final Step dummyStep = new AlertDialogStep(); // anything will do
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                FileUtil.tryReadObjectFromFile(new File("wOntbefOund"), dummyStep);
            }
        });
    }

    public void testTryCreateTempFileErrorHandling() {
        final Step dummyStep = new AlertDialogStep(); // anything will do
        ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock() {
            public void call() throws Throwable {
                // will be unable to create a file - prefix must be at least 3 chars
                FileUtil.tryCreateTempFile("x", null, dummyStep);
            }
        });
    }
}
