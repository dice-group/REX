package org.aksw.rex.uris;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.aksw.agdistis.webapp.AGDISTIS;
import org.aksw.rex.results.ExtractionResult;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

import datatypeshelper.utils.doc.ner.NamedEntityInText;

public class URIGeneratorAGDISTIS implements URIGenerator {
	private org.slf4j.Logger log = LoggerFactory.getLogger(URIGeneratorAGDISTIS.class);
	private SurfaceFormIndex index;
	private AGDISTIS agdistis;

	public URIGeneratorAGDISTIS() {
		// String modelDirectory = "/Users/ricardousbeck/dbpedia_en";
		//TODO error?
		String modelDirectory = "/Users/ricardousbeck/Dropbox/Dissertation/git-workspace/REX/";
		this.agdistis = new AGDISTIS(modelDirectory);
	}

	@Override
	public Set<Triple> getTriples(Set<ExtractionResult> pairs, Property p) throws Exception {
		Set<Triple> set = new HashSet<Triple>();
		// for each pos and neg example
		if (pairs == null) {
			return set;
		}
		for (ExtractionResult res : pairs) {
			// process left
			log.info("Disambiguating:" + res.getSubject() +", "+ res.getObject());
			Triple process = process(res, p);
			if (process != null)
				set.add(process);
		}
		return set;
	}

	private Triple process(ExtractionResult res, Property p) throws Exception {
		String subjectString = res.getSubject();
		String objectString = res.getObject();

		if(subjectString==null||subjectString.equals(""))
			return null;
		if(objectString==null||objectString.equals(""))
			return null;
		Node s = null;
		Node o = null;
		String preAnnotatedText = "<entity>" + subjectString + "</entity><entity>" + objectString + "</entity>";

		HashMap<NamedEntityInText, String> results = agdistis.runDisambiguation(preAnnotatedText);
		for (NamedEntityInText namedEntity : results.keySet()) {
			String disambiguatedURL = results.get(namedEntity);
			if (namedEntity.getLabel().equals(subjectString)&&disambiguatedURL!=null) {
				s = Node.createURI(disambiguatedURL);
			}
			if (namedEntity.getLabel().equals(objectString)&&disambiguatedURL!=null) {
				o = Node.createURI(disambiguatedURL);
			}
		}
		if (s == null) {
			s = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(subjectString, "UTF8"));
		}
		if (o == null) {
			o = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(objectString, "UTF8"));
		}
		Triple t = new Triple(s, p.asNode(), o);
		return t;
	}

}
