package edu.stanford.cs276;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.cs276.util.Pair;


//import edu.stanford.nlp.process.

/**
 * The entry class for this programming assignment.
 */
public class Rank {

  /**
   * Call this function to score and rank documents for some queries,
   * using a specified scoring function.
   * @param queryDict
   * @param scoreType
   * @param idfs
   * @return a mapping of queries to rankings
   */

    //PTBLexer temp;
    //CoreTokenFactory ctf;
    //Americanize am = new Americanize();
    static Stemmer stemmer = new Stemmer();

  /**
   * Call this function to score and rank documents for some queries,
   * using a specified scoring function.
   * @return a mapping of queries to rankings
 * @throws UnsupportedEncodingException
   */
  private static Map<Query,List<Document>> score(Map<Query, Map<String, Document>> queryDict, String scoreType, Map<String,Double> idfs) throws UnsupportedEncodingException {
    AScorer scorer = null;

    if (scoreType.equals("baseline")) {
      scorer = new BaselineScorer();
    } else if (scoreType.equals("cosine")) {
      scorer = new CosineSimilarityScorer(idfs);
    } else if (scoreType.equals("bm25")) {
      scorer = new BM25Scorer(idfs, queryDict);
    } else if (scoreType.equals("window")) {
      // feel free to change this to match your cosine scorer if you choose to build on top of that instead
      scorer = new SmallestWindowScorer(idfs, queryDict);
    } else if (scoreType.equals("extra")) {
      scorer = new ExtraCreditScorer(idfs);
    }
      // ranking result Map.
    Map<Query, List<Document>> queryRankings = new HashMap<Query, List<Document>>();
    //Map<Query,List<String>> queryRankings = new HashMap<Query,List<String>>();

    //loop through urls for query, getting scores
    for (Query query : queryDict.keySet()) {
      // Pair of url and ranked relevance.
      List<Pair<Document,Double>> docAndScores = new ArrayList<Pair<Document,Double>>(queryDict.get(query).size());
      for (String url : queryDict.get(query).keySet()) {
        Document doc = queryDict.get(query).get(url);
        //force debug string to be 1-line and truncate to only includes only first 200 characters for rendering purpose
        String debugStr = scorer.getDebugStr(doc, query).trim().replace("\n", " ");
        doc.debugStr = debugStr.substring(0, Math.min(debugStr.length(), 200));

        double score = 0;

        if (Stemmer.useStemming) {
            Query stemmed_query = getStemmedQuery(query);
            Document stemmed_document = getStemmedDocument(doc);
            score = scorer.getSimScore(stemmed_document, stemmed_query);
        } else {
            score = scorer.getSimScore(doc, query);
        }

        docAndScores.add(new Pair<Document, Double>(doc, score));
      }

      /* Sort urls for query based on scores. */
      Collections.sort(docAndScores, new Comparator<Pair<Document,Double>>() {
        @Override
        public int compare(Pair<Document, Double> o1, Pair<Document, Double> o2) {
          /*
           * TODO : Your code here
           * Define a custom compare function to help sort urls
           * urls for a query based on scores.
           */
            return o2.getSecond().compareTo(o1.getSecond());
        }
      });

      //put completed rankings into map
      List<Document> curRankings = new ArrayList<Document>();
      for (Pair<Document,Double> docAndScore : docAndScores)
        curRankings.add(docAndScore.getFirst());
      queryRankings.put(query, curRankings);
    }
    return queryRankings;
  }

  private static Query getStemmedQuery(Query query) {
      Query stemmed_query = new Query("");
      stemmed_query.queryWords.clear();

      for(String word : query.queryWords)
        stemmed_query.queryWords.add(stemmer.stem(word));

      return stemmed_query;
  }

