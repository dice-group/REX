package org.aksw.rex.consistency;

import java.util.Set;
import com.hp.hpl.jena.graph.Triple;
import org.dllearner.core.owl.Axiom;
import org.dllearner.kb.sparql.SparqlEndpoint;

public interface ConsistencyChecker {
    Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms);
    Set<Axiom> generateAxioms(SparqlEndpoint endpoint);
}
