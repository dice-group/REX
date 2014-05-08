package org.aksw.rex.controller.dao;

import java.util.ArrayList;

/**
 * Interface for supplying golden XPath Rules to extract Subject-Object pairs,
 * given a certain predicate
 * 
 * @author r.usbeck
 * 
 */
public interface PropertyXPathSupplier {

	/**
	 * 
	 * @return a list of triples (XPATH, Property, Domain)
	 */
	ArrayList<RexPropertiesWithGoldstandard> getPropertiesToCheck();

}
