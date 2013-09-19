/**
 * 
 */
package org.aksw.rex.xpath;

/**
 * @author Lorenz Buehmann
 *
 */
public class XPathExtractionRule {
	
	private String subjectXPathExpression;
	private String objectXPathExpression;
	
	public XPathExtractionRule(String subjectXPathExpression, String objectXPathExpression) {
		super();
		this.subjectXPathExpression = subjectXPathExpression;
		this.objectXPathExpression = objectXPathExpression;
	}
	
	/**
	 * @return the subjectXPathExpression
	 */
	public String getSubjectXPathExpression() {
		return subjectXPathExpression;
	}
	
	/**
	 * @return the objectXPathExpression
	 */
	public String getObjectXPathExpression() {
		return objectXPathExpression;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Subject:" + subjectXPathExpression + "\nObject" + objectXPathExpression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((objectXPathExpression == null) ? 0 : objectXPathExpression.hashCode());
		result = prime * result + ((subjectXPathExpression == null) ? 0 : subjectXPathExpression.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XPathExtractionRule other = (XPathExtractionRule) obj;
		if (objectXPathExpression == null) {
			if (other.objectXPathExpression != null)
				return false;
		} else if (!objectXPathExpression.equals(other.objectXPathExpression))
			return false;
		if (subjectXPathExpression == null) {
			if (other.subjectXPathExpression != null)
				return false;
		} else if (!subjectXPathExpression.equals(other.subjectXPathExpression))
			return false;
		return true;
	}
	
	

}
