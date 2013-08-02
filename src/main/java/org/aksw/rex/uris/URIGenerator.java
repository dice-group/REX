package org.aksw.rex.uris;

import org.aksw.rex.modules.results.ExtractionResult;
import java.util.Set;

import org.aksw.rex.util.Pair;

import com.hp.hpl.jena.graph.Triple;

public interface URIGenerator {
    Set<Triple> getTriples(Set<Pair<ExtractionResult, ExtractionResult>> posNegEx);

}
