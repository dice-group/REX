package org.aksw.rex.uris;

import java.util.Set;

import org.aksw.rex.results.ExtractionResult;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

public interface URIGenerator {
    Set<Triple> getTriples(Set<ExtractionResult> result, Property p) throws Exception;

}
