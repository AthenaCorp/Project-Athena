package athena.execute;

import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;


public class LuceneExecutor {

    private CommonUtils commonUtils = new CommonUtils();

    private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
    private static Analyzer Sanalyzer = new SimpleAnalyzer(Version.LUCENE_47);

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<>();

    public void executor() {
        IndexSearcher searcher;
        TopScoreDocCollector collector;
        try {
            String indexLocation = commonUtils.getOutputPath() + "\\LuceneIndex";
            /*String folderPath = commonUtils.getResourcePath() + "\\cacm";
            FSDirectory dir = FSDirectory.open(new File(indexLocation));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, Sanalyzer);
            writer = new IndexWriter(dir, config);
            indexFileOrDirectory(folderPath);
            writer.close();*/


            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
            searcher = new IndexSearcher(reader);
            collector = TopScoreDocCollector.create(100, true);
            String outputFolder = commonUtils.getOutputPath() + "\\Lucene";
            commonUtils.verifyFolder(outputFolder);
            Map<Integer, String> queries = SearchEngineUtils.getQuerySet(commonUtils.getResourcePath() + "query\\cacm.query.txt");
            for (int j = 1; j <= queries.size(); j++) {
                String s = queries.get(j);
                System.out.println("Searching: " + s);
                Query q = new QueryParser(Version.LUCENE_47, "contents", Sanalyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");
                FileWriter fileWriter = new FileWriter(new File(outputFolder + "\\1.txt"));
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    fileWriter.write(1 + " Q0 " + d.get("filename") + " " + (i + 1) + " " + hits[i].score + " Lucene\n");
                    //System.out.println((i + 1) + ". " + d.get("filename") + " score=" + hits[i].score);
                }
                // 5. term stats --> watch out for which "version" of the term
                // must be checked here instead!
                fileWriter.close();
                Term termInstance = new Term("contents", s);
                long termFreq = reader.totalTermFreq(termInstance);
                long docCount = reader.docFreq(termInstance);
                System.out.println(s + " Term Frequency " + termFreq + " - Document Frequency " + docCount + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Cannot create athena.index..." + ex.getMessage());
            System.exit(-1);
        }
    }


    LuceneExecutor() {
    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the
     *                 athena.index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
        // ===================================================
        // gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name)
        // ===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();

                // ===================================================
                // add contents of file
                // ===================================================
                fr = new FileReader(f);
                doc.add(new TextField("contents", fr));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(),
                        Field.Store.YES));

                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            // ===================================================
            // Only athena.index text files
            // ===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the athena.index.
     *
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}
