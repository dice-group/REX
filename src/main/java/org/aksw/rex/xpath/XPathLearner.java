package org.aksw.rex.xpath;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.util.Pair;

import rules.xpath.XPathRule;

import com.hp.hpl.jena.rdf.model.Resource;
/**
 * Interface for XPathLearner modules
 * @author r.usbeck
 *
 */
public interface XPathLearner {
	
	
	/**
	 * this methods gets a number of training points and returns a list of possible pairs of extraction rules
	 * @param posExamples
	 * @param negExamples
	 * @param Domain
	 * @return
	 */
	List<Pair<XPathRule, XPathRule>> getXPathExpressions(Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples, URL Domain);
    /**
     * this method does not need examples to learn extraction rules
     * @param expressions
     * @param domain
     * @return
     */
    Set<ExtractionResult> getExtractionResults(List<Pair<XPathRule, XPathRule>> expressions, URL domain);
    
    /**
	 * Whether to use exact matching or containment for node matching task in DOM tree.
	 * @param useExactMatch the useExactMatch to set
	 */
    void setUseExactMatch(boolean useExactMatch);

}
