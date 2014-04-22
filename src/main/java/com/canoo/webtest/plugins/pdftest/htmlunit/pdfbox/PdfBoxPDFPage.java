package com.canoo.webtest.plugins.pdftest.htmlunit.pdfbox;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.collections.functors.TruePredicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.BadSecurityHandlerException;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.pdmodel.encryption.SecurityHandlersManager;
import org.apache.pdfbox.pdmodel.encryption.StandardSecurityHandler;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.PDFTextStripperByArea;

import com.canoo.webtest.plugins.pdftest.htmlunit.PDFEncryptionPermission;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFField;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFInvalidPasswordException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;

/**
 * Implementation of {@link PDFPage} based on <a href="http://www.pdfbox.org/">PDFBox</a>.
 * @author Etienne Studer
 * @author Paul King
 * @author Marc Guillemot
 */
public class PdfBoxPDFPage implements PDFPage {
	private PDDocument pdfDocument_;
	private List bookmarks_;
	private final WebWindow webWindow_;
	private final WebResponse webResponse_;
	
	private static final COSName INFO_PROPERTY_TITLE = COSName
			.getPDFName("Title"); // title of document

	private boolean cleanUpCalled;
	private static int counter = 0;
	private static int allocated = 0;
	private final byte[] bytes;
	
	public void cleanUp() throws IOException {
		cleanUpCalled = true;
		allocated--;
		if (pdfDocument_ != null)
			pdfDocument_.close();
	}

	public PdfBoxPDFPage(final WebResponse webResponse, final WebWindow webWindow) throws IOException {
		webWindow_ = webWindow;
		webResponse_ = webResponse;
		bytes = IOUtils.toByteArray(webResponse.getContentAsStream());

        pdfDocument_ = loadPDFDocument();
		counter++;
		allocated++;
	}

	protected PDDocument loadPDFDocument()
	{
		try {
			return PDDocument.load(new ByteArrayInputStream(bytes));
		}
		catch (final IOException e) {
	        getLog().warn("Failed parsing PDF document " + getUrl() + ": " + e.getMessage(), e);
		}
		
		return null;
	}

	/**
     * Return the log object for this web client
     * @return The log object
     */
    protected final Log getLog() {
        return LogFactory.getLog(getClass());
    }

    
    
    private COSDictionary getInfoDictionary() {
		final COSDictionary encryptProperties = getPDFDocument()
				.getDocumentInformation().getDictionary();
		return encryptProperties != null ? encryptProperties
				: new COSDictionary();
	}

	private static void assertKeyExists(final COSName key, final COSDictionary properties) {
		if (properties.keySet().contains(key)) {
			return;
		}

		throw new IllegalArgumentException("Specified property key '"
				+ key.getName() + "' does not exist.");
	}

	public String getDocumentTitle() {
		assertKeyExists(INFO_PROPERTY_TITLE, getInfoDictionary());
		COSString title = (COSString) getInfoDictionary().getItem(
				INFO_PROPERTY_TITLE);
		return title.getString();
	}

	public WebWindow getEnclosingWindow() {
		return webWindow_;
	}

	public WebResponse getWebResponse() {
		return webResponse_;
	}

	public URL getUrl() {
		return getWebResponse().getWebRequest().getUrl();
	}
	
	public void initialize() throws IOException {
		// TODO Auto-generated method stub

	}

	public int getNumberOfPages() {
		return getPDFDocument().getNumberOfPages();
	}

	/**
	 * Gets the PDF document
	 * @return the document
	 * @throws RuntimeException if the PDF document couldn't be parsed
	 */
	protected PDDocument getPDFDocument() {
		if (cleanUpCalled)
		{
	        pdfDocument_ = loadPDFDocument();
			cleanUpCalled = false;
		}
		if (pdfDocument_ == null)
			throw new RuntimeException("Can't work on pdf document as it couldn't get parsed");
		return pdfDocument_;
	}

	public List getFields() {
		return getFields(TruePredicate.INSTANCE);
	}

	public void decrypt(String password)
	{
		try {
			getPDFDocument().decrypt(password);
		}
		catch (final InvalidPasswordException e)
		{
			throw new PDFInvalidPasswordException(e);
		}
		catch (final CryptographyException e)
		{
			throw new PDFInvalidPasswordException(e);
		}
		catch (final Exception e) {
			throw new RuntimeException("Problem decrypting the document", e);
		}
	}

