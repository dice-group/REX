/**
 * 
 */
package org.aksw.rex.xpath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.results.ExtractionResultImpl;
import org.aksw.rex.util.Pair;
import org.aksw.rex.util.SurfaceFormGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rules.xpath.XPathRule;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Simple Implemention of XPath Learning
 * @author Lorenz Buehmann
 *
 */
public class XPathLearnerImpl implements XPathLearner{
	
	private org.slf4j.Logger log = LoggerFactory.getLogger(XPathLearnerImpl.class);
	
	private XPathExtractor xPathExtractor;
	private SurfaceFormGenerator surfaceFormGenerator;
	
	private XPath xpath;

	private SparqlEndpoint endpoint;
	private boolean useExactMatch = true;
	
	public XPathLearnerImpl(XPathExtractor xPathExtractor, SparqlEndpoint endpoint) {
		this.xPathExtractor = xPathExtractor;
		this.endpoint = endpoint;
		
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		
		surfaceFormGenerator = new SurfaceFormGenerator(endpoint, "sparql-cache");
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.xpath.XPathLearner#getXPathExpressions(java.util.Set, java.util.Set, java.net.URL)
	 */
	@Override
	public List<Pair<XPathRule, XPathRule>> getXPathExpressions(Set<Pair<Resource, Resource>> posExamples,
			Set<Pair<Resource, Resource>> negExamples, URL domain) {
		
		//generate XPath extraction rules for each positive example
		List<Pair<XPathRule, XPathRule>> extractionRules = Lists.newArrayList();
		for (Pair<Resource,Resource> pair : posExamples) {
			Resource subject = pair.getLeft();
			Resource object = pair.getRight();
			//get the surface forms
			Set<String> subjectSurfaceForms = surfaceFormGenerator.getSurfaceForms(endpoint, subject.getURI());
			Set<String> objectSurfaceForms = surfaceFormGenerator.getSurfaceForms(endpoint, object.getURI());
			
			for (String sub : subjectSurfaceForms) {
				for (String obj : objectSurfaceForms) {
					try {
						log.debug("Generating XPath extraction rules for '" + sub + "' and '" + obj + "'...");
						List<Pair<XPathRule, XPathRule>> extractionRulesTmp = xPathExtractor.extractPathsFromCrawlIndex(sub, obj, domain.toString(), useExactMatch);
						log.debug("...got " + extractionRulesTmp.size() + " XPath extraction rules.");
						extractionRules.addAll(extractionRulesTmp);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//generate generalized extraction rules
		List<Pair<XPathRule, XPathRule>> generalizedExtractionRules = XPathGeneralizer.generateXPathExtractionRules(extractionRules);
		
		return generalizedExtractionRules;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.xpath.XPathLearner#getExtractionResults(java.util.List)
	 */
	@Override
	public Set<ExtractionResult> getExtractionResults(List<Pair<XPathRule, XPathRule>> extractionRules, URL domain) {
		Set<ExtractionResult> extractionResults = new HashSet<ExtractionResult>();
		
		//get documents of domain
		List<Pair<String, String>> documents = xPathExtractor.getIndex().getDocumentsWithDomain(domain.toString());
		
		//for each document each of the rules will be applied
		for (Pair<String, String> document : documents) {
			String url = document.getLeft();
			String html = document.getRight();
			try {
				Document doc = Jsoup.parse(html);
//				log.debug("Trying URL " + url);

				for (Pair<XPathRule, XPathRule> rule : extractionRules) {
					String subjectXPath = rule.getLeft().toString();
					String objectXPath = rule.getRight().toString();

					Set<String> subjects = new HashSet<String>();
					Set<String> objects = new HashSet<String>();

					//get nodes applying rule for subject
					NodeList nodeList = (NodeList) xpath.evaluate(subjectXPath,
							org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						subjects.add(item.getTextContent());
					}

					//get nodes applying rule for object
					nodeList = (NodeList) xpath.evaluate(objectXPath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc),
							XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node item = nodeList.item(i);
						objects.add(item.getTextContent());
					}
					
					//add extraction results for each subject-object pair
					for (String sub : subjects) {
						for (String obj : objects) {
							extractionResults.add(new ExtractionResultImpl(sub, obj,""));
						}
					}
				}

			} catch (Exception e) {
				log.debug("Could not process URL: " + url);
			}
		}
		return extractionResults;
	}
	
	/* (non-Javadoc)
	 * @see org.aksw.rex.xpath.XPathLearner#setUseExactMatch(boolean)
	 */
	@Override
	public void setUseExactMatch(boolean useExactMatch) {
		this.useExactMatch = useExactMatch;
	}

}
