package com.canoo.webtest.steps.mouse;


/**
 * Triggers a <code>mouseOut</code> event on the HTML element determined by its id or xpath.
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="mouseOut"
 * description="Triggers a <em>mouseOut</em> event on the <key>HTML</key> element identified by its
 * <em>id</em> or <em>xpath</em>. If this triggers the load of a new page, then this page becomes the current one."
 */
public class MouseOut extends MouseBaseStep
{
	public void doExecute() throws Exception
	{
		findElement().mouseOut();
	}
}
