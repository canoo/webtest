package com.canoo.webtest.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Produces a unified diff like output (should be like unified diff, but not sure).
 * @author Marc Guillemot
 */
public class VerifyContentTextDiff implements VerifyContentDiff 
{
	private final static int CONTEXT_SIZE = 3;
	final List deltas = new ArrayList();
	private List hashValuesA;
	private List hashValuesB;
	protected String[] tabReference;
	protected String[] tabActual;

	/**
	 * {@inheritDoc}
	 */
	public String compare(final WebResponse reference, final WebResponse actual, 
			final String referenceLabel, final String actualLabel)
	{
		final String referenceText = reference.getContentAsString();
		final String actualText = actual.getContentAsString();
	
		return produceTextDiffMessage(referenceText, actualText, referenceLabel, actualLabel);
	}

	/**
	 * Resets the state to allow new computation
	 */
	protected void reset() {
		deltas.clear();
		hashValuesA = null;
		hashValuesB = null;
		tabReference = null;
		tabActual = null;
	}

	String produceTextDiffMessage(final String referenceText, final String actualText, 
			final String referenceLabel, final String actualLabel) 
	{
		reset();

		tabReference = referenceText.split("[\\n\\r]+");
		tabActual = actualText.split("[\\n\\r]+");
		
		preProcess();
		
		compare(0, tabReference.length, 0, tabActual.length);

		if (deltas.isEmpty())
			return null;

		final StringBuffer sb = new StringBuffer();
		sb.append("--- " + referenceLabel + "\n");
		sb.append("+++ " + actualLabel + "\n");
		printDiff(sb);

		return sb.toString();
	}

	/**
	 * Called before comparision starts
	 */
	protected void preProcess() {
		hashValuesA = computeHash(tabReference);
		hashValuesB = computeHash(tabActual);
	}

    private void printDiff(final StringBuffer sb) 
    {
    	for (final Iterator iter = getContexts(deltas).iterator(); iter.hasNext();) 
    	{
			final Context context = (Context) iter.next();
			context.print(sb);
		}
 	}

	private List getContexts(final List _deltas) 
	{
		if (_deltas.isEmpty())
			return Collections.EMPTY_LIST;

		
		final List contexts = new ArrayList();
		Context context = new Context();
		contexts.add(context);
		for (final Iterator iter = _deltas.iterator(); iter.hasNext();) 
		{
			final Delta delta = (Delta) iter.next();
			if (!context.accept(delta))
			{
				context = new Context();
				contexts.add(context);
			}
			context.addDelta(delta);
		} 
		
		return contexts;
	}

	private void compare(int startA, int endA, int startB, int endB) 
    {
    	
    	// handle identical content at the start
    	while (startA < endA && startB < endB && sameContent(startA, startB))
    	{
			startB++;
			startA++;
    	}

    	// handle identical content at the end
    	while (endA > startA && endB > startB && sameContent(endA - 1, endB - 1))
    	{
    		endB--;
    		endA--;
    	}

    	if (startA == endA) 
    	{
    		for (int i=startB; i < endB; ++i)
    			deltas.add(new Delta(startA, i, 'B'));
    	}
    	else if (startB == endB) 
    	{
    		for (int i=startA; i < endA; ++i)
    			deltas.add(new Delta(i, startB, 'A'));
    	}
    	else if (startA == endA - 1 && startB == endB - 1) 
    	{
    		if (!sameContent(startA, startB))
    		{
    			deltas.add(new Delta(startA, startB, 'A'));
    			deltas.add(new Delta(startA, startB, 'B'));
    		}
    	} 
    	else
    	{
    		final MidPoint splitPoints = retrieveMidPoint(startA, endA, startB, endB);
    		compare(startA, splitPoints.lineA, startB, splitPoints.lineB);
    		compare(splitPoints.lineA, endA, splitPoints.lineB, endB);
    	}
    }

	
	/**
	 * Indicates if 2 content lines are identical
	 * @param posA the line in the first content
	 * @param posB the line in the 2nd content
	 * @return <code>true</code> if lines are identical
	 */
	protected boolean sameContent(final int posA, final int posB) {
		return hashValuesA.get(posA).equals(hashValuesB.get(posB));
	}
    
