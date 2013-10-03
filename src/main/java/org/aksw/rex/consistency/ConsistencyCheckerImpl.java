/**
 * 
 */
package org.aksw.rex.consistency;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Lorenz Buehmann
 *
 */
public class ConsistencyCheckerImpl implements ConsistencyChecker{
	
	private static final Logger logger = Logger.getLogger(ConsistencyCheckerImpl.class.getName());
	
	private double accuracyThreshold = 0.6;
	private SparqlEndpoint endpoint; 
	private SPARQLReasoner reasoner;
	
	public ConsistencyCheckerImpl(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), "sparql-cache");
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.consistency.ConsistencyChecker#getConsistentTriples(java.util.Set, java.util.Set)
	 */
	@Override
	public Set<Triple> getConsistentTriples(Set<Triple> triples, Set<Axiom> axioms) {
		
		Individual subject;
		Individual object;
		ObjectProperty property;
		for (Iterator<Triple> iter = triples.iterator(); iter.hasNext();) {
			Triple triple = iter.next();
			
			subject = new Individual(triple.getSubject().getURI());
			object = new Individual(triple.getObject().getURI());
			property = new ObjectProperty(triple.getPredicate().getURI());
			
			//get the types of the subject
			Set<NamedClass> subjectTypes = reasoner.getTypes(subject);
			
			//get the types of the object
			Set<NamedClass> objectTypes = reasoner.getTypes(object);
			
			//get domain of the property
			Set<NamedClass> domains = new HashSet<NamedClass>();
			for (Axiom axiom : axioms) {
				if(axiom instanceof ObjectPropertyDomainAxiom){
					if(((ObjectPropertyDomainAxiom) axiom).getProperty().equals(property)){
						domains.add(((ObjectPropertyDomainAxiom) axiom).getDomain().asNamedClass());
					}
				}
			}
			
			//get range of the property
			Set<NamedClass> ranges = new HashSet<NamedClass>();
			for (Axiom axiom : axioms) {
				if(axiom instanceof ObjectPropertyRangeAxiom){
					if(((ObjectPropertyRangeAxiom) axiom).getProperty().equals(property)){
						ranges.add(((ObjectPropertyRangeAxiom) axiom).getRange().asNamedClass());
					}
				}
			}
			
			boolean consistent = true;
			DisjointClassesAxiom axiom;
			//check if there is a disjointness statement between asserted types of the subject and the domains of the property
			for (NamedClass type : subjectTypes) {
				for (NamedClass domain : domains) {
					axiom = new DisjointClassesAxiom(type, domain);
					if(axioms.contains(axiom)){
						consistent = false;
					}
				}
			}
			
			//check if there is a disjointness statement between asserted types of the subject and the domains of the property
			for (NamedClass type : objectTypes) {
				for (NamedClass range : ranges) {
					axiom = new DisjointClassesAxiom(type, range);
					if(axioms.contains(axiom)){
						consistent = false;
					}
				}
			}
			
			if(!consistent){
				logger.warn("Omitting triple " + triple);
				iter.remove();
			}
		}
		return triples;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.consistency.ConsistencyChecker#generateAxioms(org.dllearner.kb.sparql.SparqlEndpoint)
	 */
	@Override
	public Set<Axiom> generateAxioms(SparqlEndpoint endpoint) {
		Set<Axiom> disjointnessAxioms = new HashSet<Axiom>();
		
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, "cache/sparql");
		reasoner.precomputeClassPopularity();
		
		DisjointClassesLearner learner = new DisjointClassesLearner(ks);
		learner.setReasoner(reasoner);
		
		//get all OWL classes in KB
		Set<NamedClass> classes = reasoner.getOWLClasses();
		
		//compute the disjoint classes for each class
		for (NamedClass cls : classes) {
			learner.setClassToDescribe(cls);
			learner.start();
			List<Axiom> currentlyBestAxioms = learner.getCurrentlyBestAxioms(accuracyThreshold);
			disjointnessAxioms.addAll(currentlyBestAxioms);
		}
		
		return disjointnessAxioms;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.consistency.ConsistencyChecker#generateAxioms(org.dllearner.kb.sparql.SparqlEndpoint, org.dllearner.core.owl.ObjectProperty[])
	 */
	@Override
	public Set<Axiom> generateAxioms(SparqlEndpoint endpoint, ObjectProperty... properties) {
		Set<Axiom> axioms = new HashSet<Axiom>();
		
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks, "cache/sparql");
		reasoner.precomputeClassPopularity();
		
		ObjectPropertyDomainAxiomLearner domainLearner = new ObjectPropertyDomainAxiomLearner(ks);
		domainLearner.setReasoner(reasoner);
		ObjectPropertyRangeAxiomLearner rangeLearner = new ObjectPropertyRangeAxiomLearner(ks);
		rangeLearner.setReasoner(reasoner);
		DisjointClassesLearner disjointnessLearner = new DisjointClassesLearner(ks);
		disjointnessLearner.setReasoner(reasoner);
		
		//we only compute disjointness axioms between domain and range of the properties to all other classes
		for (ObjectProperty prop : properties) {
			
			//get domains of the property
			SortedSet<NamedClass> domains = reasoner.getDomains(prop);
			//if domain is not available in the knowledge base, we try to learn an appropriate one
			if(domains.isEmpty() || (domains.size() == 1 && domains.contains(new Thing()))){
				try {
					domainLearner.setPropertyToDescribe(prop);
					domainLearner.start();
					List<Axiom> domainAxioms = domainLearner.getCurrentlyBestAxioms(accuracyThreshold);
					axioms.addAll(domainAxioms);
					for (Axiom axiom : domainAxioms) {
						domains.add(((ObjectPropertyDomainAxiom)axiom).getDomain().asNamedClass());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			//get ranges of the property
			SortedSet<NamedClass> ranges = reasoner.getRanges(prop);
			//if range is not available in the knowledge base, we try to learn an appropriate one
			if(ranges.isEmpty() || (ranges.size() == 1 && ranges.contains(new Thing()))){
				try {
					rangeLearner.setPropertyToDescribe(prop);
					rangeLearner.start();
					List<Axiom> rangeAxioms = rangeLearner.getCurrentlyBestAxioms(accuracyThreshold);
					axioms.addAll(rangeAxioms);
					for (Axiom axiom : rangeAxioms) {
						domains.add(((ObjectPropertyRangeAxiom)axiom).getRange().asNamedClass());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//compute the disjoint classes for each class
			for (NamedClass cls : Sets.union(domains, ranges)) {
				disjointnessLearner.setClassToDescribe(cls);
				disjointnessLearner.start();
				List<Axiom> disjointnessAxioms = disjointnessLearner.getCurrentlyBestAxioms(accuracyThreshold);
				axioms.addAll(disjointnessAxioms);
			}
		}
		
		return axioms;
	}
	
}
