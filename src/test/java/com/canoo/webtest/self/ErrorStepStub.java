// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import com.canoo.webtest.engine.StepExecutionException;

public class ErrorStepStub extends StepStub
{
    public void doExecute() {
        throw new StepExecutionException("A setup error", this);
    }
}