    static class MidPoint
    {
    	final int lineA;
    	final int lineB;
    	public MidPoint(final int _first, final int _second)
    	{
    		lineA = _first;
    		lineB = _second;
    	}
    }
    
    private MidPoint retrieveMidPoint(final int startA, final int endA, final int startB, final int endB) 
    {
    	final int midA = (endA + startA) / 2;
    	final int midB = (endB + startB) / 2;
    	int index1 = midA;
    	int incrementA = -1;

    	while (index1 >= startA && index1 < endA) 
    	{
    		int indexB = midB;
    		int incrementB = -1;
    		while (indexB >= startB && indexB < endB) 
    		{
    			if (sameContent(index1, indexB))
    			{
    				return new MidPoint(index1, indexB);
    			}
    			indexB += incrementB;
    			incrementB = nextIncrement(incrementB);
    		}
		    index1 += incrementA;
		    incrementA = nextIncrement(incrementA);
		}
	
    	return new MidPoint(midA, midB);
    }

    static int nextIncrement(final int increment)
    {
		if (increment < 0)
			return -increment + 1;
		else
			return -increment - 1;
    }
    
    /**
     * Holds information about differences
     */
    static private class Delta
    {
    	final int lineA;
    	final int lineB;
    	final char type;
	
    	public Delta(final int _lineA, final int _lineB, final char _type) 
    	{
    		lineA = _lineA;
    		lineB = _lineB;
    		type = _type;
    	}
    }
    
    /**
     * Contains a group of deltas
     */
    private class Context
    {
    	final List deltas = new ArrayList();
    	int firstLineA = -1;
    	int lastLineA = -1;
    	private int firstA = -1;
    	private int lastA = -1;
    	private Delta firstDelta;
    	
    	public boolean accept(final Delta _delta)
    	{
    		if (_delta.type == 'B')
    			return true;
    		else if (firstLineA == -1)
    			return true;
    		else if (_delta.lineA <= lastLineA)
    			return true;
    		
    		return false;
    	}
    	
    	public void addDelta(final Delta _delta)
    	{
    		if (firstDelta == null)
    			firstDelta = _delta;
    		deltas.add(_delta);
    		if (_delta.type == 'A')
    		{
        		if (firstLineA == -1)
        		{
        			firstA = _delta.lineA;
        			firstLineA = Math.max(1, firstA - CONTEXT_SIZE);
        		}
        		lastA = _delta.lineA;
        		lastLineA = Math.min(tabReference.length, lastA + CONTEXT_SIZE);
    		}
    	}
    	
    	int getFirstContextLineB()
    	{
    		return Math.max(1, firstDelta.lineB - CONTEXT_SIZE);
    	}
    	
    	int getLastContextLineB()
    	{
    		final Delta lastDelta = (Delta) deltas.get(deltas.size() - 1);
    		return Math.min(tabActual.length, lastDelta.lineB + CONTEXT_SIZE);
    	}

    	void print(final StringBuffer sb)
    	{
    		// the chunk range information
    		sb.append("@@ -");
    		sb.append(firstLineA);
    		sb.append(",");
    		sb.append(lastLineA - firstLineA + 1);
    		sb.append(" +");
    		sb.append(getFirstContextLineB());
    		sb.append(",");
    		sb.append(getLastContextLineB() - getFirstContextLineB() + 1);
    		sb.append(" @@");
    		sb.append("\n");

    		for (int i=firstLineA; i<firstA; ++i)
    		{
				sb.append(" ").append(tabReference[i]).append("\n");
    		}
    		for (final Iterator iter = deltas.iterator(); iter.hasNext();) 
    		{
				final Delta delta = (Delta) iter.next();
				if (delta.type == 'B')
					sb.append("+").append(tabActual[delta.lineB]).append("\n");
				else
					sb.append("-").append(tabReference[delta.lineA]).append("\n");
			}
    		for (int i=lastA+1; i<lastLineA; ++i)
    		{
				sb.append(" ").append(tabReference[i]).append("\n");
    		}
    	}
    }

    /**
	 * Gets for each element its hashCode()
	 * @param tab
	 */
	private List computeHash(final String[] tab) {
		final List hashValues = new ArrayList(tab.length);
		for (int i = 0; i < tab.length; i++) 
		{
			hashValues.add(new Integer(tab[i].hashCode()));
		}
		
		return hashValues;
	}

}
