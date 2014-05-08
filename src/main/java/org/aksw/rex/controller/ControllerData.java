package org.aksw.rex.controller;

/**
 * DAO class to provide evaluation data
 * 
 * @author r.usbeck
 * 
 */
public class ControllerData {
	/**
	 * index is meant to be the lucene index which stores (url, htmlcode) pairs
	 * for the evaluation
	 */
	public String index;
	public String dbpediaProperty;
	public String urlDomain;
	public String subjectRule;
	public String objectRule;

	public ControllerData(String index, String dbpediaProperty, String urlDomain, String subjectRule, String objectRule) {
		this.index = index;
		this.dbpediaProperty = dbpediaProperty;
		this.urlDomain = urlDomain;
		this.subjectRule = subjectRule;
		this.objectRule = objectRule;
	}
}
