package org.aksw.rex.experiments;

import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.rdf.model.Property;


public class ExampleGeneratorFactory {
	
	private static ExampleGeneratorFactory instance;
	
	public synchronized static ExampleGeneratorFactory getInstance(){
		if (instance == null)
			instance = new ExampleGeneratorFactory();
		return instance;
	}
	
	public ExampleGenerator getExampleGenerator(Property p, int pairs){
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

		ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
		exampleGenerator.setMaxNrOfPositiveExamples(pairs);
		exampleGenerator.setEndpoint(endpoint);
		exampleGenerator.setPredicate(p);
		return exampleGenerator;
	}

}
