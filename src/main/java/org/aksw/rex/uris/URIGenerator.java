package org.aksw.rex.uris;

import java.util.Set;

import org.aksw.rex.results.ExtractionResult;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

import edu.stanford.nlp.util.Quadruple;

public interface URIGenerator {
    Set<Quadruple<Node, Node, Node, String>> getTriples(Set<ExtractionResult> result, Property p) throws Exception;
}
