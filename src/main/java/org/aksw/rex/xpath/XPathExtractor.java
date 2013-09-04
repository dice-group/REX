package org.aksw.rex.xpath;

import static org.joox.JOOX.$;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.util.Pair;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathExtractor {
	private CrawlIndex index;

	public XPathExtractor() {
		index = new CrawlIndex("htmlindex/");

	}

	public static void main(String args[]) throws FileNotFoundException, XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		XPathExtractor xpathExtractor = new XPathExtractor();
		ArrayList<String> paths = xpathExtractor.extractPathsFromCrawlIndex("Tom Cruise");
		for (String path : paths) {
			System.out.println(path);
		}
	}

	public ArrayList<String> extractPathsFromCrawlIndex(String query) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException {
		ArrayList<String> paths = new ArrayList<String>();
		// search for all pages containing this string
		ArrayList<Pair<String, String>> docs = index.searchHTML(query);
		int d = 0;
		for (Pair<String, String> document : docs) {
			System.out.println("Progress: " + ((double) d++ / (double) docs.size()));
			// search for all xpath containing this string in the content
			String url = document.getLeft();
			String html = document.getRight();
			try {
				paths.addAll(extractXPaths(query, html));
			} catch (Exception e) {
				System.out.println("Could not process URL: " + url);
			}
		}

		return paths;
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
