/**
 * 
 */
package org.aksw.rex.examplegenerator;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.rex.util.Pair;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Lorenz Buehmann
 *
 */
public class SimpleExampleGenerator implements ExampleGenerator{
	
	
	private static final Logger logger = Logger.getLogger(SimpleExampleGenerator.class.getName());

	private SparqlEndpoint endpoint;
	private SPARQLReasoner reasoner;
	private Property property;
	private int maxNrOfPositiveExamples = 10;
	private int maxNrOfNegativeExamples = 10;
	
	private QueryExecutionFactory qef;
	private String cacheDirectory = "cache/sparql";
	
	Random rnd = new Random(123);

	/* (non-Javadoc)
	 * @see org.aksw.rex.examplegenerator.ExampleGenerator#setEndpoint(org.dllearner.kb.sparql.SparqlEndpoint)
	 */
	@Override
	public void setEndpoint(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
		
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
			try {
				long timeToLive = TimeUnit.DAYS.toMillis(30);
				CacheCoreEx cacheBackend = CacheCoreH2.create(cacheDirectory, timeToLive, true);
				CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
				qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
//		qef = new QueryExecutionFactoryPaginated(qef, 10000);
		
		reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint), cacheDirectory);
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.examplegenerator.ExampleGenerator#setPredicate(com.hp.hpl.jena.rdf.model.Property)
	 */
	@Override
	public void setPredicate(Property property) {
		this.property = property;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.examplegenerator.ExampleGenerator#getPositiveExamples()
	 */
	@Override
	public Set<Pair<Resource, Resource>> getPositiveExamples() {
		return getMostProminentPositiveExamples();
//		return getRandomPositiveExamples();
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.examplegenerator.ExampleGenerator#getNegativeExamples()
	 */
	@Override
	public Set<Pair<Resource, Resource>> getNegativeExamples() {
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		examples.addAll(getNegativeExamplesRandomSubjectInDomain(10));
//		examples.addAll(getNegativeExamplesRandomObjectInRange(10));
//		examples.addAll(getNegativeExamplesRandomSubjectInDomainRandomObjectInRange(10));
		return examples;
	}
	
	private Set<Pair<Resource, Resource>> getMostProminentPositiveExamples(){
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		String query = "SELECT ?s ?o WHERE {?s <" + property.getURI() + "> ?o. ?s_in ?p1 ?s. ?o_in ?p2 ?o.} "
				+ "GROUP BY ?s ?o ORDER BY DESC(COUNT(?s_in)+COUNT(?o_in)) LIMIT " + maxNrOfPositiveExamples;
		ResultSet rs = executeSelectQuery(query);
		QuerySolution qs;
		Resource subject;
		Resource object;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.get("s").isURIResource()){
				subject = qs.getResource("s");
			} else {
				logger.warn("Omitting triple:Subject " + qs.get("s") + " is not a URI resource!");
				continue;
			}
			if(qs.get("o").isURIResource()){
				object = qs.getResource("o");
			} else {
				logger.warn("Omitting triple:Object " + qs.get("o") + " is not a URI resource!");
				continue;
			}
			examples.add(new Pair<Resource, Resource>(subject, object));
		}
		return examples;
	}
	
	private Set<Pair<Resource, Resource>> getRandomPositiveExamples(){
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		
		ParameterizedSparqlString queryString = new ParameterizedSparqlString("SELECT ?s ?o WHERE {?s ?p ?o.}");
		queryString.setIri("p", property.getURI());
		
		QuerySolution qs;
		Resource subject;
		Resource object;
		int propCnt = reasoner.getPopularity(new ObjectProperty(property.getURI()));
		while(examples.size() < Math.min(maxNrOfPositiveExamples, propCnt)){
			int offset = rnd.nextInt(propCnt);
			
			Query query = queryString.asQuery();
			query.setLimit(1);
			query.setOffset(offset);
		
			ResultSet rs = executeSelectQuery(query);
			if(rs.hasNext()){
				qs = rs.next();
				if(qs.get("s").isURIResource()){
					subject = qs.getResource("s");
				} else {
					logger.warn("Omitting triple:Subject " + qs.get("s") + " is not a URI resource!");
					continue;
				}
				if(qs.get("o").isURIResource()){
					object = qs.getResource("o");
				} else {
					logger.warn("Omitting triple:Object " + qs.get("o") + " is not a URI resource!");
					continue;
				}
				examples.add(new Pair<Resource, Resource>(subject, object));
			}
		}
		
		return examples;
	}
	
