package org.aksw.rex.modules;

import java.util.Set;

import org.aksw.rex.util.Axiom;

import com.hp.hpl.jena.graph.Triple;

public interface ConsistencyChecker {
    Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms);

}
