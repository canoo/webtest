// Released under the Canoo WebTest license
package com.canoo.webtest.steps.form;
/**
 * @author Denis N. Antonioli
 */

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.StepTest;
import com.gargoylesoftware.htmlunit.html.HtmlBase;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSetFieldStepTest extends StepTest {

	public void testXPathAccessor() {
		AbstractSetFieldStep step = (AbstractSetFieldStep) getStep();
		final String xpath = "//form/input";
		step.setXpath(xpath);
		assertEquals(xpath, step.getXpath());
	}

	public void testHtmlIdAccessor() {
		AbstractSetFieldStep step = (AbstractSetFieldStep) getStep();
		final String htmlId = "id";
		step.setHtmlId(htmlId);
		assertEquals(htmlId, step.getHtmlId());
	}

	public void testNameAccessor() {
		AbstractSetFieldStep step = (AbstractSetFieldStep) getStep();
		final String name = "nom";
		step.setName(name);
		assertEquals(name, step.getName());
	}

	public void testSelectField() {
		final List fieldList = new ArrayList();
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() {
				AbstractSetFieldStep.selectField(fieldList, "", null);
			}
		});
		final HtmlPage page = getDummyPage("<html></html>");

		final HtmlBase element0 = (HtmlBase) page.createElement("base");
		fieldList.add(element0);
		final HtmlBase element1 = (HtmlBase) page.createElement("base");
		fieldList.add(element1);
		fieldList.add(page.createElement("base"));
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() {
				AbstractSetFieldStep.selectField(fieldList, "3", null);
			}
		});
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() {
				AbstractSetFieldStep.selectField(fieldList, "-1", null);
			}
		});
		assertSame(element0, AbstractSetFieldStep.selectField(fieldList, "", null));
		assertSame(element1, AbstractSetFieldStep.selectField(fieldList, "1", null));
	}
}