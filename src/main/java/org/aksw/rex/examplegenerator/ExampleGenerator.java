package org.aksw.rex.examplegenerator;

import java.util.Set;

import org.aksw.rex.util.Pair;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface ExampleGenerator {

    void setEndpoint(SparqlEndpoint e);

    void setPredicate(Property p);

    Set<Pair<Resource, Resource>> getPositiveExamples();

    Set<Pair<Resource, Resource>> getNegativeExamples();

}
