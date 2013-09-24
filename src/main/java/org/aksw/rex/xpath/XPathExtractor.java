package org.aksw.rex.xpath;

import static org.joox.JOOX.$;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.util.Pair;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import rules.xpath.XPathRule;

public class XPathExtractor {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(XPathExtractor.class);

	private CrawlIndex index;

	public XPathExtractor() {
		index = new CrawlIndex("htmlindex/");
	}
	
	public XPathExtractor(CrawlIndex crawlIndex) {
		index = crawlIndex;
	}
	
	public List<Pair<XPathRule, XPathRule>> extractPathsFromCrawlIndex(String subject, String object, boolean exactMatch) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		return extractPathsFromCrawlIndex(subject, object, null);
	}

	public List<Pair<XPathRule, XPathRule>> extractPathsFromCrawlIndex(String subject, String object) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		return extractPathsFromCrawlIndex(subject, object, null);
	}
	
	public List<Pair<XPathRule, XPathRule>> extractPathsFromCrawlIndex(String subject, String object, String domainURL) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		return extractPathsFromCrawlIndex(subject, object, domainURL, true);
	}
	
	public List<Pair<XPathRule, XPathRule>> extractPathsFromCrawlIndex(String subject, String object, String domainURL, boolean exactMatch) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		List<Pair<XPathRule, XPathRule>> paths = new ArrayList<Pair<XPathRule, XPathRule>>();
		// search for all pages containing subject and object
		ArrayList<Pair<String, String>> docs = index.searchHTML(subject + " AND " + object);
		int d = 0;
		for (Pair<String, String> document : docs) {
//			log.debug("Progress: " + ((double) d++ / (double) docs.size()));
			// search for all xpath containing this string in the content
			String url = document.getLeft();
			String html = document.getRight();
			if(domainURL != null && url.startsWith(domainURL)){
				try {
					
					List<String> subjectXPaths = extractXPaths(subject, html, exactMatch);
					List<String> objectXPaths = extractXPaths(object, html, exactMatch);
					if(!subjectXPaths.isEmpty() && !objectXPaths.isEmpty()){
						log.trace("Found XPath expressions on " + url);
					}
					for (String subjectXPath : subjectXPaths) {
						for (String objectXPath : objectXPaths) {
							log.trace("For subject: " + subjectXPath);
							log.trace("For object: " + objectXPath);
							paths.add(new Pair<XPathRule, XPathRule>(new XPathRule(subjectXPath), new XPathRule(objectXPath)));
						}
					}
				} catch (Exception e) {
					log.error("Could not process URL: " + url);
				}
			}
		}
		return paths;
	}
	
	public Map<String, List<Pair<XPathRule, XPathRule>>> extractPathsFromCrawlIndexWithURL(String subject, String object) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		Map<String, List<Pair<XPathRule, XPathRule>>> url2XpathPairs = new HashMap<String, List<Pair<XPathRule, XPathRule>>>();
		
		// search for all pages containing subject and object
		List<Pair<String, String>> docs = index.searchHTML(subject + " AND " + object);
		int d = 0;
		log.debug("Start working on HTML to extract XPATHs");
		for (Pair<String, String> document : docs) {
			List<Pair<XPathRule, XPathRule>> paths = new ArrayList<Pair<XPathRule, XPathRule>>();
			log.debug("Progress: " + ((double) d++ / (double) docs.size()));
			// search for all xpath containing this string in the content
			String url = document.getLeft();
			String html = document.getRight();
			try {
				log.debug("URL: " + url);
				List<String> subjectXPaths = extractXPaths(subject, html);
				List<String> objectXPaths = extractXPaths(object, html);
				for (String subjectXPath : subjectXPaths) {
					for (String objectXPath : objectXPaths) {
						log.debug("Subject: " + subjectXPath);
						log.debug("Object: " + objectXPath);
						paths.add(new Pair<XPathRule, XPathRule>(new XPathRule(subjectXPath), new XPathRule(objectXPath)));
					}
				}
				url2XpathPairs.put(url, paths);
			} catch (Exception e) {
				log.debug("Could not process URL: " + url);
			}
		}
		log.debug("Finished working on HTML to extract XPATHs");

		return url2XpathPairs;
	}
	
	public List<String> extractXPaths(String query, String html) throws XPathExpressionException {
		return extractXPaths(query, html, true);
	}
	
	public List<String> extractXPaths(String query, String html, boolean exactMatch) throws XPathExpressionException {
		List<String> paths = new ArrayList<String>();

		// XPATH PART
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
		Document W3CDoc = org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(jsoupDoc);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		//*/text()[normalize-space(.)='match']/parent::*
		String expression;
		if(exactMatch){// all nodes that contains text that equals "query"
			expression = "//*[text()='" + query + "']";
		} else {// all nodes that contains text that contains "query"
			expression = "//*[contains(.,'" + query + "')]";
		}
		
		NodeList nodeList = (NodeList) xpath.evaluate(expression, W3CDoc, XPathConstants.NODESET);
		// select appropriate nodes
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);
			if (checkChildsDoContainText(item, query)) {
				String xPath = getXPath(item);
				paths.add(xPath);
			}
		}
		return paths;
	}

	private boolean checkChildsDoContainText(Node item, String query) {
		NodeList childs = item.getChildNodes();
		for (int i = 0; i < childs.getLength(); ++i) {
			Node child = childs.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				if (child.getTextContent().contains(query)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getXPath(Node item) {
		Element element = (Element) item;
		String elementXpath = $(element).xpath();
		return elementXpath;
	}

	public CrawlIndex getIndex() {
		return index;
	}
	
	public static void main(String args[]) throws FileNotFoundException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		XPathExtractor xpathExtractor = new XPathExtractor();
		List<Pair<XPathRule, XPathRule>> paths = xpathExtractor.extractPathsFromCrawlIndex("Tom Cruise", "Mission Impossible");
		for (Pair<XPathRule, XPathRule> path : paths) {
			log.debug(path.toString());
		}
	}
}
