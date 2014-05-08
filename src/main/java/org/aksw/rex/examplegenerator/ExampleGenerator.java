package org.aksw.rex.examplegenerator;

import java.util.Set;

import org.aksw.rex.util.Pair;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * This class generates examples, i.e., subject object pairs for a given
 * predicate, for generating XPaths.
 * 
 * @author r.usbeck
 * 
 */
public interface ExampleGenerator {
	/**
	 * sets the SPARQL endpoint used for extracting the pairs
	 * 
	 * @param e
	 */
	void setEndpoint(SparqlEndpoint e);

	/**
	 * sets the predicate for the subject-object pairs
	 * 
	 * @param p
	 */
	void setPredicate(Property p);

	/**
	 * returns a number of valid subject-object pairs, given a certain knowledge
	 * base and the predicate
	 * 
	 * @return
	 */
	Set<Pair<Resource, Resource>> getPositiveExamples();

	/**
	 * returns all subject-object pairs, given a certain knowledge base and the
	 * predicate
	 * 
	 * @return
	 */
	Set<Pair<Resource, Resource>> getAllPositiveExamples();

	/**
	 * returns a number of invalid subject-object pairs, given a certain
	 * knowledge base and the predicate
	 * 
	 * @return
	 */
	Set<Pair<Resource, Resource>> getNegativeExamples();

	/**
	 * sets the number of positive examples to be extracted from the endpoint.
	 * This influences significantly the runtime
	 * 
	 * @param maxNrOfPositiveExamples
	 */
	void setMaxNrOfPositiveExamples(int maxNrOfPositiveExamples);

	/**
	 * sets the number of positive examples to be extracted from the endpoint.
	 * This influences significantly the runtime
	 * 
	 * @param maxNrOfPositiveExamples
	 */
	void setMaxNrOfNegativeExamples(int maxNrOfNegativeExamples);
}
