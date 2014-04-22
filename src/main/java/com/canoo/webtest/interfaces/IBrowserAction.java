package com.canoo.webtest.interfaces;

/**
 * @author Paul King
 */
public interface IBrowserAction
{
    void setSave(String prefix);

    void setSavePrefix(String prefix);

    void setSaveResponse(String response);

    String getTaskName();
}