	private Set<Pair<Resource, Resource>> getNegativeExamplesRandomSubjectInDomain(int limit){
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		//get domain of the property (we assume at most one domain exists in K)
		ParameterizedSparqlString query = new ParameterizedSparqlString(
				"SELECT ?domain WHERE {?p <http://www.w3.org/2000/01/rdf-schema#domain> ?domain.}");
		query.setParam("p", property);
		ResultSet rs = executeSelectQuery(query.asQuery());
		Resource domain = null;
		if(rs.hasNext()){
			domain = rs.next().getResource("domain");
		}
		
		if(domain != null){
			//count triples with p
			int propCnt = reasoner.getPopularity(new ObjectProperty(property.getURI()));
			//count instances of domain
			int domainCnt = reasoner.getPopularity(new NamedClass(domain.getURI()));
			int innerOffset;
			int outerOffset;
			for (int i = 0; i < limit; i++) {
				innerOffset = rnd.nextInt(domainCnt);
				outerOffset = rnd.nextInt(propCnt);
				//let (s p o) be in K, we get (s' p o) where s' is in the domain of p and (s' p o) is not in K
				query  = new ParameterizedSparqlString(
						"SELECT ?s_false ?o WHERE "
						+ "{?s_false a ?domain.  FILTER NOT EXISTS {?s_false ?p ?o.}"
						+ "{SELECT ?o WHERE { ?s ?p ?o.} LIMIT 1 OFFSET " + innerOffset + "}"
						+ "} LIMIT 1 OFFSET " + outerOffset);
				query.setParam("p", property);
				query.setParam("domain", domain);
				rs = executeSelectQuery(query.asQuery());
				if(rs.hasNext()){
					QuerySolution qs = rs.next();
					Resource subject = qs.getResource("s_false");
					Resource object = qs.getResource("o");
					examples.add(new Pair<Resource, Resource>(subject, object));
				}
			}
		}
		return examples;
	}
	
	private Set<Pair<Resource, Resource>> getNegativeExamplesRandomObjectInRange(int limit){
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		//get range of the property (we assume at most one range exists in K)
		ParameterizedSparqlString query = new ParameterizedSparqlString(
				"SELECT ?range WHERE {?p <http://www.w3.org/2000/01/rdf-schema#range> ?range.}");
		query.setParam("p", property);
		ResultSet rs = executeSelectQuery(query.asQuery());
		Resource range = null;
		if(rs.hasNext()){
			range = rs.next().getResource("range");
		}
		
		if(range != null){
			int innerOffset;
			int outerOffset;
			//count triples with p
			int propCnt = reasoner.getPopularity(new ObjectProperty(property.getURI()));
			//count instances of range
			int rangeCnt = reasoner.getPopularity(new NamedClass(range.getURI()));
			for (int i = 0; i < limit; i++) {
				innerOffset = rnd.nextInt(rangeCnt);
				outerOffset = rnd.nextInt(propCnt);
				//let (s p o) be in K, we get (s p o') where o' is in the range of p and (s p o') is not in K
				query  = new ParameterizedSparqlString(
						"SELECT ?s ?o_false WHERE "
						+ "{?o_false a ?range.  FILTER NOT EXISTS {?s ?p ?o_false.}"
						+ "{SELECT ?s WHERE { ?s ?p ?o.} LIMIT 1 OFFSET " + innerOffset + "}"
						+ "} LIMIT 1 OFFSET " + outerOffset);
				query.setParam("p", property);
				query.setParam("range", range);
				rs = executeSelectQuery(query.asQuery());
				if(rs.hasNext()){
					QuerySolution qs = rs.next();
					Resource subject = qs.getResource("s");
					Resource object = qs.getResource("o_false");
					examples.add(new Pair<Resource, Resource>(subject, object));
				}
			}
		}
		return examples;
	}
	
