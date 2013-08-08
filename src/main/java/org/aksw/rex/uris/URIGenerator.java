package org.aksw.rex.uris;

import java.util.Set;

import org.aksw.commons.collections.Pair;
import org.aksw.rex.results.ExtractionResult;

import com.hp.hpl.jena.graph.Triple;

public interface URIGenerator {
    Set<Triple> getTriples(Set<Pair<ExtractionResult, ExtractionResult>> posNegEx) throws Exception;

}