	public boolean isEncrypted() {
		return getPDFDocument().isEncrypted();
	}

	public String getText(int startPage, int endPage) {
		return getTextInternal(startPage, endPage);
	}

	protected String getTextInternal(int startPage, int endPage) {
		try {
			final PDFTextStripper textStripper = new PDFTextStripper();
			textStripper.setStartPage(startPage);
			textStripper.setEndPage(endPage);
			return textStripper.getText(getPDFDocument());
		} catch (final IOException e) {
			throw new RuntimeException("Problem extracting text", e);
		}
	}
	
	protected List getFields(final Predicate filter)
	{
        final PDAcroForm acroForm = getPDFDocument().getDocumentCatalog().getAcroForm();
        final List<PDFField> response = new ArrayList<PDFField>(); 

        try
        {
	        if (acroForm != null) {
	            final List fields = acroForm.getFields();
	            for (final Iterator iter = fields.iterator(); iter.hasNext();) {
	                final PDField field = (PDField) iter.next();
                	final List kids = field.getKids();
                	if (kids != null && !kids.isEmpty())
                	{
                		for (final Iterator iterKids = kids.iterator(); iterKids.hasNext();) {
                            final PDField childField = (PDField) iterKids.next();
                        	if (filter.evaluate(childField))
           	                {
                        		response.add(PdfBoxPDFField.wrap(childField));
           	                }
						}
                	}
                	else if (filter.evaluate(field))
   	                {
   	                	response.add(PdfBoxPDFField.wrap(field));
	                }
	            }
	        }
    	} 
        catch (final IOException e) 
        {
        	throw new RuntimeException("Failed reading fields", e);
        }
 
        return response;
	}
	
	public List getFields(final String name, final PDFField.Type type)
	{
		return getFields(PdfBoxPDFField.FieldPredicate.buildNamePredicate(name));
	}

	public List getFields(final String name, final int pageNumber, final PDFField.Type type)
	{
		final Predicate predicateName = PdfBoxPDFField.FieldPredicate.buildNamePredicate(name);
		final Predicate predicatePage = PdfBoxPDFField.FieldPredicate.buildPageNumberPredicate(pageNumber);

		final Predicate predicate = new AndPredicate(predicateName, predicatePage);
		
		return getFields(predicate);
	}


    public boolean hasPermission(final PDFEncryptionPermission permission) {
    	// with release 0.7.3, following doesn't work
    	/*
    	AccessPermission info = getPDFDocument().getCurrentAccessPermission();
    	...
        else if (PDFEncryptionPermission.PRINTING.equals(permission))
        	return info.canPrint();
        ...
    	*/
    	// values taken from deprecated class PDStandardEncryption
        final int PRINT_BIT = 3;
        final int MODIFICATION_BIT = 4;
        final int MODIFY_ANNOTATIONS_BIT = 6;
        final int ASSEMBLE_DOCUMENT_BIT = 11;
        final int DEGRADED_PRINT_BIT = 12;
        final int EXTRACT_BIT = 5;
        final int FILL_IN_FORM_BIT = 9;
        final int EXTRACT_FOR_ACCESSIBILITY_BIT = 10;

        final int bitValue;
        if (PDFEncryptionPermission.ASSEMBLY.equals(permission))
        	bitValue = ASSEMBLE_DOCUMENT_BIT;
        else if (PDFEncryptionPermission.COPY.equals(permission))
        	bitValue = EXTRACT_BIT;
        else if (PDFEncryptionPermission.DEGRADED_PRINTING.equals(permission))
        	bitValue = DEGRADED_PRINT_BIT;
        else if (PDFEncryptionPermission.FILL_IN.equals(permission))
        	bitValue = FILL_IN_FORM_BIT;
        else if (PDFEncryptionPermission.MODIFY_ANNOTATIONS.equals(permission))
        	bitValue = MODIFY_ANNOTATIONS_BIT;
        else if (PDFEncryptionPermission.MODIFY_CONTENTS.equals(permission))
        	bitValue = MODIFICATION_BIT;
        else if (PDFEncryptionPermission.PRINTING.equals(permission))
        	bitValue = PRINT_BIT;
        else if (PDFEncryptionPermission.SCREEN_READERS.equals(permission))
        	bitValue = EXTRACT_FOR_ACCESSIBILITY_BIT;
        else
        	throw new IllegalArgumentException("Unknown pdf permission: " + permission);

        
        final PDEncryptionDictionary info;
		try {
			info = (PDEncryptionDictionary) getPDFDocument().getEncryptionDictionary();
		}
		catch (final IOException e)
		{
			throw new RuntimeException("Can't read permissions", e);
		}
        return (info.getPermissions() & (1 << (bitValue-1))) != 0;
    }

