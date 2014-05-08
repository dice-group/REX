package org.aksw.rex.results;

public class ExtractionResultImpl implements ExtractionResult {

    private String subject;
    private String object;
    private String pageURL;

    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#getSubject()
     */
    @Override
	public String getSubject() {
        return subject;
    }

    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#getObject()
     */
    @Override
	public String getObject() {
        return object;
    }

    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#setSubject(java.lang.String)
     */
    @Override
	public void setSubject(String s) {
        subject = s;
    }

    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#setObject(java.lang.String)
     */
    @Override
	public void setObject(String o) {
        object = o;
    }
    /**
     * constructor for filling in the subject, object and predicate
     * @param subject
     * @param object
     * @param predicate
     */
    public ExtractionResultImpl(String s, String o, String p)
    {
        subject = s;
        object = o;
        pageURL = p;
    }
    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#getPageURL()
     */
    @Override
	public String getPageURL() {
		return pageURL;
	}
    /* (non-Javadoc)
     * @see org.aksw.rex.results.ExtractionResult#setPageURL(java.lang.String)
     */
    @Override
	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
	}
    
}
