package org.aksw.rex.experiments;

import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.examplegenerator.UniformDistributionGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.rdf.model.Property;

/**
 * A factory to produce a example generator,  multi-thread safe
 * @author r.usbeck
 *
 */
public class ExampleGeneratorFactory {
	
	private static ExampleGeneratorFactory instance;
	
	public synchronized static ExampleGeneratorFactory getInstance(){
		if (instance == null)
			instance = new ExampleGeneratorFactory();
		return instance;
	}
	/**
	 * 
	 * @param predicate
	 * @param number of pairs
	 * @param random distribution or uniform distribution
	 * @return ExampleGenerator
	 */
	public ExampleGenerator getExampleGenerator(Property p, int pairs, boolean random){
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		ExampleGenerator exampleGenerator = null;
		if (random){
			exampleGenerator = new UniformDistributionGenerator();
		}else{			
			exampleGenerator = new SimpleExampleGenerator();
		}
		exampleGenerator.setMaxNrOfPositiveExamples(pairs);
		exampleGenerator.setEndpoint(endpoint);
		exampleGenerator.setPredicate(p);
		return exampleGenerator;
	}

}
