package org.aksw.rex.crawler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.aksw.rex.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

public class CrawlIndex {
	private org.slf4j.Logger log = LoggerFactory.getLogger(CrawlIndex.class);
	public static final String TSV = "TSV";
	private String FIELD_NAME_URL = "url";
	private String FIELD_NAME_HTML = "html";
	private Directory directory;
	private Analyzer analyzer;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;

	public CrawlIndex(String file) {
		log.info("Building CrawlIndex!");
		try {
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			File indexDirectory = new File(file);

			if (indexDirectory.exists() && indexDirectory.isDirectory() && indexDirectory.listFiles().length > 0) {
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
				iwriter = new IndexWriter(directory, config);
			} else {
				indexDirectory.mkdir();
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
				iwriter = new IndexWriter(directory, config);
			}

		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
		log.info("Finished building CrawlIndex!");
	}

	public ArrayList<Pair<String, String>> searchURL(String queryString) {
		ArrayList<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			log.debug("\tStart asking index...");
			TermQuery tq = new TermQuery(new Term(FIELD_NAME_URL, queryString));
			BooleanQuery bq = new BooleanQuery();
			bq.add(tq, BooleanClause.Occur.SHOULD);
			TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
			isearcher.search(bq, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				sites.add(new Pair<String, String>(hitDoc.get(FIELD_NAME_URL), hitDoc.get(FIELD_NAME_HTML)));
			}
			log.debug("\tFinished asking index...");
		} catch (IOException e) {
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + queryString);
		}
		return sites;
	}

	public ArrayList<Pair<String, String>> searchHTML(String queryString) {
		ArrayList<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			log.debug("\tStart asking index...");

			BooleanQuery bq = new BooleanQuery();
			TokenStream stream = analyzer.tokenStream(FIELD_NAME_HTML, new StringReader(queryString));
			stream.reset();
			while (stream.incrementToken()) {
				TermQuery tq = new TermQuery(new Term(FIELD_NAME_HTML, stream.getAttribute(CharTermAttribute.class).toString()));
				bq.add(tq, BooleanClause.Occur.MUST);
			}

			TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
			isearcher.search(bq, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				sites.add(new Pair<String, String>(hitDoc.get(FIELD_NAME_URL), hitDoc.get(FIELD_NAME_HTML)));
			}
			log.debug("\tFinished asking index...");
		} catch (IOException e) {
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			log.error(e.getLocalizedMessage() + " -> " + queryString);
		}
		return sites;
	}

	public ArrayList<Pair<String, String>> getDocument(int i) {
		ArrayList<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			ireader.document(i);
			Document hitDoc = ireader.document(i);
			sites.add(new Pair<String, String>(hitDoc.get(FIELD_NAME_URL), hitDoc.get(FIELD_NAME_HTML)));
			log.debug("\t finished asking index...");
		} catch (IOException e) {
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			log.error("Could not find document: " + i);
		}
		return sites;
	}

	public void addDocumentToIndex(String url, String html) {
		Document doc = new Document();
		doc.add(new TextField(FIELD_NAME_URL, url, Store.YES));
		doc.add(new TextField(FIELD_NAME_HTML, html, Store.YES));
		try {
			iwriter.addDocument(doc);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			log.error("\tURL:" + url);
			log.error("\tHTML:" + html);
		}
	}

	public void close() {
		try {
			iwriter.close();
			directory.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}
}
