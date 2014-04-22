package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFLink;

/**
 * Implementation of {@link PDFLink} based on 
 * <a href="http://www.pdfbox.org/">PDFBox</a>. 
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class PDFBoxPDFLink implements PDFLink {

	private final String text_;
	private final String href_;
	private final int page_;

	public PDFBoxPDFLink(final String text, final String href, final int page) {
		text_ = text;
		href_ = href;
		page_ = page;
	}

	public String getHref() {
		return href_;
	}

	public int getPage() {
		return page_;
	}

	public String getText() {
		return text_;
	}
	
	/**
	 * Returns information for debug purpose
	 */
	public String toString() {
		final StringBuilder sb = new StringBuilder("PDFBoxPDFLink(");
		sb.append("href: ");
		sb.append(getHref());
		sb.append(", page: ");
		sb.append(getPage());
		sb.append(", text: ");
		sb.append(getText());
		sb.append(")@");
		sb.append(System.identityHashCode(this));
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((href_ == null) ? 0 : href_.hashCode());
		result = prime * result + page_;
		result = prime * result + ((text_ == null) ? 0 : text_.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof PDFBoxPDFLink)) {
			return false;
		}
		final PDFBoxPDFLink other = (PDFBoxPDFLink) obj;
		if (href_ == null) {
			if (other.href_ != null)
				return false;
		} else if (!href_.equals(other.href_))
			return false;
		if (page_ != other.page_)
			return false;
		if (text_ == null) {
			if (other.text_ != null)
				return false;
		} else if (!text_.equals(other.text_))
			return false;
		return true;
	}
}
