package org.aksw.rex.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;

public class IndexMerger {
	/**
	 * Index all text files under a directory.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		File INDEXES_DIR = new File("ESPNIndexes_old");
		File INDEX_DIR = new File("ESPNIndex_new");

		INDEX_DIR.mkdir();

		Version luceneVersion = Version.LUCENE_44;
		Analyzer analyzer = new StandardAnalyzer(luceneVersion);

		Directory directory = new MMapDirectory(INDEX_DIR);
		IndexWriterConfig config = new IndexWriterConfig(luceneVersion, analyzer);
		IndexWriter iwriter = new IndexWriter(directory, config);

		ArrayList<Directory> indexes = new ArrayList<Directory>();

		for (File f : INDEXES_DIR.listFiles()) {
			if(f.isDirectory())
			indexes.add(new MMapDirectory(f));
		}

		System.out.print("Merging added indexes...");
		iwriter.addIndexes(indexes.toArray(new Directory[indexes.size()]));
		System.out.println("done");

		System.out.print("Optimizing index...");
		iwriter.close();
		System.out.println("done");

	}
}