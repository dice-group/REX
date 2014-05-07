package org.aksw.rex.uris;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;

import edu.stanford.nlp.util.Quadruple;

public class URIGeneratorAGDISTIS implements URIGenerator {
	private org.slf4j.Logger log = LoggerFactory.getLogger(URIGeneratorAGDISTIS.class);
	private AGDISTISPost agdistis;

	public URIGeneratorAGDISTIS() {
		this.agdistis = new AGDISTISPost();
	}

	@Override
	public Set<Quadruple<Node, Node, Node, String>> getTriples(Set<ExtractionResult> pairs, Property p) throws Exception {
		Set<Quadruple<Node, Node, Node, String>> set = new HashSet<Quadruple<Node, Node, Node, String>>();
		// for each pos and neg example
		if (pairs == null) {
			return set;
		}
		for (ExtractionResult res : pairs) {
			// process left
			log.info("Disambiguating:" + res.getSubject() +", "+ res.getObject());
			Quadruple<Node, Node, Node, String> process = process(res, p);
			if (process != null)
				set.add(process);
		}
		return set;
	}

	private Quadruple<Node, Node, Node, String> process(ExtractionResult res, Property p) throws Exception {
		String subjectString = res.getSubject();
		String objectString = res.getObject();

		if(subjectString==null||subjectString.equals(""))
			return null;
		if(objectString==null||objectString.equals(""))
			return null;
		Node s = null;
		Node o = null;
		String preAnnotatedText = "<entity>" + subjectString + "</entity><entity>" + objectString + "</entity>";

		HashMap<String, String> results = agdistis.runDisambiguation(preAnnotatedText);
		for (String namedEntity : results.keySet()) {
			String disambiguatedURL = results.get(namedEntity);
			if (namedEntity.equals(subjectString)&&disambiguatedURL!=null) {
				s = Node.createURI(disambiguatedURL);
			}
			if (namedEntity.equals(objectString)&&disambiguatedURL!=null) {
				o = Node.createURI(disambiguatedURL);
			}
		}
		if (s == null) {
			s = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(subjectString, "UTF8"));
		}
		if (o == null) {
			o = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(objectString, "UTF8"));
		}
		Quadruple<Node, Node, Node, String> t = new Quadruple<Node, Node, Node, String>(s, p.asNode(), o,res.getPageURL());
		return t;
	}

}
