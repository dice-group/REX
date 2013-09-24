/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.test.domainidentifier;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.aksw.rex.domainidentifier.GoogleDomainIdentifier;
import org.aksw.rex.util.Pair;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Test for domain identifier
 * 
 * @author ngonga
 */
public class DomainIdentifierTest {
	private org.slf4j.Logger log = LoggerFactory.getLogger(DomainIdentifierTest.class);

	@Test
	public void testDomainIdentifier() throws Exception {
		Set<Pair<Resource, Resource>> posExamples = new HashSet<Pair<Resource, Resource>>();
		Resource r1 = ResourceFactory.createResource("http://dbpedia.org/resource/Tom_Cruise");
		Resource r2 = ResourceFactory.createResource("http://dbpedia.org/resource/Minority_Report");
		Set<Pair<Resource, Resource>> negExamples = new HashSet<Pair<Resource, Resource>>();
		posExamples.add(new Pair(r1, r2));
		Resource r3 = ResourceFactory.createResource("http://dbpedia.org/resource/Don_Johnson");
		Resource r4 = ResourceFactory.createResource("http://dbpedia.org/resource/Miami_Vice");
		posExamples.add(new Pair(r3, r4));
		GoogleDomainIdentifier gdi = new GoogleDomainIdentifier();
		URL domain = gdi.getDomain(ResourceFactory.createProperty("http://dbpedia.org/ontology/starring"), posExamples, negExamples, true);
		boolean correct = false;
		log.debug(domain.toString());
		if (domain.equals(new URL("http://www.imdb.com")))
			correct = true;
		assertTrue("Should be imdb", correct);
		
		log.debug(r1.toString());
	}
}