  private static Document getStemmedDocument(Document doc) throws UnsupportedEncodingException {
      Document stemmed_doc = new Document(doc.url);

      //Stem the url
      if (doc.url != null) {
          //System.err.println("old url = " + doc.url);
          String decoded = URLDecoder.decode(doc.url, "UTF-8");
          stemmed_doc.url = stem_words(decoded.split("[^A-Za-z0-9]+"));
          //System.err.println("stemmed url = " + stemmed_doc.url);
      }

      //Stem the title
      if (doc.title != null){
          //System.err.println("old title = " + doc.title);
          stemmed_doc.title = stem_words(doc.title.split("\\s+"));
          //System.err.println("new title = " + stemmed_doc.title);
      }

      //Stem the headers
      if (doc.headers != null) {
          stemmed_doc.headers = new ArrayList<String>();

          /*
          System.err.println("old header");
          for(String str : doc.headers) {
              System.err.print(str + ", ");
          }
          System.err.println();
          */

          for(String header : doc.headers)
              stemmed_doc.headers.add(stem_words(header.split("\\s+")));

          /*
          System.err.println("new header");
          for(String str : stemmed_doc.headers) {
              System.err.print(str + ", ");
          }
          System.err.println();
          */
      }

      //Stem the body hits
      if (doc.body_hits != null) {
          /*
          for(String str : doc.body_hits.keySet()) {
              List<Integer> list = doc.body_hits.get(str);
              System.err.print("body_hits old word = " + str);
              for(int i = 0; i < list.size(); i++) {
                  System.err.print(list.get(i) + " ");
              }
          }
          System.err.println();
          */

          stemmed_doc.body_hits = stemBodyHits(doc.body_hits);

          /*
          for(String str : stemmed_doc.body_hits.keySet()) {
              List<Integer> list = stemmed_doc.body_hits.get(str);
              System.err.print("body_hits new word = " + str);
              for(int i = 0; i < list.size(); i++) {
                  System.err.print(list.get(i) + " ");
              }
          }
          System.err.println();
          */
      }

      //Stem the anchors
      if (doc.anchors != null) {
          /*
          System.err.println("old anchors");
          for(String str : doc.anchors.keySet()) {
              System.err.print(str + ": " + doc.anchors.get(str) + ", ");
          }
          System.err.println();
          */

          stemmed_doc.anchors = stemAnchors(doc.anchors);

          /*
          System.err.println("new anchors");
          for(String str : stemmed_doc.anchors.keySet()) {
              System.err.print(str + ": " + stemmed_doc.anchors.get(str) + ", ");
          }
          System.err.println();
          */
      }

      return stemmed_doc;
  }

  private static Map<String, Integer> stemAnchors(Map<String, Integer> anchors) {
      Map<String, Integer> stemmed_anchors = new HashMap<String, Integer>();

      for(String unstemmed_anchor : anchors.keySet()) {
          String stemmed_anchor = stem_words(unstemmed_anchor.split("\\s+"));

          if(stemmed_anchors.containsKey(stemmed_anchor)) {
              stemmed_anchors.put(stemmed_anchor, stemmed_anchors.get(stemmed_anchor) + anchors.get(unstemmed_anchor));
          } else {
              stemmed_anchors.put(stemmed_anchor, anchors.get(unstemmed_anchor));
          }
      }

      return stemmed_anchors;
  }

  private static Map<String, List<Integer>> stemBodyHits(Map<String, List<Integer>> body_hits) {
      HashMap<String, List<Integer>> stemmed_body_hits = new HashMap<String, List<Integer>>();

      for(String unstemmed_key : body_hits.keySet()) {

          String key = stemmer.stem(unstemmed_key);

          if(stemmed_body_hits.containsKey(key)) {
              stemmed_body_hits.get(key).addAll(body_hits.get(unstemmed_key));
          } else {
              ArrayList<Integer> list = new ArrayList<Integer>();
              list.addAll(body_hits.get(unstemmed_key));
              stemmed_body_hits.put(key, list);
          }
      }

      for(String key : stemmed_body_hits.keySet()) {
          Collections.sort(stemmed_body_hits.get(key));
      }

      return stemmed_body_hits;
  }

  private static String stem_words(String[] words) {
    StringBuilder sb = new StringBuilder();

    for(int i = 0; i < words.length; i++) {
        if(i == 0)
            sb.append(stemmer.stem(words[i]));
        else {
            sb.append(" ");
            sb.append(stemmer.stem(words[i]));
        }
    }

    return sb.toString();
  }


