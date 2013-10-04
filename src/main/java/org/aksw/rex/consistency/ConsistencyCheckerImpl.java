/**
 * 
 */
package org.aksw.rex.consistency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Lorenz Buehmann
 *
 */
public class ConsistencyCheckerImpl implements ConsistencyChecker{
	
	private static final Logger logger = Logger.getLogger(ConsistencyCheckerImpl.class.getName());
	
	private static final NamedClass OWL_THING = new NamedClass("http://www.w3.org/2002/07/owl#Thing");
	
	private double accuracyThreshold = 0.6;
	private SparqlEndpoint endpoint; 
	private SparqlEndpointKS ks;
	private SPARQLReasoner reasoner;
	private DisjointClassesLearner disjointnessLearner;
	private String namespace;
	
	LoadingCache<Individual, Set<NamedClass>> typesCache = CacheBuilder.newBuilder()
		       .maximumSize(10000)
		       .build(
		           new CacheLoader<Individual, Set<NamedClass>>() {
		             public Set<NamedClass> load(Individual individual)  {
		               return reasoner.getTypes(individual, namespace);
		             }
		           });
	
	LoadingCache<ObjectProperty, Set<NamedClass>> domainCache = CacheBuilder.newBuilder()
		       .maximumSize(100)
		       .build(
		           new CacheLoader<ObjectProperty, Set<NamedClass>>() {
		             public Set<NamedClass> load(ObjectProperty property)  {
		               return getDomain(ks, property);
		             }
		           });
	
	LoadingCache<ObjectProperty, Set<NamedClass>> rangeCache = CacheBuilder.newBuilder()
		       .maximumSize(100)
		       .build(
		           new CacheLoader<ObjectProperty, Set<NamedClass>>() {
		             public Set<NamedClass> load(ObjectProperty property)  {
		               return getRange(ks, property);
		             }
		           });
	
