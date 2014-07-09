/**
 * 
 */
package org.aksw.rex.consistency;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Lorenz Buehmann
 * 
 */
public class ConsistencyCheckerImplTest {

    // SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
    // SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveAKSW();
    SparqlEndpoint endpoint;
    String namespace = "http://dbpedia.org/ontology/";
    ConsistencyChecker consistencyChecker;
    ObjectProperty property = new ObjectProperty("http://dbpedia.org/ontology/director");
    static final String DBR = "http://dbpedia.org/resource/";
    static final String DBO = "http://dbpedia.org/ontology/";
    static final String REX = "http://aksw.org/resource/";

    @Before
    public void setup() {
        try {
            endpoint = new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"));
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        consistencyChecker = new ConsistencyCheckerImpl(endpoint, namespace);

    }

    /**
     * Test method for
     * {@link org.aksw.rex.consistency.ConsistencyCheckerImpl#getConsistentTriples(java.util.Set, java.util.Set)}.
     */
    @Test
    public void testGetConsistentTriples() {
        Set<Triple> triples = new HashSet<Triple>();
        Triple triple = Triple.create(
                Node.createURI(DBR + "Die_Hard"),
                Node.createURI(property.getName()),
                Node.createURI(DBR + "Steven_Spielberg"));
        triples.add(triple);
        triple = Triple.create(
                Node.createURI(DBR + "Leipzig"),
                Node.createURI(property.getName()),
                Node.createURI(DBR + "Steven_Spielberg"));
        triples.add(triple);
        triple = Triple.create(
                Node.createURI(DBR + "The_Lord_of_the_Rings:_The_Fellowship_of_the_Ring"),
                Node.createURI(property.getName()),
                Node.createURI(REX + "Axel_Ngonga"));
        triples.add(triple);

        Set<Triple> consistentTriples = consistencyChecker.getConsistentTriples(triples);
        // first triple should be fine
        // second triple should be omitted because
        // * dbr:Leipzig is typed as dbo:Place
        // * the generated domain of dbo:director is dbo:Work
        // * a disjointness between dbo:Place and dbo:Work is detected based on the instances data
        // third triple should be fine
        assertTrue(consistentTriples.size() == 2);
    }

    /**
     * Test method for
     * {@link org.aksw.rex.consistency.ConsistencyCheckerImpl#generateAxioms(org.dllearner.kb.sparql.SparqlEndpoint, org.dllearner.core.owl.ObjectProperty[])}
     * .
     */
    @Test
    public void testGenerateAxiomsSparqlEndpointObjectPropertyArray() {
        Set<Axiom> axioms = consistencyChecker.generateAxioms(endpoint, property);
    }

}
