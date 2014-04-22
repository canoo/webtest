package com.canoo.webtest.steps.mouse;


/**
 * Triggers a <code>mouseOver</code> event on the HTML element determined by its id or xpath.
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="mouseOver"
 * description="Triggers a <em>mouseOver</em> event on the <key>HTML</key> element identified by its
 * <em>id</em> or <em>xpath</em>. If this triggers the load of a new page, then this page becomes the current one."
 */
public class MouseOver extends MouseBaseStep
{
	public void doExecute() throws Exception
	{
		findElement().mouseOver();
	}
}
