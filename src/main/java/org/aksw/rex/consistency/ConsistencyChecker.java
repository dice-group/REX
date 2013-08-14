package org.aksw.rex.consistency;

import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.graph.Triple;

public interface ConsistencyChecker {
    Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms);
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint);
}
