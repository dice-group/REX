package org.aksw.rex.uris;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

public class SurfaceFormIndex {
	private org.slf4j.Logger log = LoggerFactory.getLogger(SurfaceFormIndex.class);
	public static final String TSV = "TSV";
	private String FIELD_NAME_URL = "url";
	private String FIELD_NAME_LABEL = "label";
	private Directory directory;
	private Analyzer analyzer;
	private IndexSearcher isearcher;
	private QueryParser parser;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private HashMap<String, HashSet<String>> cacheSearch;
	private String baseURI;

	public SurfaceFormIndex(String file, String idxDirectory, String type, String baseURI) {
		log.info("Building surface form index!");
		this.baseURI = baseURI;
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			File indexDirectory = new File(idxDirectory);

			if (indexDirectory.exists() && indexDirectory.isDirectory() && indexDirectory.listFiles().length > 0) {
				directory = new MMapDirectory(indexDirectory);
			} else {
				indexDirectory.mkdir();
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
				iwriter = new IndexWriter(directory, config);
				if (type.equals(TSV))
					indexTSVFile(file);
				iwriter.close();
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			cacheSearch = new HashMap<String, HashSet<String>>();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
		log.info("Finished building surface form index!");
	}

	private void indexTSVFile(String surfaceFormsTSV) {
		try {
			InputStream fileStream = new FileInputStream(surfaceFormsTSV);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
			BufferedReader br = new BufferedReader(decoder);
			while (br.ready()) {
				String[] line = br.readLine().split("\t");
				String subject = line[0];
				for (int i = 1; i < line.length; ++i) {
					String object = line[i];
					Document doc = new Document();
					log.debug("\t" + subject + " -> " + object);
					doc.add(new StringField(FIELD_NAME_URL, subject, Store.YES));
					doc.add(new TextField(FIELD_NAME_LABEL, object, Store.YES));
					iwriter.addDocument(doc);
				}
			}
			br.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	public HashSet<String> search(String label) {
		if (cacheSearch.containsKey(label)) {
			return cacheSearch.get(label);
		}
		HashSet<String> result = new HashSet<String>();
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			parser = new QueryParser(Version.LUCENE_40, FIELD_NAME_LABEL, analyzer);
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = parser.parse(label);
			ScoreDoc[] hits = isearcher.search(query, 10000).scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String URI = java.net.URLDecoder.decode(hitDoc.get(FIELD_NAME_URL), "UTF-8");
				if (URI.replace(baseURI, "").equals(label)) {
					result.add(URI);
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + label);
		}
		cacheSearch.put(label, result);
		return result;
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
