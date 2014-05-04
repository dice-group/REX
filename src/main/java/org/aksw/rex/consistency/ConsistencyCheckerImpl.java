/**
 * 
 */
package org.aksw.rex.consistency;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.properties.AsymmetricObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.InverseFunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.IrreflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.core.owl.AsymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.InverseFunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
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
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * A heuristically method to check for consistency in the knowledge base. This approach is sound but incomplete.
 * @author Lorenz Buehmann
 *
 */
public class ConsistencyCheckerImpl implements ConsistencyChecker{
	
	private static final Logger logger = Logger.getLogger(ConsistencyCheckerImpl.class.getName());
	
	private static final NamedClass OWL_THING = new NamedClass("http://www.w3.org/2002/07/owl#Thing");
	
	private double accuracyThreshold = 0.7;
	private String cacheDirectory = "sparql-cache";
	
	private SparqlEndpoint endpoint; 
	private SparqlEndpointKS ks;
	private String namespace;
	private QueryExecutionFactory qef;
	
	private SPARQLReasoner reasoner;
	
	private DisjointClassesLearner disjointnessLearner;
	private ObjectPropertyDomainAxiomLearner domainLearner;
	private ObjectPropertyRangeAxiomLearner rangeLearner;
	private FunctionalObjectPropertyAxiomLearner functionalLearner;
	private InverseFunctionalObjectPropertyAxiomLearner inverseFunctionalLearner;
	private AsymmetricObjectPropertyAxiomLearner asymmetricLearner;
	private IrreflexiveObjectPropertyAxiomLearner irreflexiveLearner;
	
