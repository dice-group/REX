package org.aksw.rex.results;
/**
 * DOA interface to keep subject, object pairs, given a prominence url
 * @author r.usbeck
 *
 */
public interface ExtractionResult {
	/**
	 * 
	 * @return subject
	 */
    public String getSubject();
    /**
	 * 
	 * @return object
	 */
    public String getObject();
    /**
	 * 
	 * @param subject
	 */
    public void setSubject(String s);
    /**
	 * 
	 * @param object
	 */
    public void setObject(String o);
    /**
	 * 
	 * @return prominence url
	 */
	String getPageURL();
	/**
	 * 
	 * @param prominence url
	 */
	void setPageURL(String pageURL);
}