    public String getEncryptProperty(final String key)
    {
    	final COSDictionary encryptProperties = getPDFDocument().getDocument().getEncryptionDictionary();
    	return stringValue(encryptProperties.getDictionaryObject(key));
    }
    
    static String stringValue(final COSBase element)
    {
        if (element == null) {
            return null;
        }
        else if (element instanceof COSString) {
            return ((COSString) element).getString();
        }
        else if (element instanceof COSName) {
            return ((COSName) element).getName();
        }
        else if (element instanceof COSBoolean) {
            return String.valueOf(((COSBoolean) element).getValue());
        }
        else if (element instanceof COSInteger) {
            return String.valueOf(((COSInteger) element).intValue());
        }
        else if (element instanceof COSFloat) {
            return String.valueOf(((COSFloat) element).floatValue());
        }
        else if (element instanceof COSNull) {
            return "null";
        }
        else
        	return String.valueOf(element);
    }

    public int getEncryptionStrength() 
    {
        try {
			return getPDFDocument().getEncryptionDictionary().getLength();
		}
        catch (final IOException e) 
        {
        	throw new RuntimeException("Failed reading encryption strength", e);
		}
    }

    public String getInfoProperty(final String key) {
        final COSDictionary properties = getPDFDocument().getDocumentInformation().getDictionary();
        if (properties == null)
        	return null;

        final COSName pdfName = COSName.getPDFName(key);
        return stringValue(properties.getDictionaryObject(pdfName));
    }

    public boolean isUserPassword(final String password)
    {
        try {
        	return isPassword(password, true);
		}
        catch (final Exception e) 
        {
        	throw new RuntimeException("Failed verifying user password", e);
		}
    }


	private boolean isPassword(String password, boolean userPassword) throws IOException, BadSecurityHandlerException, CryptographyException {
        final StandardSecurityHandler secHandler = getSecurityHandler();
    	
        PDEncryptionDictionary dictionary = getPDFDocument().getEncryptionDictionary();

        int dicPermissions = dictionary.getPermissions();
        int dicRevision = dictionary.getRevision();
        int dicLength = dictionary.getLength()/8;

        COSString id = (COSString) getPDFDocument().getDocument().getDocumentID().getObject( 0 );
        byte[] u = dictionary.getUserKey();
        byte[] o = dictionary.getOwnerKey();

        if (userPassword)
        {
        	return secHandler.isUserPassword(password.getBytes(), u, 
        			o, dicPermissions, id.getBytes(), dicRevision, dicLength, true);
        }
        else
        {
        	return secHandler.isOwnerPassword(password.getBytes(), u, 
        			o, dicPermissions, id.getBytes(), dicRevision, dicLength, true);
        }
	}

	private StandardSecurityHandler getSecurityHandler() throws IOException,
			BadSecurityHandlerException {
		PDEncryptionDictionary dict = getPDFDocument().getEncryptionDictionary();
        StandardSecurityHandler secHandler = (StandardSecurityHandler) SecurityHandlersManager.getInstance().getSecurityHandler(dict.getFilter());
		return secHandler;
	}

	public boolean isOwnerPassword(final String password) {
        try {
			return isPassword(password, false);
		}
        catch (final Exception e) 
        {
        	throw new RuntimeException("Failed verifying owner password", e);
		}
    }

    public List getBookmarks()
    {
    	if (bookmarks_ == null)
    		bookmarks_ = extractBookmarks();
    	
    	return bookmarks_;
    }

