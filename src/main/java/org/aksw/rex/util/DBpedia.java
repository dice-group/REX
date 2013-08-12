package org.aksw.rex.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBpedia {
    private Logger log = LoggerFactory.getLogger(DBpedia.class);
    private RepositoryConnection con;
    private QueryLanguage queryLanguage = QueryLanguage.SPARQL;

    public DBpedia(String publicEndpoint) throws RepositoryException {
        SPARQLRepository rep = new SPARQLRepository(publicEndpoint);
        rep.initialize();
        con = rep.getConnection();
    }

    public static void main(String args[]) throws RepositoryException, IOException {

        DBpedia dbpedia = new DBpedia("http://dbpedia.org/sparql");
        BufferedWriter bw = new BufferedWriter(new FileWriter("DBpedia_top10instances.txt"));
        for (ArrayList<String> row : dbpedia.getTopClasses()) {
            for (String cell : row) {
                bw.write(cell + "\t");
            }
            bw.write("\n");
        }
        bw.close();
    }

    public ArrayList<ArrayList<String>> getTopClasses() {
        String query = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX owl:<http://www.w3.org/2002/07/owl#> "
                + "SELECT ?s (COUNT(?i) AS ?count ) WHERE { "
                + "    ?s  rdf:type owl:Class. "
                + "    OPTIONAL { "
                + "    ?o  rdfs:subClassOf ?s. "
                + "    } "
                + "    ?i rdf:type ?s. "
                + "    FILTER(!bound(?o)&& STRSTARTS(STR(?s), \"http://dbpedia.org/ontology/\")) "
                + "} "
                + "GROUP BY ?s "
                + "ORDER BY DESC(?count) "
                + "LIMIT 10";

        return askDbpedia(query);
    }

    /**
     * returns a matrix of query results where each row represents a result and each column a variable projection of the
     * query
     * 
     */
    public ArrayList<ArrayList<String>> askDbpedia(String query) {
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        try {
            TupleQuery tupleQuery = con.prepareTupleQuery(queryLanguage, query);
            TupleQueryResultBuilder tQRW = new TupleQueryResultBuilder();
            tupleQuery.evaluate(tQRW);
            TupleQueryResult tQR = tQRW.getQueryResult();
            while (tQR.hasNext()) {
                ArrayList<String> tmp = new ArrayList<String>();
                BindingSet st = tQR.next();
                Iterator<Binding> stIterator = st.iterator();
                while (stIterator.hasNext()) {
                    // watch out! the binding has to ensure the order
                    Binding b = stIterator.next();
                    tmp.add(b.getValue().stringValue());
                }
                result.add(tmp);
            }
        } catch (RepositoryException e) {
            log.error(e.getLocalizedMessage());
        } catch (MalformedQueryException e) {
            log.error(e.getLocalizedMessage());
        } catch (QueryEvaluationException e) {
            log.error(e.getLocalizedMessage());
        } catch (TupleQueryResultHandlerException e) {
            log.error(e.getLocalizedMessage());
        }
        return result;
    }

    public void close() throws RepositoryException {
        con.close();
    }

}