	private int maxLearningTimeInSeconds = 30;
	
	
	LoadingCache<Individual, Set<NamedClass>> typesCache = CacheBuilder.newBuilder()
		       .maximumSize(10000)
		       .build(
		           new CacheLoader<Individual, Set<NamedClass>>() {
		             public Set<NamedClass> load(Individual individual)  {
		            	 Set<NamedClass> types = reasoner.getTypes(individual);
		            	 filterByNamespace(types);
		               return types;
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
	
	LoadingCache<Set<NamedClass>, EvaluatedAxiom> disjointnessCache = CacheBuilder.newBuilder()
		       .maximumSize(1000)
		       .build(
		           new CacheLoader<Set<NamedClass>, EvaluatedAxiom>() {
		             public EvaluatedAxiom load(Set<NamedClass> classes)  {
		               return disjointnessLearner.computeDisjointness(classes).iterator().next();
		             }
		           });
	
	
	
	public ConsistencyCheckerImpl(SparqlEndpoint endpoint, String namespace) {
		this.endpoint = endpoint;
		this.namespace = namespace;
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.init();
			
			
			qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
			try {
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
				CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
				ks.setCache(cacheFrontend);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
				reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cacheFrontend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			disjointnessLearner = new DisjointClassesLearner(ks);
			disjointnessLearner.setReasoner(reasoner);
			disjointnessLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			disjointnessLearner.init();
			
			domainLearner = new ObjectPropertyDomainAxiomLearner(ks);
			domainLearner.setReasoner(reasoner);
			domainLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			domainLearner.init();
			
			rangeLearner = new ObjectPropertyRangeAxiomLearner(ks);
			rangeLearner.setReasoner(reasoner);
			rangeLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			rangeLearner.init();
			
			functionalLearner = new FunctionalObjectPropertyAxiomLearner(ks);
			functionalLearner.setReasoner(reasoner);
			functionalLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			functionalLearner.init();
			
			inverseFunctionalLearner = new InverseFunctionalObjectPropertyAxiomLearner(ks);
			inverseFunctionalLearner.setReasoner(reasoner);
			inverseFunctionalLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			inverseFunctionalLearner.init();
			
			asymmetricLearner = new AsymmetricObjectPropertyAxiomLearner(ks);
			asymmetricLearner.setReasoner(reasoner);
			asymmetricLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			asymmetricLearner.init();
			
			irreflexiveLearner = new IrreflexiveObjectPropertyAxiomLearner(ks);
			irreflexiveLearner.setReasoner(reasoner);
			irreflexiveLearner.setMaxExecutionTimeInSeconds(maxLearningTimeInSeconds);
			irreflexiveLearner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public ConsistencyCheckerImpl(SparqlEndpoint endpoint) {
		this(endpoint, null);
	}
	
	/**
	 * @param accuracyThreshold the minimum accuracy used to accept an automatically generated schema axiom
	 */
	public void setAccuracyThreshold(double accuracyThreshold) {
		this.accuracyThreshold = accuracyThreshold;
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
	
	/**
	 * Returns domain and range axioms, as well as axioms according to characteristics of the given properties.
	 * @param properties
	 * @return
	 */
	private Set<Axiom> getPropertyAxioms(Set<ObjectProperty> properties){
		Set<Axiom> axioms = new HashSet<>();
		
		for (ObjectProperty property : properties) {
			try {
				//domain
				Set<NamedClass> domains = domainCache.get(property);
				for (NamedClass domain : domains) {
					axioms.add(new ObjectPropertyDomainAxiom(property, domain));
				}
				//range
				Set<NamedClass> ranges = rangeCache.get(property);
				for (NamedClass range : ranges) {
					axioms.add(new ObjectPropertyRangeAxiom(property, range));
				}
				//functionality
				boolean functional = isFunctional(property);
				if(functional){
					axioms.add(new FunctionalObjectPropertyAxiom(property));
				}
				//inverse-functionality
				boolean inverseFunctional = isInverseFunctional(property);
				if(inverseFunctional){
					axioms.add(new InverseFunctionalObjectPropertyAxiom(property));
				}
				//asymmetry
				boolean asymmetric = isAsymmetric(property);
				if(asymmetric){
					axioms.add(new AsymmetricObjectPropertyAxiom(property));
				}
				//irreflexivity
				boolean irreflexive = isIrreflexive(property);
				if(irreflexive){
					axioms.add(new IrreflexiveObjectPropertyAxiom(property));
				}
				
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		}
		
		return axioms;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.consistency.ConsistencyChecker#getConsistentTriples(java.util.Set)
	 */
	@Override
	public Set<Triple> getConsistentTriples(Set<Triple> triples) {
		
		for (Iterator<Triple> iter = triples.iterator(); iter.hasNext();) {
			Triple triple = iter.next();
			logger.debug("Checking triple " + triple);
			Individual subject = new Individual(triple.getSubject().getURI());
			Individual object = new Individual(triple.getObject().getURI());
			ObjectProperty property = new ObjectProperty(triple.getPredicate().getURI());
			try {
				
				if(violatesDisjointnessRestrictions(subject, object, property)){
					logger.warn("Omitting triple " + triple + " because it violates the disjointness restrictions.");
					iter.remove();
				} else if(isFunctional(property) && violatesFunctionality(subject, object, property)){
					logger.warn("Omitting triple " + triple + " because it violates the functionality restriction.");
					iter.remove();
				} else if(isInverseFunctional(property) && violatesInverseFunctionality(subject, object, property)){
					logger.warn("Omitting triple " + triple + " because it violates the inverse-functionality restriction.");
					iter.remove();
				} else if(isAsymmetric(property) && violatesAsymmetry(subject, object, property)){
					logger.warn("Omitting triple " + triple + " because it violates the asymmetry restriction.");
					iter.remove();
				} else if(isIrreflexive(property) && violatesIrreflexivity(subject, object, property)){
					logger.warn("Omitting triple " + triple + " because it violates the irreflexivity restriction.");
					iter.remove();
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			}catch (UncheckedExecutionException e) {
				e.printStackTrace();
			}
		}
		return triples;
	}
	
	/**
	 * Checks whether disjointess is violated, i.e. if domain(resp. range) of the given property is disjoint with the type 
	 * of the subject(resp. object).
	 * @param subject
	 * @param object
	 * @param property
	 * @return
	 * @throws ExecutionException
	 */
	private boolean violatesDisjointnessRestrictions(Individual subject, Individual object, ObjectProperty property) throws ExecutionException{
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
//				EvaluatedAxiom axiom = disjointnessLearner.computeDisjointess(type, domain);
				EvaluatedAxiom axiom = disjointnessCache.get(Sets.newHashSet(type, domain));
				if(axiom.getScore().getAccuracy() >= accuracyThreshold){
					logger.debug("Detected disjointness violation of subject type " + type + " and domain " + domain + 
							" with a confidence of " + axiom.getScore().getAccuracy());
					return true;
				}
			}
		}
		
		//try to generate disjointness axioms between object types and property range
		for (NamedClass type : objectTypes) {
			for (NamedClass range : ranges) {
//				EvaluatedAxiom axiom = disjointnessLearner.computeDisjointess(type, range);
				EvaluatedAxiom axiom = disjointnessCache.get(Sets.newHashSet(type, range));
				if(axiom.getScore().getAccuracy() >= accuracyThreshold){
					logger.debug("Detected disjointness violation of object type " + type + " and range " + range + 
							" with a confidence of " + axiom.getScore().getAccuracy());
					return true;
				}
			}
		}
		//knowledge base remains consistent according to the rules we checked in this method
		return false;
	}
	
	/**
	 * Checks whether the given property is functional.
	 * @param property The property which has to be checked for functionality
	 * @return TRUE if the property is functional, otherwise FALSE
	 */
	private boolean isFunctional(ObjectProperty property){
		boolean functional = false;
		//check if property is already declared to be functional in the KB
		functional = reasoner.isFunctional(property);
		//TODO check if it makes sense to analyze the data
		return functional;
	}
	
	/**
	 * Checks whether the given property is inverse functional.
	 * @param property The property which has to be checked for inverse functionality
	 * @return TRUE if the property is inverse functional, otherwise FALSE
	 */
	private boolean isInverseFunctional(ObjectProperty property){
		boolean inverseFunctional = false;
		//check if property is already declared to be inverse-functional in the KB
		inverseFunctional = reasoner.isInverseFunctional(property);
		//TODO check if it makes sense to analyze the data
		return inverseFunctional;
	}
	
	/**
	 * Checks whether the given property is asymmetric.
	 * @param property The property which has to be checked for asymmetry
	 * @return TRUE if the property is asymmetric, otherwise FALSE
	 */
	private boolean isAsymmetric(ObjectProperty property){
		boolean asymmetric = false;
		//check if property is already declared to be asymmetric in the KB
		asymmetric = reasoner.isAsymmetric(property);
		//TODO check if it makes sense to analyze the data
		return asymmetric;
	}
	
	/**
	 * Checks whether the given property is irreflexive.
	 * @param property The property which has to be checked for irreflexivity
	 * @return TRUE if the property is irreflexive, otherwise FALSE
	 */
	private boolean isIrreflexive(ObjectProperty property){
		boolean irreflexive = false;
		//check if property is already declared to be irreflexive in the KB
		irreflexive = reasoner.isIrreflexive(property);
		//TODO check if it makes sense to analyze the data
		return irreflexive;
	}
	
	private boolean violatesFunctionality(Individual subject, Individual object, ObjectProperty property){
		logger.debug("Checking if " + property + " is functional...");
		functionalLearner.setPropertyToDescribe(property);
		functionalLearner.start();
		List<EvaluatedAxiom> axioms = functionalLearner.getCurrentlyBestEvaluatedAxioms(0d);
		
		boolean violates = false;
		if(axioms.isEmpty()){
			logger.debug("..." + property + " is not supposed to be functional.");
		} else {
			double accuracy = axioms.iterator().next().getScore().getAccuracy();
			if(accuracy < accuracyThreshold){
				logger.debug("..." + property + " is not supposed to be functional with a score of " + accuracy);
			} else {
				logger.debug("..." + property + " is supposed to be functional with a score of " + accuracy);
				logger.debug("Validating triple...");
				
				if(!axioms.isEmpty()){
					String query = "ASK {<" + subject + "> <" + property + "> ?o. FILTER(?o != <" + object + ">)}";
					QueryExecution qe = qef.createQueryExecution(query);
					violates = qe.execAsk();
					qe.close();
				}
			}
		}
		return violates;
	}
	
	private boolean violatesInverseFunctionality(Individual subject, Individual object, ObjectProperty property){
		String query = "ASK {?s <" + property + "> <" + object + ">. FILTER(?s != <" + subject + ">)}";
		QueryExecution qe = qef.createQueryExecution(query);
		boolean violates = qe.execAsk();
		qe.close();
		return violates;
	}
	
	private boolean violatesAsymmetry(Individual subject, Individual object, ObjectProperty property){
		String query = "ASK {<" + object + "> <" + property + "> <" + subject + ">}";
		QueryExecution qe = qef.createQueryExecution(query);
		boolean violates = qe.execAsk();
		qe.close();
		return violates;
	}
	
	private boolean violatesIrreflexivity(Individual subject, Individual object, ObjectProperty property){
		return subject.equals(object);
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