    private List extractBookmarks() 
    {
        final PDDocumentOutline outline = getPDFDocument().getDocumentCatalog().getDocumentOutline();
        final List<PdfBoxPDFBookmark> result = new ArrayList<PdfBoxPDFBookmark>();
        if (outline != null) 
        {
           	PDOutlineItem child = outline.getFirstChild();
            while (child != null) 
            {
            	final PdfBoxPDFBookmark topBookmark = new PdfBoxPDFBookmark(child, null);
            	result.add(topBookmark);
            	result.addAll(topBookmark.getAllChildren());
            	child = child.getNextSibling();
            }
        }
        return result;
    }

    public List getFonts() {
        final List<PDFBoxPDFFont> fonts = new ArrayList<PDFBoxPDFFont>();
    	final List pages = getPDFDocument().getDocumentCatalog().getAllPages();
    	for (final ListIterator iter = pages.listIterator(); iter.hasNext();) 
    	{
			final PDPage page = (PDPage) iter.next();
			try {
    			for (final Iterator fontIterator = page.findResources().getFonts().values().iterator(); 
    					fontIterator.hasNext();) {
    				final PDFont font = (PDFont) fontIterator.next();
    				fonts.add(new PDFBoxPDFFont(font, iter.nextIndex())); // nextIndex() because page number start with 1 not 0
    			}
    		}
	        catch (final IOException e) 
	        {
	        	throw new RuntimeException("Failed retrieving the fonts on page " + iter.nextIndex(), e);
	        }
        }
        return fonts;
    }

	public List getFields(int pageNumber) {
		final Predicate predicatePage = PdfBoxPDFField.FieldPredicate.buildPageNumberPredicate(pageNumber);
		return getFields(predicatePage);
	}

	public List getFields(final String name, final int pageNumber) {
		final Predicate predicateName = PdfBoxPDFField.FieldPredicate.buildNamePredicate(name);
		final Predicate predicatePage = PdfBoxPDFField.FieldPredicate.buildPageNumberPredicate(pageNumber);

		final Predicate predicate = new AndPredicate(predicateName, predicatePage);
		
		return getFields(predicate);
	}

	public List getFields(final String name) {
		return getFields(PdfBoxPDFField.FieldPredicate.buildNamePredicate(name));
	}

	/**
	 * Gets the links from the document
	 * @return the links
	 */
	public List<PDFBoxPDFLink> getLinks() {
        final List<PDFBoxPDFLink> result = new ArrayList<PDFBoxPDFLink>();
        final List allPages = getPDFDocument().getDocumentCatalog().getAllPages();
        for (final ListIterator iter = allPages.listIterator(); iter.hasNext();) {
			final PDPage page = (PDPage) iter.next();
	        processPage(result, page, iter.nextIndex());
	    }
	    return result;
	}

    private static void processPage(final List<PDFBoxPDFLink> result, final PDPage page, final int pageNum) {
        try {
        	final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        	final List<PDAnnotationLink> linkAnnotations = new ArrayList<PDAnnotationLink>();
        	final List<Rectangle2D.Float> linkRegions = new ArrayList<Rectangle2D.Float>();
            extractAnnotations(page, stripper, linkAnnotations, linkRegions);
            stripper.extractRegions(page);
            final Map<Rectangle2D.Float, String> uriMap = new HashMap<Rectangle2D.Float, String>();
            final Map<Rectangle2D.Float, String> textMap = new HashMap<Rectangle2D.Float, String>();
            collateLinks(linkAnnotations, linkRegions, uriMap, textMap, stripper);
            final Iterator it = uriMap.keySet().iterator();
            while (it.hasNext()) {
            	final Object key = it.next();
                result.add(new PDFBoxPDFLink(textMap.get(key), uriMap.get(key), pageNum));
            }
        }
        catch (final IOException e) {
            // ignore
        }
    }

    private static void collateLinks(final List<PDAnnotationLink> linkAnnotations, final List<Rectangle2D.Float> linkRegions, 
    		final Map<Rectangle2D.Float, String>  uriMap, final Map<Rectangle2D.Float, String> textMap, final PDFTextStripperByArea stripper) throws IOException {
        for (int j = 0; j < linkAnnotations.size(); j++) {
            final PDAnnotationLink link = (PDAnnotationLink) linkAnnotations.get(j);
            final PDAction action = link.getAction();
            final String urlText = stripper.getTextForRegion(Integer.toString(j));
            if (action instanceof PDActionURI) {
            	final PDActionURI uri = (PDActionURI) action;
                // internal links have no text
                if (urlText.length() > 0) {
                    textMap.put(linkRegions.get(j), urlText.trim());
                }
                uriMap.put(linkRegions.get(j), uri.getURI());
            }
            else if (action instanceof PDActionGoTo) {
                // internal link text associated with goto
                if (urlText.length() > 0) {
                    textMap.put(linkRegions.get(j), urlText.trim());
                }
            }
        }
    }

