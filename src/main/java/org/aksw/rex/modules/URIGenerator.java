package org.aksw.rex.modules;

import java.util.Set;

import org.aksw.rex.util.Pair;

import com.hp.hpl.jena.graph.Triple;

public interface URIGenerator {
    Set<Triple> getTriples(Set<Pair<ExtractionResult, ExtractionResult>> posNegEx);

}