	public ConsistencyCheckerImpl(SparqlEndpoint endpoint, String namespace) {
		this.endpoint = endpoint;
		this.namespace = namespace;
		ks = new SparqlEndpointKS(endpoint);
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), "sparql-cache");
		disjointnessLearner = new DisjointClassesLearner(ks);
		disjointnessLearner.setReasoner(reasoner);
	}
	
	public ConsistencyCheckerImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
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
			Set<NamedClass> subjectTypes = reasoner.getTypes(subject, namespace);
			
			//get the types of the object
			Set<NamedClass> objectTypes = reasoner.getTypes(object, namespace);
			
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
		
		DisjointClassesLearner disjointnessLearner = new DisjointClassesLearner(ks);
		disjointnessLearner.setReasoner(reasoner);
		
		//we only compute disjointness axioms between domain and range of the properties to all other classes
		for (ObjectProperty property : properties) {
			
			//get the domain and add it to the set of axioms
			Set<NamedClass> domains = getDomain(ks, property);
			for (NamedClass domain : domains) {
				axioms.add(new ObjectPropertyDomainAxiom(property, domain));
			}
			
			//get the range and add it to the set of axioms
			Set<NamedClass> ranges = getRange(ks, property);
			for (NamedClass range : ranges) {
				axioms.add(new ObjectPropertyRangeAxiom(property, range));
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
	
	/**
	 * Returns either the existing domain or tries to generate one based on a machine learning algorithm.
	 * @param property
	 * @return
	 */
	private Set<NamedClass> getDomain(SparqlEndpointKS ks, ObjectProperty property){
		ObjectPropertyDomainAxiomLearner domainLearner = new ObjectPropertyDomainAxiomLearner(ks);
		domainLearner.setReasoner(reasoner);
		//get domains of the property
		SortedSet<NamedClass> domains = reasoner.getDomains(property);
		//if domain is not available in the knowledge base, we try to learn an appropriate one
		if(domains.isEmpty() || (domains.size() == 1 && domains.contains(OWL_THING))){
			try {
				domainLearner.setPropertyToDescribe(property);
				domainLearner.start();
				List<Axiom> domainAxioms = domainLearner.getCurrentlyBestAxioms(accuracyThreshold);
				
				for (Axiom axiom : domainAxioms) {
					domains.add(((ObjectPropertyDomainAxiom)axiom).getDomain().asNamedClass());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//filter out domain classes not starting with namespace
		filterByNamespace(domains);
		return domains;
	}
	
	/**
	 * Returns either the existing range or tries to generate one based on a machine learning algorithm.
	 * @param property
	 * @return
	 */
	private Set<NamedClass> getRange(SparqlEndpointKS ks, ObjectProperty property){
		ObjectPropertyRangeAxiomLearner rangeLearner = new ObjectPropertyRangeAxiomLearner(ks);
		rangeLearner.setReasoner(reasoner);
		
		//get ranges of the property
		SortedSet<NamedClass> ranges = reasoner.getRanges(property);
		//if range is not available in the knowledge base, we try to learn an appropriate one
		if(ranges.isEmpty() || (ranges.size() == 1 && ranges.contains(OWL_THING))){
			try {
				rangeLearner.setPropertyToDescribe(property);
				rangeLearner.start();
				List<Axiom> rangeAxioms = rangeLearner.getCurrentlyBestAxioms(accuracyThreshold);
				
				for (Axiom axiom : rangeAxioms) {
					ranges.add(((ObjectPropertyRangeAxiom)axiom).getRange().asNamedClass());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//filter out range classes not starting with namespace
		filterByNamespace(ranges);
		return ranges;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.consistency.ConsistencyChecker#getConsistentTriples(java.util.Set)
	 */
	@Override
	public Set<Triple> getConsistentTriples(Set<Triple> triples) {
		
		for (Iterator<Triple> iter = triples.iterator(); iter.hasNext();) {
			Triple triple = iter.next();
			
			try {
				if(leadsToInconsistentKnowledgeBase(triple)){
					logger.warn("Omitting triple " + triple);
					iter.remove();
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return triples;
	}
	
	private boolean leadsToInconsistentKnowledgeBase(Triple triple) throws ExecutionException{
		logger.debug("Checking triple " + triple);
		Individual subject = new Individual(triple.getSubject().getURI());
		Individual object = new Individual(triple.getObject().getURI());
		ObjectProperty property = new ObjectProperty(triple.getPredicate().getURI());
		
		//get the types of the subject
		Set<NamedClass> subjectTypes = typesCache.get(subject);
		logger.debug("Subject types: " + subjectTypes);
		
		//get the types of the object
		Set<NamedClass> objectTypes = typesCache.get(object);
		logger.debug("Object types: " + objectTypes);
		
		//get domain of the property
		Set<NamedClass> domains = domainCache.get(property);
		logger.debug("Domain: " + domains);
		
		//get range of the property
		Set<NamedClass> ranges = rangeCache.get(property);
		logger.debug("Range: " + ranges);
		
		//try to generate disjointness axioms between subject types and property domain
		for (NamedClass type : subjectTypes) {
			for (NamedClass domain : domains) {
				EvaluatedAxiom axiom = disjointnessLearner.computeDisjointess(type, domain);
				logger.debug(axiom);
				if(axiom.getScore().getAccuracy() >= accuracyThreshold){
					return true;
				}
			}
		}
		
		//try to generate disjointness axioms between object types and property range
		for (NamedClass type : objectTypes) {
			for (NamedClass range : ranges) {
				EvaluatedAxiom axiom = disjointnessLearner.computeDisjointess(type, range);
				logger.debug(axiom);
				if(axiom.getScore().getAccuracy() >= accuracyThreshold){
					return true;
				}
			}
		}
		//knowledge base remains consistent according to the rules we checked in this method
		return false;
	}
	
	private void filterByNamespace(Collection<NamedClass> classes){
		if(namespace != null){
			for (Iterator<NamedClass> iterator = classes.iterator(); iterator.hasNext();) {
				NamedClass cls = iterator.next();
				if(!cls.getName().startsWith(namespace)){
					iterator.remove();
				}
			}
		}
	}
	
}
