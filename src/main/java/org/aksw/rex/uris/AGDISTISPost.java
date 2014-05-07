package org.aksw.rex.uris;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hp.hpl.jena.graph.Node;

import edu.stanford.nlp.util.Quadruple;

public class AGDISTISPost {
	public static void main(String[] args) throws ParseException, IOException {
		AGDISTISPost post = new AGDISTISPost();
		String subjectString = "Tom Cruise";
		String objectString = "Katie Holmes";

		Node s = null;
		Node o = null;
		String preAnnotatedText = "<entity>" + subjectString + "</entity><entity>" + objectString + "</entity>";

		HashMap<String, String> results = post.runDisambiguation(preAnnotatedText);
		for (String namedEntity : results.keySet()) {
			String disambiguatedURL = results.get(namedEntity);
			if (namedEntity.equals(subjectString) && disambiguatedURL != null) {
				s = Node.createURI(disambiguatedURL);
			}
			if (namedEntity.equals(objectString) && disambiguatedURL != null) {
				o = Node.createURI(disambiguatedURL);
			}
		}

		if (s == null) {
			s = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(subjectString, "UTF8"));
		}
		if (o == null) {
			o = Node.createURI("http://aksw.org/resource/" + URLEncoder.encode(objectString, "UTF8"));
		}
		Quadruple<Node, Node, Node, String> t = new Quadruple<Node, Node, Node, String>(s, null, o, "");

		System.out.println(t);
	}

	HashMap<String, String> runDisambiguation(String inputText) throws ParseException, IOException {
		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&type=agdistis";

		URL url = new URL("http://139.18.2.164:8080/AGDISTIS");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();

		InputStream inputStream = connection.getInputStream();
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(in);

		StringBuilder sb = new StringBuilder();
		while (reader.ready()) {
			sb.append(reader.readLine());
		}

		wr.close();
		reader.close();
		connection.disconnect();

		String agdistis = sb.toString();

		JSONParser parser = new JSONParser();
		JSONArray resources = (JSONArray) parser.parse(agdistis);

		HashMap<String, String> tmp = new HashMap<String, String>();
		for (Object res : resources.toArray()) {
			JSONObject next = (JSONObject) res;
			String namedEntity = (String) next.get("namedEntity");
			String disambiguatedURL = (String) next.get("disambiguatedURL");
			tmp.put(namedEntity, disambiguatedURL);
		}
		return tmp;
	}
}
