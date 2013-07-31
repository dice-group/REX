package org.aksw.rex.modules;

import java.util.Set;

import org.aksw.rex.util.Pair;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface ExampleGenerator {
    // TODO declare param conform to Jena API
    // void setEndpoint(Endpoint e);

    void setPredicate(Property p);

    Set<Pair<Resource, Resource>> getPositiveExamples();

    void getNegativeExamples();

}
