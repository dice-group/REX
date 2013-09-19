/**
 * 
 */
package org.aksw.rex.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class SurfaceFormGenerator {

	private static IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	
	public static Set<String> getSurfaceForms(SparqlEndpoint endpoint, String uri){
		Set<String> surfaceforms = new HashSet<String>();
		
		String query = "SELECT ?l WHERE {<" + uri + "> rdfs:label ?l. FILTER(LANGMATCHES(LANG(?l),'en'))}";
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		ResultSet rs = qef.createQueryExecution(query).execSelect();
		while(rs.hasNext()){
			surfaceforms.add(cleanUp(rs.next().getLiteral("l").getLexicalForm()));
		}
		
		//Fallback: Use short form of URI
		if(surfaceforms.isEmpty()){
			try {
				uri = URLDecoder.decode(uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			surfaceforms.add(cleanUp(sfp.getShortForm(IRI.create(uri)).replace("_", " ")));
		}
		return surfaceforms;
	}
	
	/**
	 * Remove content like (something)
	 * @param s
	 */
	private static String cleanUp(String s){
		if(s.contains("(")){
			return s.substring(0, s.indexOf('(')).trim();
		}
		return s;
	}
}
