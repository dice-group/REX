/**
 * 
 */
package org.aksw.rex.xpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.rex.util.Pair;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class XPathGeneralizer {
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(XPathGeneralizer.class);
	
	private static final Pattern XPATH_SPLIT_REGEX = Pattern.compile("/(.+?)/");
	private static final Pattern NODE_SPLIT_REGEX = Pattern.compile("(.+?)\\[(.+?)\\]");
	private static final Set<String> CONTAINER_ELEMENTS = Sets.newHashSet("body", "div", "tr", "td");
	private static String STAR = "*";

	/**
	 * Warning: This is a preliminary simple implementation for generalizing 2 XPath expressions given as String objects.
	 * @param xPath1
	 * @param xPath2
	 * @return
	 */
	public static String generalizeXPathExpressions(String xPath1, String xPath2){
		if(xPath1.equals(xPath2)){
			return xPath1;
		}
		List<String> nodes1 = extractNodes(xPath1);
		List<String> nodes2 = extractNodes(xPath2);
		
		if(nodes1.size() != nodes2.size()){
			throw new UnsupportedOperationException("Can not handle XPath expressions with different length.");
		} else {
			StringBuilder generalizedXPath = new StringBuilder("/");
			for(int i = 0; i < nodes1.size(); i++){
				String node1 = nodes1.get(i);
				String node2 = nodes2.get(i);
				
				//if both nodes are the same, just add one of the nodes the the generalized XPath expression
				if(node1.equals(node2)){
					addNode(generalizedXPath, node1);
				} else {
					//if one of the nodes is * just return *
					if(node1.equals(STAR) || node2.equals(STAR)){
						addNode(generalizedXPath, STAR);
					} else {
						Pair<String, Integer> pair1 = splitNode(node1);
						Pair<String, Integer> pair2 = splitNode(node2);
						//check if elements are the same
						if(pair1.getLeft().equals(pair2.getLeft())){
							//both HTML elements are the same, thus we know that the position is different and
							//we add just the element as node without any position
							addNode(generalizedXPath, pair1.getLeft());
						} else {
							//both elements are different, thus we add *
							addNode(generalizedXPath, STAR);
						}
					}
				}
			}
			String xPath = generalizedXPath.toString();
			//if last character is '/' remove it as it is not allowed
			if(xPath.endsWith("/")){
				xPath = xPath.substring(0, xPath.length()-1);
			}
//			log.debug("Generalized XPath: " + xPath);
			return xPath;
		}
	}
	
	/**
	 * Warning: This is a preliminary simple implementation for generalizing 2 XPath expressions given as String objects.
	 * @param xPath1
	 * @param xPath2
	 * @return
	 */
	public static String generalizeXPathExpressions(String ... xPaths){
		if(xPaths.length == 0){
			return null;
		} else if(xPaths.length == 1){
			return xPaths[0];
		} else {
			String generalizedXPath = xPaths[0];
			for(int i = 1; i < xPaths.length; i++){
				generalizedXPath = generalizeXPathExpressions(generalizedXPath, xPaths[i]);
			}
			return generalizedXPath;
		}
		
	}
	
	/**
	 * Generates generalized XPath expressions for subject and object
	 * @param xPath1
	 * @param xPath2
	 * @return
	 */
	public static List<Pair<String, String>> generalizeXPathExpressions(List<Pair<String, String>> xPathPairs){
		List<Pair<String, String>> generalizedXPathPairs = Lists.newArrayList();
		
		//cluster by HTML element sequence
		List<List<Pair<String, String>>> clusters = clusterByHTMLElementSequence(xPathPairs);
		
		//for each cluster generalize the XPaths
		for (List<Pair<String, String>> cluster : clusters) {
			log.debug("Processing cluster with size " + cluster.size());
			String generalizedSubjectXPath = cluster.get(0).getLeft();
			String generalizedObjectXPath = cluster.get(0).getRight();

			if (cluster.size() > 1) {
				for (Pair<String, String> xPathPair : cluster) {
					String subjectXPath = xPathPair.getLeft();
					String objectXPath = xPathPair.getRight();

					generalizedSubjectXPath = XPathGeneralizer.generalizeXPathExpressions(generalizedSubjectXPath, subjectXPath);
					generalizedObjectXPath = XPathGeneralizer.generalizeXPathExpressions(generalizedObjectXPath, objectXPath);
				}
			}

			log.debug("Generalized XPath for subjects:" + generalizedSubjectXPath);
			log.debug("Generalized XPath for objects:" + generalizedObjectXPath);
			
			generalizedXPathPairs.add(new Pair<String, String>(generalizedSubjectXPath, generalizedSubjectXPath));
		}
		return generalizedXPathPairs;
	}
	
	private static List<List<Pair<String, String>>> clusterByHTMLElementSequence(List<Pair<String, String>> xPathsPairs){
		List<List<Pair<String, String>>> clusters = new ArrayList<List<Pair<String,String>>>();
		
		Iterator<Pair<String, String>> iter = xPathsPairs.iterator();
		//initialize with the first pair
		clusters.add(Lists.newArrayList(iter.next()));
		
		//for each pair add to corresponding cluster or create new one
		for (Pair<String, String> pair : xPathsPairs) {
			boolean added = false;
			for (List<Pair<String, String>> cluster : clusters) {
				Pair<String, String> representative = cluster.get(0);
				
				List<String> representativeSubjectNodes = extractHTMLElementSequence(representative.getLeft());
				List<String> representativeObjectNodes = extractHTMLElementSequence(representative.getRight());
				
				List<String> subjectNodes = extractHTMLElementSequence(pair.getLeft());
				List<String> objectNodes = extractHTMLElementSequence(pair.getRight());
				if(representativeSubjectNodes.equals(subjectNodes) && representativeObjectNodes.equals(objectNodes)){
					cluster.add(pair);
					added = true;
					break;
				}
			}
			if(!added){
				List<Pair<String, String>> cluster = Lists.newArrayList();
				cluster.add(pair);
				clusters.add(cluster);
			}
		}
		
		return clusters;
	}
	
	private static void addNode(StringBuilder xPath, String node){
		xPath.append(node).append("/");
	}
	
	/**
	 * Split node into HTML element and position.
	 * @param node
	 * @return
	 */
	private static Pair<String, Integer> splitNode(String node){
		if(node.contains("[")){
			final Matcher matcher = NODE_SPLIT_REGEX.matcher(node);
		    if(matcher.find()) {
		    	String element = matcher.group(1);
		    	Integer position = Integer.parseInt(matcher.group(2));
		    	return new Pair<String, Integer>(element, position);
		    }
		} 
		return new Pair<String, Integer>(node, null);
	}
	
	/**
	 * Extract the nodes on the path, i.e. basically we try to get here all between / and / .
	 * @return
	 */
	private static List<String> extractNodes(String xPathExpression){
		return Lists.newArrayList(Splitter.on('/').omitEmptyStrings().trimResults().split(xPathExpression));
	}
	
	/**
	 * Extract the HTML elements on the path, i.e. basically we try to get here all between / and / without [(0-9)*].
	 * @return
	 */
	private static List<String> extractHTMLElementSequence(String xPath){
		List<String> nodes = Lists.newArrayList();
		for (String node : Splitter.on('/').omitEmptyStrings().trimResults().split(xPath)) {
			if(node.contains("[")){
				nodes.add(node.substring(0, node.indexOf('[')));
			} else {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
}