	private Set<Pair<Resource, Resource>> getNegativeExamplesRandomSubjectInDomainRandomObjectInRange(int limit){
		Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource,Resource>>();
		//get domain of the property (we assume at most one domain exists in K)
		ParameterizedSparqlString query = new ParameterizedSparqlString(
				"SELECT ?domain WHERE {?p <http://www.w3.org/2000/01/rdf-schema#domain> ?domain.}");
		query.setParam("p", property);
		ResultSet rs = executeSelectQuery(query.asQuery());
		Resource domain = null;
		if(rs.hasNext()){
			domain = rs.next().getResource("domain");
		}
		//get range of the property (we assume at most one range exists in K)
		query = new ParameterizedSparqlString(
				"SELECT ?range WHERE {?p <http://www.w3.org/2000/01/rdf-schema#range> ?range.}");
		query.setParam("p", property);
		rs = executeSelectQuery(query.asQuery());
		Resource range = null;
		if(rs.hasNext()){
			range = rs.next().getResource("range");
		}
		if(domain != null && range != null){
			//count triples with p
			int domainCnt = reasoner.getPopularity(new NamedClass(domain.getURI()));
			int rangeCnt = reasoner.getPopularity(new NamedClass(range.getURI()));
			
			int domainOffset;
			int rangeOffset;
			if(range != null){
				for (int i = 0; i < limit; i++) {
					domainOffset = rnd.nextInt(domainCnt);
					rangeOffset = rnd.nextInt(rangeCnt);
					//let (s p o) be in K, we get (s p o') where o' is in the range of p and (s p o') is not in K
					query  = new ParameterizedSparqlString(
							"SELECT ?s_false ?o_false WHERE {"
							+ "{SELECT * WHERE {?s_false a ?domain} LIMIT 1 OFFSET " + domainOffset + "}"
							+ "{SELECT * WHERE {?o_false a ?range } LIMIT 1 OFFSET " + rangeOffset + "}"
							+ "FILTER NOT EXISTS {?s_false ?p ?o_false.}"
							+ "} LIMIT 1");
					query.setParam("p", property);
					query.setParam("domain", domain);
					query.setParam("range", range);
					rs = executeSelectQuery(query.asQuery());
					if(rs.hasNext()){
						QuerySolution qs = rs.next();
						Resource subject = qs.getResource("s_false");
						Resource object = qs.getResource("o_false");
						examples.add(new Pair<Resource, Resource>(subject, object));
					}
				}
			}
		}
		
		return examples;
	}
	
	private ResultSet executeSelectQuery(Query query){System.out.println(query);
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		return rs;
	}
	
	private ResultSet executeSelectQuery(String query){
		return executeSelectQuery(QueryFactory.create(query, Syntax.syntaxARQ));
	}
	
	public static void main(String[] args) throws Exception {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		Model model = ModelFactory.createDefaultModel();
		Property property = model.createProperty("http://dbpedia.org/ontology/director");
		ExampleGenerator gen = new SimpleExampleGenerator();
		gen.setEndpoint(endpoint);
		gen.setPredicate(property);
		Set<Pair<Resource, Resource>> positiveExamples = gen.getPositiveExamples();
		Set<Pair<Resource, Resource>> negativeExamples = gen.getNegativeExamples();
		System.out.println(positiveExamples);
		System.out.println(negativeExamples);
	}

}
