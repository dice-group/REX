package org.aksw.rex.uris;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

public class URIGeneratorImpl implements URIGenerator {
    private org.slf4j.Logger log = LoggerFactory.getLogger(URIGeneratorImpl.class);
    private SurfaceFormIndex index;

    public URIGeneratorImpl() {
        String file = "en_surface_forms.tsv.gz";
        String idxDirectory = "index/";
        String type = SurfaceFormIndex.TSV;
        String baseURI = "http://dbpedia.org/resource/";
        index = new SurfaceFormIndex(file, idxDirectory, type, baseURI);
    }

    // TODO give me subject and predicate
    public Set<Triple> getTriples(Set<ExtractionResult> posNegEx, Property p) throws Exception {
        Set<Triple> set = new HashSet<Triple>();
        // for each pos and neg example
        if (posNegEx == null) {
            return set;
        }
        for (ExtractionResult res : posNegEx) {
            // process left
            Triple process = process(res, p);
            if (process != null)
                set.add(process);
        }
        return set;
    }

    // this does not seem to do what it is supposed to. It should lookup uris for
    // both subject and object, then create a triple out of these uris. The method
    // should always return a uri, even if the resource cannot be found DBpedia
    // The system should take the most prominent URI (or simply use AGDISTIS) to resolve
    // cases where there are several URIs from which we can choose
    
    private Triple process(ExtractionResult res, Property p) throws Exception {
        if (res != null) {
            String extractedString = res.getSubject();

            // lookup in index
            HashSet<Triple> indexURL = index.search(extractedString);
            log.debug("indexURL.size(): " + indexURL.size());
            for (Triple tmp : indexURL) {
                log.debug("\t" + extractedString + " -> " + tmp.getObject());
            }
            if (indexURL != null && indexURL.size() == 1) {
                return indexURL.iterator().next();
            } else if (indexURL.size() == 0) {
                // TODO if not in index generate URI, read about URI Generation at DIEF Framework
                URI uri = new URI("http", "aksw.org", "/resource", extractedString);
                log.debug("Constructed URI: " + uri);
                // TODO replace dummy subject, predicate
                Node fakeSubject = NodeFactory.createURI("http://dbpedia.org/resource/Leipzig");
                Node fakePredicate = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#label");
                return new Triple(fakeSubject, fakePredicate, NodeFactory.createURI(uri.toString()));
            } else {
                throw new Exception("More than one candidate for \"" + extractedString + "\" detected!");
            }
        } else {
            return null;
        }
    }
}
