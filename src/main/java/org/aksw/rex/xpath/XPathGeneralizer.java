/**
 * 
 */
package org.aksw.rex.xpath;

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
		List<String> nodes1 = extractNodes(xPath1);log.debug(xPath1);log.debug(nodes1.toString());
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
			return xPath;
		}
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
		final Matcher matcher = NODE_SPLIT_REGEX.matcher(node);
	    if(matcher.find()) {
	    	String element = matcher.group(1);
	    	Integer position = Integer.parseInt(matcher.group(2));
	    	return new Pair<String, Integer>(element, position);
	    }
	    return null;
	}
	
	/**
	 * Extract the nodes on the path, i.e. basically we try to get here all between / and / .
	 * @return
	 */
	private static List<String> extractNodes(String xPathExpression){
//		List<String> nodes = new ArrayList<String>();
//		final Matcher matcher = XPATH_SPLIT_REGEX.matcher(xPathExpression);
//	    while (matcher.find()) {
//	    	nodes.add(matcher.group(1));
//	    }
//	    return nodes;
		return Lists.newArrayList(Splitter.on('/').omitEmptyStrings().trimResults().split(xPathExpression));
	}
	
}
