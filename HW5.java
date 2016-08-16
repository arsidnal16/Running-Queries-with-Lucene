package lucene;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;


/**
 * To create Apache Lucene index in a folder and add files into this index based
 * on the input of the user.
 */
public class HW5 {
	//private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
	private static Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_47);
	//private static DefaultCategoryDataset dataset = new DefaultCategoryDataset();

	private IndexWriter writer;
	private ArrayList<File> queue = new ArrayList<File>();
	public static boolean DESC = false;
	public static void main(String[] args) throws IOException {
		System.out
		.println("Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\\temp\\index)");

		String indexLocation = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();

		HW5 indexer = null;
		try {

			indexLocation = s;
			indexer = new HW5(s);
		} catch (Exception ex) {
			System.out.println("Cannot create index..." + ex.getMessage());
			System.exit(-1);
		}

		// ===================================================
		// read input from user until he enters q for quit
		// ===================================================
		while (!s.equalsIgnoreCase("q")) {
			try {
				System.out
				.println("Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
				System.out
				.println("[Acceptable file types: .xml, .html, .html, .txt]");
				s = br.readLine();
				if (s.equalsIgnoreCase("q")) {
					break;
				}

				// try to add file into the index
				indexer.indexFileOrDirectory(s);
			} catch (Exception e) {
				System.out.println("Error indexing " + s + " : "
						+ e.getMessage());
			}
		}

		// ===================================================
		// after adding, we always have to call the
		// closeIndex, otherwise the index is not created
		// ===================================================
		indexer.closeIndex();

		// =========================================================
		// Now search
		// =========================================================

		// IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));

		/* Document doc = reader.document(docNbr);         
	    System.out.println("Processing file: "+doc.get("id"));



	    reader.close();     
		 */

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(3, true);
		//
		Bits s1 = MultiFields.getLiveDocs(reader);
		//Terms termVector = reader.getTermVector(docNbr, "contents");
		TermsEnum itr = MultiFields.getFields(reader).terms("contents").iterator(null);
		BytesRef term = null;
		Map<String, Integer> passedMap = new HashMap<String, Integer>();
		while ((term = itr.next()) != null) {               
			String termText = term.utf8ToString();
			Term termInstance = new Term("contents", term);                              
			int termFreq = (int) reader.totalTermFreq(termInstance);
			int docCount = reader.docFreq(termInstance);

			passedMap.put(termText, termFreq);
		}
		PrintWriter pw = new PrintWriter("SortedSid.txt", "UTF-8");
		Map<String, Integer> sortedMapDesc = sortByComparator(passedMap, DESC);

		for (Entry<String, Integer> entry : sortedMapDesc.entrySet())

		{
			// System.out.println("Key : " + entry.getKey() + " Value : "+ entry.getValue());
			//plotchart(entry.getKey(), entry.getValue());
			//File f = new File("SortedSid.txt");

			//pw.println("Key : " + entry.getKey() + " Value : "+ entry.getValue());

		}


		double mapAvgPrecision = 0;
		
		HashMap<Integer, ArrayList<Integer>> queryMap = new HashMap<Integer, ArrayList<Integer>>();
		queryMap.put(12, new ArrayList<Integer>());
		queryMap.put(13, new ArrayList<Integer>());
		queryMap.put(19, new ArrayList<Integer>());

		HashMap<Integer, TreeMap<Integer, HashMap<String, String>>> resultMap = new HashMap<Integer, TreeMap<Integer, HashMap<String, String>>>();
		resultMap.put(12, new TreeMap<Integer, HashMap<String, String>>());
		resultMap.put(13, new TreeMap<Integer, HashMap<String, String>>());
		resultMap.put(19, new TreeMap<Integer, HashMap<String, String>>());

		HashMap<Integer, String> map = new HashMap<Integer, String>();
		map.put(12, "portable operating systems");
		map.put(13, "code optimization for space efficiency");
		map.put(19, "parallel algorithms");

		//Scanner in = new Scanner(new FileReader("cacm.txt"));
		FileInputStream fstream = new FileInputStream("cacm.txt");
		BufferedReader br2 = new BufferedReader(new InputStreamReader(fstream));

		String line;
		String[] token;
		//Read File Line By Line
		while ((line = br2.readLine()) != null)   {



			//System.out.println(line);
			token = line.split("\\s+");
			//System.out.println(token);

			if(map.containsKey(Integer.valueOf(token[0]))){
				String[] num = token[2].split("-");
				ArrayList<Integer> value = queryMap.get(Integer.valueOf(token[0]));
				value.add(Integer.valueOf(num[1]));
				queryMap.put(Integer.valueOf(token[0]), value);
			} else {
				continue;
			}

		}
		br.close();

		//System.out.println(queryMap);


		for(Map.Entry<Integer, String> a : map.entrySet()) {

			int relevanceCount = 0;
			Double[] idcg = new Double[100];
			double prevDCG = 0;
			double averagePrecision = 0;
			

			//for ( String value : map.values() ) {
			Query q;
			try {
				//System.out.println(a.toString());
				q = new QueryParser(Version.LUCENE_47, "contents", analyzer).parse(a.getValue());
				TopDocs docs = searcher.search(q, 100);
				ScoreDoc[] hits = docs.scoreDocs;
				
				
				
				//System.out.println("Found " + hits.length + " hits.");
				for (int i = 0; i < hits.length; ++i) {

					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					int relevance = 0;


					if(queryMap.get(a.getKey()).contains(Integer.valueOf((d.get("path").
							split("-")[1].split("\\.")[0])))){
						relevance = 1;
						relevanceCount++;
					}
					idcg[i] = (double)relevance;
					HashMap<String, String> details = new HashMap<String, String>();
					details.put("Rank", (String.valueOf(i+1)));
					details.put("Document ID",d.get("filename"));
					details.put("Document Score", String.valueOf(hits[i].score));
					details.put("Relevance Level", String.valueOf(relevance));
					Double dcg = (double) relevance;
					if(i!=0){
						dcg = relevance/(Math.log(i+1)/Math.log(2)) + prevDCG;
					}
					prevDCG = dcg;
					details.put("NDCG", String.valueOf(dcg));
					
					TreeMap<Integer, HashMap<String, String>> table1= resultMap.get(a.getKey());
					table1.put(i+1, details);
					//System.out.println("Query " + a.getKey() + "  Relevance " + relevance);
					resultMap.put(a.getKey(), table1);

				}


			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Arrays.sort(idcg, Collections.reverseOrder());
			
			int currentRelevanceCount = 0;
			TreeMap<Integer, HashMap<String, String>> table1= resultMap.get(a.getKey());
			for(int i : table1.keySet()){

				if(table1.get(i).get("Relevance Level").equals("1")){
					currentRelevanceCount++;
				}
				double precision = (double)currentRelevanceCount/(double)i;
				double recall = (double)currentRelevanceCount/(double)relevanceCount;
				HashMap<String, String> details = table1.get(i);
				details.put("Precision", String.valueOf(precision));
				details.put("Recall", String.valueOf(recall));
				
				double tempDCG = Double.valueOf(details.get("NDCG"));
				double tempIDCG = idcg[i-1];
				double ndcg = tempDCG;
				if(i!=1){
					tempIDCG = idcg[i-1]/(Math.log(i)/Math.log(2)) + prevDCG;
					ndcg =  tempDCG/tempIDCG;
				}
				prevDCG = tempIDCG;
				
				details.put("NDCG", String.valueOf(ndcg));
				//System.out.println("DCG " + tempDCG + "IDCG " + tempIDCG);
				//System.out.println("Rank " + i + "NDCGValue " + ndcg);
				table1.put(i, details);
				averagePrecision += precision;

			}
			resultMap.put(a.getKey(), table1);
			mapAvgPrecision += (averagePrecision/idcg.length);
		
		}
		
		mapAvgPrecision = mapAvgPrecision/3;
		for(Integer i : resultMap.keySet()){
			System.out.println("Result for Table " + i);
			System.out.println("Rank	Document ID	Document Score	Relevance Level	Precision	Recall	NDCG");
			// HashMap<Integer, TreeMap<Integer, HashMap<String, String>>> resultMap
			TreeMap<Integer, HashMap<String, String>> j = resultMap.get(i);
			for(Integer k : j.keySet()){
				HashMap<String, String> values = j.get(k);
				System.out.println(values.get("Rank") + "	" + values.get("Document ID") + "	" +
								values.get("Document Score") + "	" + values.get("Relevance Level") + "	" +
								values.get("Precision") + "	" + values.get("Recall") + "	" +
								values.get("NDCG"));
			}
			System.out.println();
		}
		System.out.println("MAP: " + mapAvgPrecision); 

	}



	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {


		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(unsortMap.entrySet());

		// Sorting the list based on values

		Collections.sort(list, new Comparator<Entry<String, Integer>>()
				{

			@Override 
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2)
			{


				if (order)
				{
					return o1.getValue().compareTo(o2.getValue());
				}
				else
				{
					return o2.getValue().compareTo(o1.getValue());

				}

			}
				});


		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}




	/**
	 * Constructor
	 * 
	 * @param indexDir
	 *            the name of the folder in which the index should be created
	 * @throws java.io.IOException
	 *             when exception creating index.
	 */

	HW5(String indexDir) throws IOException {	
		FSDirectory dir = FSDirectory.open(new File(indexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,analyzer);
		writer = new IndexWriter(dir, config);

	}

	/**
	 * Indexes a file or directory
	 * 
	 * @param fileName
	 *            the name of a text file or a folder we wish to add to the
	 *            index
	 * @throws java.io.IOException
	 *             when exception
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
				removehtml(f);
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
		System.out
		.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");

		queue.clear();
	}

	private void removehtml(File f) {
		// TODO Auto-generated method stub
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;

		//Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				String noHTMLString = strLine.replaceAll("\\<.*?>","");

			}
		} catch (IOException e) {

			e.printStackTrace();
		}   	 

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
			// Only index text files
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
	 * Close the index.
	 * 
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException {
		writer.close();
	}
}