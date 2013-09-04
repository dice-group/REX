/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.domainidentifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.aksw.rex.util.GoogleResults;
import org.aksw.rex.util.GoogleResults.Result;
import org.aksw.rex.util.Pair;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 * @author ngonga
 */
public class GoogleDomainIdentifier implements DomainIdentifier {

    public static String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    public String charset = "UTF-8";
    public static String cacheLocation = "resources/googlecache";
    public Map<Property, URL> cache;

    public GoogleDomainIdentifier() {
        readCache();
    }

    /**
     * Read the cache to a file
     */
    public void readCache() {
        cache = new HashMap<Property, URL>();
        try {
            if (new File(cacheLocation).exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheLocation), "UTF8"));
                String s = reader.readLine();
                while (s != null) {
                    Property p = ResourceFactory.createProperty(s.split("\t")[0]);
                    URL u = new URL(s.split("\t")[1]);
                    cache.put(p, u);
                    s = reader.readLine();
                }
                reader.close();
            }
        } catch (Exception e) {
            Logger.getLogger(GoogleDomainIdentifier.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Write the cache to a file
     */
    public void writeCache() {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(cacheLocation)));
            for(Property p: cache.keySet())
            {
                writer.println(p.getURI()+"\t"+cache.get(p));
            }
            writer.close();
        } catch (Exception e) {
            Logger.getLogger(GoogleDomainIdentifier.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Returns the domain from which knowledge on the posExamples can be
     * extracted
     *
     * @param p
     * @param posExamples
     * @param negExamples
     * @param useCache
     * @return URL of domain
     */
    @Override
	public URL getDomain(Property p, Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples, boolean useCache) {
        Map<URL, Double> count = new HashMap<URL, Double>();

        if (useCache) {
            if (cache.containsKey(p)) {
                return cache.get(p);
            }
        }
        //assume that positive and negative examples are disjoint
        Set<Pair<Resource, Resource>> examples = new HashSet<Pair<Resource, Resource>>(posExamples);
        examples.addAll(negExamples);
        try {
            for (Pair<Resource, Resource> pair : examples) {
                String search = "\"" + getLabel(pair.getLeft()) + "\" " + "\"" + getLabel(pair.getRight()) + "\"";
                URL url = new URL(google + URLEncoder.encode(search, charset));
                System.out.println(url);
                Reader reader = new InputStreamReader(url.openStream(), charset);
                GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

                // Show title and URL of 1st result.
                for (int i = 0; i < results.getResponseData().getResults().size(); i++) {
                    Result r = results.getResponseData().getResults().get(i);
                    URL domain = getHost(r);
                    if (domain != null) {
                        if (!count.containsKey(domain)) {
                            count.put(domain, 0d);
                        }
                        double c = count.get(domain);
                        count.remove(domain);
                        if (posExamples.contains(pair)) {
                            count.put(domain, c + (1d / (i + 1)));
                        } else {
                            count.put(domain, c - (1d / (i + 1)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(GoogleDomainIdentifier.class.getName()).log(Level.SEVERE, null, e);
        }

        double max = 0d;
        URL result = null;
        for (URL u : count.keySet()) {
            if (count.get(u) > max) {
                max = count.get(u);
                result = u;
            }
        }

        cache.put(p, result);
        
        if (useCache) {
                writeCache();
        }

        return result;
    }

    /**
     * Gets label of DBpedia resources
     *
     * @param r Resource
     * @return Label of resource
     */
    public String getLabel(Resource r) {
        return splitAtCamelCase(r.getLocalName());
    }

    private String splitAtCamelCase(String s) {
        String regex = "([a-z])([A-Z])";
        String replacement = "$1 $2";
        s = s.replaceAll(Pattern.quote("_"), " ");
        return s.replaceAll(regex, replacement).toLowerCase();
    }

    /**
     * Gets the host of a URL. Might need some refining
     *
     * @param r Google result
     * @return URL
     */
    public URL getHost(Result r) {
        URL result = null;
        try {
            URL url = new URL(r.getUrl());
            result = new URL("http://" + url.getHost());
        } catch (MalformedURLException ex) {
            Logger.getLogger(GoogleDomainIdentifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static void main(String args[]) {
        Set<Pair<Resource, Resource>> posExamples = new HashSet<Pair<Resource, Resource>>();
        Resource r1 = ResourceFactory.createResource("http://dbpedia.org/resource/Tom_Cruise");
        Resource r2 = ResourceFactory.createResource("http://dbpedia.org/resource/Minority_Report");
        Set<Pair<Resource, Resource>> negExamples = new HashSet<Pair<Resource, Resource>>();
        posExamples.add(new Pair(r1, r2));
        Resource r3 = ResourceFactory.createResource("http://dbpedia.org/resource/Don_Johnson");
        Resource r4 = ResourceFactory.createResource("http://dbpedia.org/resource/Miami_Vice");
        posExamples.add(new Pair(r3, r4));
        GoogleDomainIdentifier gdi = new GoogleDomainIdentifier();
        System.out.println(gdi.getDomain(ResourceFactory.createProperty("http://dbpedia.org/ontology/starring"), posExamples, negExamples, true));
    }
}
