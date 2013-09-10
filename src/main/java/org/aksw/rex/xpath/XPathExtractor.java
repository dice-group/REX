package org.aksw.rex.xpath;

import static org.joox.JOOX.$;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

public class XPathExtractor {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(XPathExtractor.class);

	private CrawlIndex index;

	public XPathExtractor() {
		index = new CrawlIndex("htmlindex/");

	}

	public static void main(String args[]) throws FileNotFoundException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		XPathExtractor xpathExtractor = new XPathExtractor();
		ArrayList<String> paths = xpathExtractor.extractPathsFromCrawlIndex("Tom Cruise");
		for (String path : paths) {
			log.debug(path);
		}
	}

	public ArrayList<String> extractPathsFromCrawlIndex(String query) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		ArrayList<String> paths = new ArrayList<String>();
		// search for all pages containing this string
		ArrayList<Pair<String, String>> docs = index.searchHTML(query);
		int d = 0;
		log.debug("Start working on HTML to extract XPATHs");
		for (Pair<String, String> document : docs) {
			log.debug("Progress: " + ((double) d++ / (double) docs.size()));
			// search for all xpath containing this string in the content
			String url = document.getLeft();
			String html = document.getRight();
			try {
				paths.addAll(extractXPaths(query, html));
			} catch (Exception e) {
				log.debug("Could not process URL: " + url);
			}
		}
		log.debug("Finished working on HTML to extract XPATHs");

		return paths;
	}
	
	public Map<String, Collection<String>> extractPathsFromCrawlIndexWithURL(String query) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		Map<String, Collection<String>> url2paths = new HashMap<String, Collection<String>>();
		// search for all pages containing this string
		ArrayList<Pair<String, String>> docs = index.searchHTML(query);
		int d = 0;
		log.debug("Start working on HTML to extract XPATHs");
		for (Pair<String, String> document : docs) {
			log.debug("Progress: " + ((double) d++ / (double) docs.size()));
			// search for all xpath containing this string in the content
			String url = document.getLeft();
			String html = document.getRight();
			try {
				url2paths.put(url, extractXPaths(query, html));
			} catch (Exception e) {
				log.debug("Could not process URL: " + url);
			}
		}
		log.debug("Finished working on HTML to extract XPATHs");

		return url2paths;
	}

	public ArrayList<String> extractXPaths(String query, String html) throws XPathExpressionException {
		ArrayList<String> paths = new ArrayList<String>();

		// XPATH PART
		org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
		Document W3CDoc = org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(jsoupDoc);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		// all nodes that contains text that contains "query"
		String expression = "//*[contains(.,'" + query + "')]";
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
}
