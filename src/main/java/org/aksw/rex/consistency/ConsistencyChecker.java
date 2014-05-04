package org.aksw.rex.consistency;

import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.graph.Triple;

public interface ConsistencyChecker {
	/**
	 * Returns a subset of consistent triples by taking schema axioms into account
	 * 
	 * @param triples A set of triples for which a consistent subset of triples according to the given schema axiom will be returned.
	 * @return A consistent subset of the given set of triples.
	 */
	Set<Triple> getConsistentTriples(Set<Triple> triples);
	
	/**
	 * In order to be able to check the consistency of each triple <i>t</i>, the set of axioms should contain:
	 * <ul>
	 * <li>Disjointness of classes in the schema</li>
	 * <li>Domain and range for the predicates of each <i>t</i></li>
	 * <li>Class assertions for subjects and objects of <i>t</i></li>
	 * </ul>
	 * 
	 * @param triples A set of triples for which a consistent subset of triples according to the given schema axiom will be returned.
	 * @param axioms The schema axiom which are used to compute a consistent subset of the given triples.
	 * @return A consistent subset of the given set of triples.
	 */
    Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms);
    
    /**
     * Generates a set of schema axioms for the given SPARQL endpoint.
     * @param endpoint The SPARQL endpoint
     * @return A set of schema axioms for the given SPARQL endpoint
     */
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint);
    
    /**
     * Generates domain, range for the given properties. Additionally, it generates disjointness axioms for the domain and range classes.
     * @param endpoint The SPARQL endpoint
     * @param properties The properties for which domain and range axioms are generated
     * @return A set of domain, range and disjointess axioms for the given SPARQL endpoint
     */
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint, ObjectProperty... properties);
}
