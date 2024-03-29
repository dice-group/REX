package org.aksw.rex.crawler;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Page;

import org.aksw.rex.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;

/**
 * Lucene index to provide crawled data to REX
 * 
 * @author r.usbeck
 * 
 */
public class CrawlIndex {
	private org.slf4j.Logger log = LoggerFactory.getLogger(CrawlIndex.class);
	public static final String TSV = "TSV";
	private String FIELD_NAME_URL = "url";
	private String FIELD_NAME_HTML = "html";
	private String FIELD_NAME_CONTENT = "content";
	private Directory directory;
	private Analyzer analyzer;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private String name;

	/**
	 * constructor creates or opens an already existing index
	 * 
	 * @param file
	 */
	public CrawlIndex(String file) {
		this.name = file;
		log.info("Building CrawlIndex!");
		try {
			Version luceneVersion = Version.LUCENE_44;
			analyzer = new StandardAnalyzer(luceneVersion);
			File indexDirectory = new File(file);

			if (indexDirectory.exists() && indexDirectory.isDirectory() && indexDirectory.listFiles().length > 0) {
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(luceneVersion, analyzer);
				iwriter = new IndexWriter(directory, config);
			} else {
				indexDirectory.mkdir();
				directory = new MMapDirectory(indexDirectory);
				IndexWriterConfig config = new IndexWriterConfig(luceneVersion, analyzer);
				iwriter = new IndexWriter(directory, config);
			}
			iwriter.commit();
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getLocalizedMessage());
			log.error("ERROR while building index");
		}
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Done.");
		log.info("Number of documents: " + ireader.numDocs());
	}

	/**
	 * searches in the URL field of the index for the queryString
	 * 
	 * @param queryString
	 * @return pairs of (URL, HTML)
	 */
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
		} catch (IOException e) {
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + queryString);
		}
		return sites;
	}

	/**
	 * searches in the HTML field of the index for the queryString
	 * 
	 * @param queryString
	 * @return pairs of (URL, HTML)
	 */
	public ArrayList<Pair<String, String>> searchHTML(String queryString) {
		ArrayList<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			log.debug("\tRetrieving documents from index...");

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
			log.debug("\t...got " + sites.size() + " documents.");
		} catch (IOException e) {
			System.out.println(queryString);
			e.printStackTrace();
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			log.error(e.getLocalizedMessage() + " -> " + queryString);
		}
		return sites;
	}

	/**
	 * @param domainURL
	 * @return all pairs of (URL, HTML) under a given domain
	 */
	public List<Pair<String, String>> getDocumentsWithDomain(String domainURL) {
		List<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			log.debug("\tStart asking index...");
			Query q = new PrefixQuery(new Term(FIELD_NAME_URL, domainURL));
			TopScoreDocCollector collector = TopScoreDocCollector.create(1000, true);
			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				sites.add(new Pair<String, String>(hitDoc.get(FIELD_NAME_URL), hitDoc.get(FIELD_NAME_HTML)));
			}
		} catch (IOException e) {
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + domainURL);
		}
		return sites;
	}

	/**
	 * 
	 * @param i
	 * @return the ith document from the index
	 */
	public ArrayList<Pair<String, String>> getDocument(int i) {
		ArrayList<Pair<String, String>> sites = new ArrayList<Pair<String, String>>();
		try {
			if (ireader == null) {
				ireader = DirectoryReader.open(directory);
				isearcher = new IndexSearcher(ireader);
			}
			// ireader.document(i);
			Document hitDoc = ireader.document(i);
			sites.add(new Pair<String, String>(hitDoc.get(FIELD_NAME_URL), hitDoc.get(FIELD_NAME_HTML)));
		} catch (IOException e) {
			e.printStackTrace();
			log.error("COULD NOT SEARCH INDEX");
		} catch (Exception e) {
			log.error("Could not find document: " + i);
		}
		return sites;
	}

	/**
	 * adds a document (URL, HTML) to the index
	 * 
	 * @param url
	 * @param html
	 */
	public void addDocumentToIndex(String url, String html) {
		Document doc = new Document();
		org.jsoup.nodes.Document htmlDoc = Jsoup.parse(html);
		htmlDoc.select("script, jscript").remove();
		html = htmlDoc.html();

		doc.add(new StringField(FIELD_NAME_URL, url, Store.YES));
		doc.add(new TextField(FIELD_NAME_HTML, html, Store.YES));
		try {
			doc.add(new TextField(FIELD_NAME_CONTENT, HTMLExtractor.getHTMLContent(html), Store.NO));
		} catch (Exception e) {
			doc.add(new TextField(FIELD_NAME_CONTENT, html, Store.NO));
		}

		try {
			iwriter.addDocument(doc);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			log.error("\tURL:" + url);
			log.error("\tHTML:" + html);
		}
	}

	/**
	 * close the index, important to prevent data loss after adding documents
	 */
	public void close() {
		try {
			iwriter.close();
			directory.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	/**
	 * 
	 * @return size of index
	 * @throws IOException
	 */
	public int size() throws IOException {
		if (ireader == null) {
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		}
		return ireader.numDocs();
	}
/**
 * 
 * @return name aka absolute file name of the index
 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return all pages of the index. might be huge.
	 */
	public Set<Page> getAllPages() {
		Set<Page> pages = new HashSet<Page>();
		for (int i = 0; i < ireader.maxDoc(); i++) {
			try {
				Document doc = ireader.document(i);
				String url = doc.get(FIELD_NAME_URL);
				String html = doc.get(FIELD_NAME_HTML);
				pages.add(new Page(html, null, url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pages;
	}

	/**
	 * 
	 * @param pageNumber
	 * @return all pages from index 0 to pageNumber
	 */
	public Set<Page> getPages(int pageNumber) {
		Set<Page> pages = new HashSet<Page>();
		int maxPages = ireader.maxDoc() > pageNumber ? pageNumber : ireader.maxDoc();

		for (int i = 0; i < maxPages; i++) {
			try {
				Document doc = ireader.document(i);
				String url = doc.get(FIELD_NAME_URL);
				String html = doc.get(FIELD_NAME_HTML);
				pages.add(new Page(html, null, url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pages;
	}
}