  /**
    * Print ranked results.
    * @param queryRankings the mapping of queries to rankings
    */
  public static void printRankedResults(Map<Query,List<Document>> queryRankings) {
    for (Query query : queryRankings.keySet()) {
      StringBuilder queryBuilder = new StringBuilder();
      for (String s : query.queryWords) {
        queryBuilder.append(s);
        queryBuilder.append(" ");
      }

      System.out.println("query: " + queryBuilder.toString());
      for (Document res : queryRankings.get(query)) {
        System.out.println(
          "  url: " + res.url + "\n" +
          "    title: " + res.title + "\n" +
          "    debug: " + res.debugStr
        );
      }
    }
  }

  /**
    * Writes ranked results to file.
    * @param queryRankings the mapping of queries to rankings
    * @param outputFilePath the destination file path
    */
  public static void writeRankedResultsToFile(Map<Query,List<Document>> queryRankings,String outputFilePath) {
    try {
      File file = new File(outputFilePath);
      // If file doesn't exists, then create it
      if (!file.exists()) {
        file.createNewFile();
      }

      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      BufferedWriter bw = new BufferedWriter(fw);

      for (Query query : queryRankings.keySet()) {
        StringBuilder queryBuilder = new StringBuilder();
        for (String s : query.queryWords) {
          queryBuilder.append(s);
          queryBuilder.append(" ");
        }

        String queryStr = "query: " + queryBuilder.toString() + "\n";
        System.out.print(queryStr);
        bw.write(queryStr);

        for (Document res : queryRankings.get(query)) {
          String urlString =
            "  url: " + res.url + "\n" +
            "    title: " + res.title + "\n" +
            "    debug: " + res.debugStr + "\n";
          System.out.print(urlString);
          bw.write(urlString);
        }
      }
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
    * Main method for Rank.
    * args[0] : signal files for ranking query, url pairs
    * args[1] : ranking function choice from {baseline, cosine, bm25, extra, window}
    * args[2] : PA1 corpus to build idf values (when args[3] is true), or existing idfs file to load (when args[2] is false)
    * args[3] : true: build from PA1 corpus, false: load from existing idfs.
    */
  public static void main(String[] args) throws IOException {

    if (args.length != 4) {
      System.err.println("Insufficient number of arguments: <sigFile> <taskOption> <idfPath> <buildFlag>");
      return;
    }

    /* sigFile : args[0], path for signal file. */
    String sigPath = args[0];
    /* taskOption : args[1], baseline, cosine (Task 1), bm25 (Task 2), window (Task 3), or extra (Extra Credit). */
    String taskOption = args[1];
    /* idfPath : args[2].
       When buildFlag is "true", set this as your PA1 corpus path.
       When buildFlag is "false", set this as your existing idfs file.
     */
    String idfPath = args[2];
    /* buildFlag : args[3].
       Set to "true", will buid idf from PA1 corpus.
       Set to "flase", load from existing idfs file.
     */
    String buildFlag = args[3];

    /* start building or loading idfs information. */
    Map<String, Double> idfs = null;
    /* idfFile you want to dump or already stored. */
    String idfFile = "idfs";
    if (buildFlag.equals("true")) {
      idfs = LoadHandler.buildDFs(idfPath, idfFile);
    } else {
      idfs = LoadHandler.loadDFs(idfFile);
    }

    if (!(taskOption.equals("baseline") || taskOption.equals("cosine") || taskOption.equals("bm25")
        || taskOption.equals("extra") || taskOption.equals("window"))) {
      System.err.println("Invalid scoring type; should be either 'baseline', 'bm25', 'cosine', 'window', or 'extra'");
      return;
    }

    /* start loading query pages to be ranked. */
    Map<Query,Map<String, Document>> queryDict = null;
    /* Populate map with features from file */
    try {
      queryDict = LoadHandler.loadTrainData(args[0]);
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* score documents for queries */
    Map<Query,List<Document>> queryRankings = score(queryDict, taskOption, idfs);
    /* print out ranking result, keep this stdout format in your submission */
    printRankedResults(queryRankings);

    //print results and save them to file "ranked.txt" (to run with NdcgMain.java)
    String outputFilePath = "ranked.txt";
    writeRankedResultsToFile(queryRankings,outputFilePath);
  }
}