    private static List extractAnnotations(final PDPage page, final PDFTextStripperByArea stripper, 
    		final List<PDAnnotationLink> linkAnnotations, final List<Rectangle2D.Float> linkRegions) throws IOException {
    	int annotationIndex = 0;
    	@SuppressWarnings("unchecked")
		final List<PDAnnotation> annotations = page.getAnnotations();
        for (int j = 0; j < annotations.size(); j++) {
        	final PDAnnotation annot = (PDAnnotation) annotations.get(j);
            if (annot instanceof PDAnnotationLink) {
                final PDAction action = ((PDAnnotationLink) annot).getAction();
                // it seems (at least in testDocLinks.pdf) that for an internal link there is as well a PDActionGoTo and a PDAnnotationLink
                // with the same rectangle
                // With PdfBox 1.4.0, Java 5 and Java 6 don't make the same sorting in HashMap what results in
                // text associated with the PDAnnotationLink in one case (Java 6) and with the PDActionGoTo in the other (Java5)
                // what causes text to be lost in this second case
                // => the solution seems to be to ignore the PDActionGoTo
                if (action instanceof PDActionGoTo) {
            		continue; // don't consider it at all
                }
                
            	final PDRectangle rect = annot.getRectangle();
                //need to reposition link rectangle to match text space plus add
                //a little to account for descenders and the like
            	final float x = rect.getLowerLeftX() - 1;
                float y = rect.getUpperRightY() - 1;
                final float width = rect.getWidth() + 2;
                final float height = rect.getHeight() + rect.getHeight() / 4;
                final int rotation = page.findRotation();
                if (rotation == 0) {
                	final PDRectangle pageSize = page.findMediaBox();
                    y = pageSize.getHeight() - y;
                }

                final Rectangle2D.Float awtRect = new Rectangle2D.Float(x, y, width, height);
                stripper.addRegion(Integer.toString(annotationIndex), awtRect);
                linkAnnotations.add((PDAnnotationLink) annot);
                linkRegions.add(awtRect);
                annotationIndex++;
            }
        }
        return annotations;
    }

    public String getText(final String fragmentSeparator, final String lineSeparator, final String pageSeparator, final String mode) {
        return getText(0, getNumberOfPages(), fragmentSeparator, lineSeparator, pageSeparator, mode);
	}

    private String getText(final int startPage, final int endPage, final String fragmentSeparator, 
    		final String lineSeparator, final String pageSeparator, final String mode)
    {
        final StringBuilder buf = new StringBuilder();
//        if (MODE_NORMAL.equals(mode)) {
            buf.append(getTextInternal(startPage, endPage, lineSeparator, pageSeparator));
//        } 
//        else 
//        {
//            for (int page = startPage; page <= endPage; page++) {
//                final List fragments = getFragments(page, fragmentSeparator, lineSeparator);
//                final String tmp = collateFragments(fragments, fragmentSeparator, lineSeparator);
//                if (tmp.length() > 0)
//                {
//                	buf.append(tmp);
//                	buf.append(pageSeparator);
//                }
//            }
//        }
        return buf.toString();
    }

    private String getTextInternal(final int startPage, final int endPage, 
    		final String lineSeparator, final String pageSeparator)
    {
        final StringWriter output = new StringWriter();
        try 
        {
            final PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setPageSeparator(pageSeparator);
            textStripper.setLineSeparator(lineSeparator);
            textStripper.setStartPage(startPage);
            textStripper.setEndPage(endPage);
            textStripper.setPageEnd(pageSeparator);
            
//            textStripper.setAddMoreFormatting(true);
            textStripper.writeText(getPDFDocument(), output);
            return output.toString();
        }
        catch (final Exception e) 
        {
            throw new RuntimeException("Error while extracting text from document.", e);
        }
        finally 
        {
        	IOUtils.closeQuietly(output);
        }
    }
}
