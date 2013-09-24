/**
 * 
 */
package org.aksw.rex.test.xpath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.rex.util.Pair;
import org.aksw.rex.util.SurfaceFormGenerator;
import org.aksw.rex.xpath.XPathExtractor;
import org.aksw.rex.xpath.XPathGeneralizer;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rules.xpath.XPathRule;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Lorenz Buehmann
 *
 */
public class XPathGeneralizerTest {
	
	private static XPathExtractor ex;
	private org.slf4j.Logger log = LoggerFactory.getLogger(XPathGeneralizerTest.class);
	@BeforeClass
	public static void init() {
		ex = new XPathExtractor();
		 
	}

	@AfterClass
	public static void finish() {
		ex.getIndex().close();
	}
	/**
	 * Test method for {@link org.aksw.rex.xpath.XPathGeneralizer#generalizeXPathExpressions(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGeneralizeXPathExpressions() throws Exception{
		//URLs containing Tom Cruise somewhere and the corresponding XPath expressions
		String url1 = "http://www.imdb.com/title/tt0186151/";
		String url2 = "http://www.imdb.com/title/tt0051201/";
		String xPath1 = "/html[1]/body[1]/div[1]/div[1]/div[2]/div[1]/div[4]/div[3]/div[1]/div[2]/div[7]/div[3]/div[1]/div[2]/div[3]/span[1]";
		String xPath2 = "/html[1]/body[1]/div[1]/div[1]/div[2]/div[1]/div[4]/div[3]/div[1]/div[2]/div[5]/div[3]/div[1]/div[2]/div[3]/span[1]";
		
		//generalize the XPath expressions
		String generalizedXPath = XPathGeneralizer.generalizeXPathExpressions(xPath1, xPath2);
		log.debug(generalizedXPath);
		
		//check the result when applying the generalized XPath on the example URLs
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		log.debug("Extraction on URL " + url1);
		Document doc = Jsoup.connect(url1).get();
		NodeList nodeList = (NodeList) xpath.evaluate(generalizedXPath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);
			log.debug(item.getTextContent());
		}
		log.debug("Extraction on URL " + url2);
		doc = Jsoup.connect(url2).get();
		nodeList = (NodeList) xpath.evaluate(generalizedXPath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);
			log.debug(item.getTextContent());
		}
		
		
	}
	
	/**
	 * Test method for {@link org.aksw.rex.xpath.XPathGeneralizer#generalizeXPathExpressions(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGeneralizeXPathExpressions2() throws Exception{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExtractor ex = new XPathExtractor();
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		String domain = "http://www.imdb.com/title/";
		String property = "dbo:starring";
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/> "
						+ "SELECT ?s ?o WHERE {"
						+ "?s " + property + " ?o. }"
						+ "ORDER BY DESC ( <LONG::IRI_RANK> (?o) + <LONG::IRI_RANK> (?s)) "
						+ "LIMIT 1000 OFFSET 20";
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		ResultSet rs = qef.createQueryExecution(query).execSelect();
		QuerySolution qs;
		
		//Extract pairs of XPath expressions
		List<Pair<XPathRule, XPathRule>> extractionRules = Lists.newArrayList();
		while(rs.hasNext()){
			qs = rs.next();
			Resource subject = qs.getResource("s");
			Resource object = qs.getResource("o");
			//get the surface forms
			Set<String> subjectSurfaceForms = SurfaceFormGenerator.getSurfaceForms(endpoint, subject.getURI());
			Set<String> objectSurfaceForms = SurfaceFormGenerator.getSurfaceForms(endpoint, object.getURI());
			
			for (String sub : subjectSurfaceForms) {
				if(sub.contains("(")){
					sub = sub.substring(0, sub.indexOf('(')).trim();
				}
				for (String obj : objectSurfaceForms) {
					if(obj.contains("(")){
						obj = obj.substring(0, obj.indexOf('(')).trim();
					}
					log.debug("Extracting XPath expressions for '" + sub + "' and '" + obj + "'");
					List<Pair<XPathRule, XPathRule>> extractionRulesTmp = ex.extractPathsFromCrawlIndex(sub, obj, domain);
					log.debug("Got " + extractionRulesTmp.size() + " XPath expression pairs.");
					extractionRules.addAll(extractionRulesTmp);
				}
			}
		}
		
		List<Pair<XPathRule, XPathRule>> genralizedExtractionRules = XPathGeneralizer.generateXPathExtractionRules(extractionRules);
		
		log.debug("Best rule:\n" + genralizedExtractionRules.get(0));
		
		//take some random IMDB pages and check the output
		
		List<Pair<String, String>> documents = ex.getIndex().getDocumentsWithDomain(domain);
		
		for (Pair<String, String> document : documents) {
			String url = document.getLeft();
			String html = document.getRight();
			try {
				Document doc = Jsoup.parse(html);
//				log.debug("Trying URL " + url);

				for (Pair<XPathRule, XPathRule> rule : genralizedExtractionRules) {
					String subjectXPath = rule.getLeft().toString();
					String objectXPath = rule.getRight().toString();
//					log.debug("Applying \nFor subject:" + subjectXPath + "\nFor object:" + objectXPath);

					Set<String> subjects = new HashSet<String>();
					Set<String> objects = new HashSet<String>();

					NodeList nodeList = (NodeList) xpath.evaluate(subjectXPath,
							org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						subjects.add(item.getTextContent());
					}

					nodeList = (NodeList) xpath.evaluate(objectXPath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc),
							XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						objects.add(item.getTextContent());
					}
					if(!subjects.isEmpty() && !objects.isEmpty()){
						log.debug("Trying URL " + url);
//						log.debug("Found triple(s) using\nFor subject:" + subjectXPath + "\nFor object:" + objectXPath);
					}
					for (String sub : subjects) {
						for (String obj : objects) {
							log.debug(sub + " " + property + " " + obj);
						}
					}
				}

			} catch (Exception e) {
				log.debug("Could not process URL: " + url);
			}
		}
	}
	

}
