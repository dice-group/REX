package org.aksw.rex.results;

public class ExtractionResultImpl implements ExtractionResult {

    private String subject;
    private String object;
    private String pageURL;

    @Override
	public String getSubject() {
        return subject;
    }

    @Override
	public String getObject() {
        return object;
    }

    @Override
	public void setSubject(String s) {
        subject = s;
    }

    @Override
	public void setObject(String o) {
        object = o;
    }
    
    public ExtractionResultImpl(String s, String o, String p)
    {
        subject = s;
        object = o;
        pageURL = p;
    }
    @Override
	public String getPageURL() {
		return pageURL;
	}
    @Override
	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
	}
    
}
