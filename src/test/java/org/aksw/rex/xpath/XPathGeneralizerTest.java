/**
 * 
 */
package org.aksw.rex.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.rex.util.Pair;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class XPathGeneralizerTest {
	
	private org.slf4j.Logger log = LoggerFactory.getLogger(XPathGeneralizerTest.class);

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
		XPathExtractor ex = new XPathExtractor();
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
		String domain = "http://www.imdb.com/title/";
		String property = "http://dbpedia.org/ontology/starring";
		String query = "SELECT ?s ?o WHERE {"
						+ "?s <" + property + "> ?o. }"
						+ "ORDER BY DESC ( <LONG::IRI_RANK> (?o) + <LONG::IRI_RANK> (?s)) "
						+ "LIMIT 20";
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		ResultSet rs = qef.createQueryExecution(query).execSelect();
		QuerySolution qs;
		
		//Extract pairs of XPath expressions
		List<Pair<String, String>> xPathPairs = Lists.newArrayList();
		while(rs.hasNext()){
			qs = rs.next();
			String subject = sfp.getShortForm(IRI.create(qs.getResource("s").getURI())).replace("_", " ");
			String object = sfp.getShortForm(IRI.create(qs.getResource("o").getURI())).replace("_", " ");
			
			log.debug("Extracting XPath expressions for '" + subject + "' and '" + object + "'");
			List<Pair<String, String>> xPathPairsTmp = ex.extractPathsFromCrawlIndex(subject, object, domain);
			log.debug("Got " + xPathPairsTmp.size() + " XPath expression pairs.");
			xPathPairs.addAll(xPathPairsTmp);
		}
		
		List<Pair<String, String>> generalizedXPathPairs = XPathGeneralizer.generalizeXPathExpressions(xPathPairs);
		
		
	}
	

}
