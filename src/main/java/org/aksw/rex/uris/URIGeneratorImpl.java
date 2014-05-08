package org.aksw.rex.uris;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

import edu.stanford.nlp.util.Quadruple;

public class URIGeneratorImpl implements URIGenerator {
	private org.slf4j.Logger log = LoggerFactory.getLogger(URIGeneratorImpl.class);
	private SurfaceFormIndex index;

	public URIGeneratorImpl() {
		String file = "en_surface_forms.tsv.gz";
		String idxDirectory = "surfaceform-index/";
		String type = SurfaceFormIndex.TSV;
		String baseURI = "http://dbpedia.org/resource/";
		index = new SurfaceFormIndex(file, idxDirectory, type, baseURI);
	}

	@Override
	public Set<Quadruple<Node, Node, Node, String>> getTriples(Set<ExtractionResult> posNegEx, Property p) throws Exception {
		Set<Quadruple<Node, Node, Node, String>> set = new HashSet<Quadruple<Node, Node, Node, String>>();
		// for each pos and neg example
		if (posNegEx == null) {
			return set;
		}
		for (ExtractionResult res : posNegEx) {
			// process left
			Triple process = process(res, p);
			Quadruple<Node, Node, Node, String> quad = new Quadruple<Node, Node, Node, String>(process.getSubject(), process.getPredicate(), process.getObject(), res.getPageURL());
			if (process != null)
				set.add(quad);
		}
		return set;
	}

	// TODO discuss this
	// this does not seem to do what it is supposed to. It should lookup uris
	// for
	// both subject and object, then create a triple out of these uris. The
	// method
	// should always return a uri, even if the resource cannot be found DBpedia
	// The system should take the most prominent URI (or simply use AGDISTIS) to
	// resolve
	// cases where there are several URIs from which we can choose

	private Triple process(ExtractionResult res, Property p) throws Exception {
		String subjectString = res.getSubject();
		String objectString = res.getObject();

		Node s = null;
		Node o = null;
		s = generateURI(subjectString, s);
		o = generateURI(objectString, o);
		Triple t = new Triple(s, p.asNode(), o);
		return t;
	}

	private Node generateURI(String subjectString, Node s) throws URISyntaxException, Exception {
		// lookup in index
		Set<String> possibleSubjectURIs = index.search(subjectString);
		// ---DEBUG---//
		log.trace("Number of candidates for Subject: " + possibleSubjectURIs.size());
		for (String tmp : possibleSubjectURIs) {
			log.trace("\t" + subjectString + " -> " + tmp);
		}
		// ---END DEBUG---//
		if (possibleSubjectURIs != null && possibleSubjectURIs.size() == 1) {
			s = Node.createURI(possibleSubjectURIs.iterator().next());
		} else if (possibleSubjectURIs.size() == 0) {
			// generate URI, if not in index
			URI uri = new URI("http", "aksw.org", "/resource", subjectString);
			log.trace("Constructed URI: " + uri);
			s = Node.createURI(uri.toString());
		} else {
			throw new Exception("More than one candidate for \"" + subjectString + "\" detected!");
		}
		return s;
	}
}
