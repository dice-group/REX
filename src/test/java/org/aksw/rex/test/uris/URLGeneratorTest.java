package org.aksw.rex.test.uris;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.results.ExtractionResultImpl;
import org.aksw.rex.uris.URIGeneratorAGDISTIS;
import org.aksw.rex.uris.URIGeneratorImpl;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;

public class URLGeneratorTest {
	private org.slf4j.Logger log = LoggerFactory.getLogger(URLGeneratorTest.class);

	@Test
	public void testAGDISTIS() throws Exception {
		URIGeneratorAGDISTIS gen = new URIGeneratorAGDISTIS();
		// filling the test cases
		ExtractionResultImpl res = new ExtractionResultImpl("Tom Cruise", "Mission Impossible");
		Property p = ResourceFactory.createProperty("http://dbpedia.org/ontology/starring");
		Set<ExtractionResult> pair = new HashSet<ExtractionResult>();
		pair.add(res);
		Set<Triple> result = gen.getTriples(pair, p);
		for (Triple r : result) {
			assertTrue(r.getSubject().getURI().equals("http://dbpedia.org/resource/Tom_Cruise"));
			assertTrue(r.getObject().getURI().equals("http://dbpedia.org/resource/Mission:_Impossible_–_Ghost_Protocol"));
		}
	}

	@Test
	public void testAGDISTISNotExistingValues() throws Exception {
		URIGeneratorAGDISTIS gen = new URIGeneratorAGDISTIS();
		// filling the test cases
		ExtractionResultImpl res = new ExtractionResultImpl("Axel Ngonga", "Mission Impossible");
		Property p = ResourceFactory.createProperty("http://dbpedia.org/ontology/starring");
		Set<ExtractionResult> pair = new HashSet<ExtractionResult>();
		pair.add(res);
		Set<Triple> result = gen.getTriples(pair, p);
		for (Triple r : result) {
			assertTrue(r.getSubject().getURI().equals("http://aksw.org/resource/Axel+Ngonga"));
			assertTrue(r.getObject().getURI().equals("http://dbpedia.org/resource/Mission:_Impossible_–_Ghost_Protocol"));
		}
	}
	
	@Test
	public void testURLGeneratorEmptySet() throws Exception {
		URIGeneratorImpl gen = new URIGeneratorImpl();
		// filling the test cases
		Set<ExtractionResult> posNegEx = null;
		Property p = null;
		Set<Triple> result = gen.getTriples(posNegEx, p);
		assertEquals("Empty Extraction result should result in empty Triples.", new HashSet<Triple>(), result);
	}

	@Test
	public void testURLGeneratorExistingURLs() throws Exception {
		log.debug("testURLGeneratorExistingURLs");
		URIGeneratorImpl gen = new URIGeneratorImpl();
		// filling the test cases
		ExtractionResultImpl res = new ExtractionResultImpl("Paris", "London");
		Property p = ResourceFactory.createProperty("http://dbpedia.org/ontology/near");
		Set<ExtractionResult> posNegEx = new HashSet<ExtractionResult>();
		posNegEx.add(res);
		Set<Triple> result = gen.getTriples(posNegEx, p);
		assertEquals("One Triple expected.", 1, result.size());

		boolean found = false;
		for (Triple t : result) {
			if (t.getSubject().getURI().equals("http://dbpedia.org/resource/Paris"))
				found = true;
		}
		assertTrue("Subject should be Paris", found);

		found = false;
		for (Triple t : result) {
			if (t.getObject().getURI().equals("http://dbpedia.org/resource/London"))
				found = true;
		}
		assertTrue("Object should be London", found);
	}

	@Test
	public void testURLGeneratorNonExistingURLs() throws Exception {
		URIGeneratorImpl gen = new URIGeneratorImpl();
		// filling the test cases
		ExtractionResultImpl r = new ExtractionResultImpl("AKSW", "AKSW");

		Set<ExtractionResult> posNegEx = new HashSet<ExtractionResult>();
		posNegEx.add(r);
		Property p = OWL.sameAs;
		Set<Triple> result = gen.getTriples(posNegEx, p);
		boolean found = false;
		for (Triple t : result) {
			if (t.getSubject().getURI().equals("http://aksw.org/resource#AKSW"))
				found = true;
		}
		assertTrue("Should be http://aksw.org/resource#AKSW", found);
		assertEquals("One Triple expected.", 1, result.size());
	}
}
