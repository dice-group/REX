package org.aksw.rex.uris;

import java.util.Set;

import org.aksw.rex.results.ExtractionResult;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;

import edu.stanford.nlp.util.Quadruple;

/**
 * given a set of extraction results and a property this class should generate
 * triples with context,i.e., extraction page URI
 * 
 * @author r.usbeck
 * 
 */
public interface URIGenerator {
	/**
	 * 
	 * @param result set of extraction results, i.e. XPATHs
	 * @param predicate
	 * @return set of quadruples containing entity linked triples and context nodes
	 * @throws Exception
	 */
	Set<Quadruple<Node, Node, Node, String>> getTriples(Set<ExtractionResult> result, Property p) throws Exception;
}
