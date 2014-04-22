// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import com.canoo.webtest.engine.StepFailedException;

public class FailStepStub extends StepStub
{
    public void doExecute() {
        throw new StepFailedException("A failure", this);
    }
}
