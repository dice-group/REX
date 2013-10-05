package org.aksw.rex.results;

public interface ExtractionResult {
    public String getSubject();
    public String getObject();
    public void setSubject(String s);
    public void setObject(String o);
	String getPageURL();
	void setPageURL(String pageURL);
}
