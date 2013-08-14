/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.controller;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.aksw.rex.consistency.ConsistencyChecker;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.uris.URIGenerator;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.w3c.dom.xpath.XPathExpression;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author ngonga
 */
public class RexController {
    
    ExampleGenerator exampleGenerator;
    DomainIdentifier di;
    Property property;
    XPathLearner xpath;
    URIGenerator uriGenerator;
    ConsistencyChecker consistency;
    SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
    
    public RexController(Property p, ExampleGenerator e, DomainIdentifier d, 
            XPathLearner l, ConsistencyChecker c, SparqlEndpoint s)
    {
       property = p;
       exampleGenerator = e;
       di = d;        
       xpath = l;
       consistency = c;
       endpoint = s;
    }
    
    /** Runs the extraction pipeline
     * 
     * @return A set of triples
     * @throws Exception If URI generation does not work
     */
    public Set<Triple> run() throws Exception
    {
        Set<Pair<Resource, Resource>> posExamples = exampleGenerator.getPositiveExamples();
        Set<Pair<Resource, Resource>> negExamples = exampleGenerator.getNegativeExamples();
        URL domain = di.getDomain(property, posExamples, negExamples, true);
        List<Pair<XPathExpression,XPathExpression>> expressions = xpath.getXPathExpressions(posExamples, negExamples, domain);
        Set<ExtractionResult> results = xpath.getExtractionResults(expressions);
        Set<Triple> triples = uriGenerator.getTriples(results, property);
        triples = consistency.getConsistentTriples(triples, consistency.generateAxioms(endpoint));
        return triples;
    }
}
