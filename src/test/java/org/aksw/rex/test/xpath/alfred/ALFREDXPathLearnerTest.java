package org.aksw.rex.test.xpath.alfred;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import rules.xpath.XPathRule;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


public class ALFREDXPathLearnerTest {
	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDXPathLearnerTest.class);
	
	private ALFREDXPathLearner learner;
	private Set<Pair<Resource, Resource>> posExamples; 
	
	@Before
	public void init(){
		this.learner = new ALFREDXPathLearner();
		
		posExamples = new HashSet<Pair<Resource,Resource>>();
		Resource r1 = ResourceFactory.createResource("http://dbpedia.org/resource/Tom_Cruise");		
		Resource r2 = ResourceFactory.createResource("http://dbpedia.org/resource/Minority_Report");
		Resource r3 = ResourceFactory.createResource("http://dbpedia.org/resource/Don_Johnson");
		Resource r4 = ResourceFactory.createResource("http://dbpedia.org/resource/Miami_Vice");		
		posExamples.add(new Pair<Resource, Resource>(r1, r2));
		posExamples.add(new Pair<Resource, Resource>(r3, r4));
	}
	
	@Test
	public void testGetXPathExpressions(){
		List<Pair<XPathRule, XPathRule>> xpaths = this.learner.getXPathExpressions(posExamples, null, null);
		Assert.assertEquals(1, xpaths.size());
		log.debug(xpaths.get(0).getLeft().toString());
		log.debug(xpaths.get(0).getRight().toString());
	}
}
