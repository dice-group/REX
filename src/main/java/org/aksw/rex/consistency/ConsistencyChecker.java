package org.aksw.rex.consistency;

import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.graph.Triple;

public interface ConsistencyChecker {
	/**
	 * In order to be able to check the consistency of each triple <i>t</i>, the set of axioms should contain:
	 * <ul>
	 * <li>Disjointness of classes in the schema</li>
	 * <li>Domain and range for the predicates of each <i>t</i></li>
	 * <li>Class assertions for subjects and objects of <i>t</i></li>
	 * </ul>
	 * 
	 * @param triples
	 * @param axioms
	 * @return
	 */
    Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms);
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint);
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint, ObjectProperty... properties);
}
