package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.collections.Predicate;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFField;

/**
 * Implementation of {@link PDFField} based on 
 * <a href="http://www.pdfbox.org/">PDFBox</a>. 
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PdfBoxPDFField implements PDFField {
	private final PDField field_;

	private PdfBoxPDFField(final PDField field)
	{
		field_ = field;
	}
	
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("name: " + getName() + ", ");
		sb.append("value: " + getValue());
		return sb.toString();
	}

	public String getValue() {
		try 
		{
			final PDField parent = field_.getParent();
			final PDField field = parent != null ? parent : field_;

			String value = field.getValue();
			if (value == null)
				value = PdfBoxPDFPage.stringValue(field.getDictionary().getDictionaryObject(COSName.DV));
			
			return value;
		}
		catch (final IOException e) {
			throw new RuntimeException("Can't read field value", e);
		}
	}
	
	protected static int getPageNumber(final PDField nativeField)
	{
        int pageNumber = -1;
		try 
		{
	        final COSDictionary page = (COSDictionary) nativeField.getWidget().getDictionary().getDictionaryObject(COSName.P);
	        final List pages = nativeField.getAcroForm().getDocument().getDocumentCatalog().getAllPages();
	        for (final ListIterator iter = pages.listIterator(); iter.hasNext();) {
	            final PDPage pageX = (PDPage) iter.next();
	            if (pageX.getCOSDictionary().equals(page)) {
	            	pageNumber = iter.previousIndex();
	            	break;
	            }
			}
		}
		catch (final IOException e) 
		{
			throw new RuntimeException("Failed accessing the page", e);
		}

		if (pageNumber == -1)
			throw new RuntimeException("Failed retrieving page number for field " + nativeField);
        return 1 + pageNumber;
	}

	public PDField getNativeField()
	{
		return field_;
	}
	
	public boolean isReadOnly() 
	{
		return getNativeField().isReadonly();
	}

	/**
	 * Gets the field name
	 */
	public String getName() 
	{
		try 
		{
			return getNativeField().getFullyQualifiedName();
		}
		catch (final IOException e) 
		{
			throw new RuntimeException("Problem extracting field name", e);
		}
	}

	private static final Map<PDField, PDFField> wrappers_ = new WeakHashMap<PDField, PDFField>(); 

	static public PDFField wrap(final PDField nativeField)
	{
		PDFField wrapperField = wrappers_.get(nativeField);
		if (wrapperField == null)
		{
			wrapperField = new PdfBoxPDFField(nativeField);
			wrappers_.put(nativeField, wrapperField);
		}
		return wrapperField;
	}
	
	protected abstract static class FieldPredicate implements Predicate
	{
		public boolean evaluate(final Object object) {
			try {
				return evaluateField((PDField) object);
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		protected abstract boolean evaluateField(final PDField field) throws IOException;
		
		/**
		 * Builds a field predicate accepting only fields with the given name
		 * @param name the name to filter with
		 * @return a predicate
		 */
		static FieldPredicate buildNamePredicate(final String name)
		{
			return new FieldPredicate()
			{
				protected boolean evaluateField(final PDField field) throws IOException {
					final PDField parent = field.getParent();
					final String fieldName;
					if (parent != null)
						fieldName = parent.getFullyQualifiedName();
					else
						fieldName = field.getFullyQualifiedName();
					return name.equals(fieldName);
				}
			};
		}

		/**
		 * Builds a field predicate accepting only fields located on the given page
		 * @param pageNumber the page on which fields should be located
		 * @return a predicate
		 */
		static FieldPredicate buildPageNumberPredicate(final int pageNumber)
		{
			return new FieldPredicate()
			{
				protected boolean evaluateField(final PDField field) throws IOException {
					return pageNumber == getPageNumber(field);
				}
			};
		}
	}
}
