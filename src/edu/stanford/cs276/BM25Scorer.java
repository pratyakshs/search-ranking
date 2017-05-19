package edu.stanford.cs276;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Skeleton code for the implementation of a BM25 Scorer in Task 2.
 */
public class BM25Scorer extends AScorer {

  /*
   *  TODO: You will want to tune these values
   */
    double urlweight = 0.8;
    double titleweight  = 1.2;
    double bodyweight = 0.05;
    double headerweight = 0.8;
    double anchorweight = 0.25;

  // BM25-specific weights
  double burl = 0.2;
  double btitle = 1.5;
  double bheader = 0.8;
  double bbody = 0.3;
  double banchor = 0.3;

  double k1 = 1;
  double pageRankLambda = 5;
  double pageRankLambdaPrime = 3;

  // query -> url -> document
  Map<Query,Map<String, Document>> queryDict;

  // BM25 data structures--feel free to modify these
  // Document -> field -> length
  Map<Document,Map<String,Double>> lengths;

  // field name -> average length
  Map<String,Double> avgLengths;

  // Document -> pagerank score
  Map<Document,Double> pagerankScores;

    /**
     * Construct a BM25Scorer.
     * @param idfs the map of idf scores
     * @param queryDict a map of query to url to document
     * @throws UnsupportedEncodingException
     */
    public BM25Scorer(Map<String,Double> idfs, Map<Query,Map<String, Document>> queryDict) throws UnsupportedEncodingException {
      super(idfs);
      this.queryDict = queryDict;
      this.calcAverageLengths();
    }

    /**
     * Set up average lengths for BM25, also handling PageRank.
     * @throws UnsupportedEncodingException
     */
  public void calcAverageLengths() throws UnsupportedEncodingException {
    lengths = new HashMap<Document,Map<String,Double>>();
    avgLengths = new HashMap<String,Double>();
    pagerankScores = new HashMap<Document,Double>();

    /*
     * TODO : Your code here
     * Initialize any data structures needed, perform
     * any preprocessing you would like to do on the fields,
     * handle pagerank, accumulate lengths of fields in documents.
     */
    // url -> anchor -> count
    Map<String, Map<String, Integer>> anchors = new HashMap<String, Map<String, Integer>>();

    // url -> header
    Map<String, Set<String>> headers = new HashMap<String, Set<String>>();

    // url -> body
    Map<String, Map<String, Integer>> bodyHits = new HashMap<String, Map<String, Integer>>();

    for (Query query : queryDict.keySet()) {
        for (String url : queryDict.get(query).keySet()) {
          Document doc = queryDict.get(query).get(url);
          if (lengths.containsKey(doc)) {
              Map<String, Double> dmap = lengths.get(doc);
              // only update header, anchor
              for (String anchor : doc.anchors.keySet()) {
                  anchors.get(url).put(anchor, doc.anchors.get(anchor));
              }

              for (String header : doc.headers) {
                  headers.get(url).add(header);
              }

          } else {
              // seeing this doc for the first time
              Map<String, Double> dmap = new HashMap<String, Double>();

              // url
              double urlLen = urlSplit(doc.url.trim()).length;
              dmap.put("url", urlLen);

              // title
              double titleLen = doc.title.trim().split("\\s+").length;
              dmap.put("title", titleLen);

              // body
              double bodyLen = doc.body_length;
              dmap.put("body", bodyLen);
//              Map<String, Integer> curBodyHits = new HashMap<String, Integer>();
//              if (doc.body_hits != null) {
//                  for (String bodyWord : doc.body_hits.keySet()) {
//                      curBodyHits.put(bodyWord, doc.body_hits.get(bodyWord).size());
//                  }
//              }
//              bodyHits.put(url, curBodyHits);

              // anchor
              Map<String, Integer> curAnchors = null;
              if (doc.anchors == null) {
                  curAnchors = new HashMap<String, Integer>();
              } else {
                  curAnchors = new HashMap<String, Integer>(doc.anchors);
              }
              anchors.put(url, curAnchors);

              // header
              Set<String> curHeaders = null;
              if (doc.headers == null) {
                  curHeaders = new HashSet<String>();
              } else {
                  curHeaders = new HashSet<String>(doc.headers);
              }

              headers.put(url, curHeaders);

              lengths.put(doc, dmap);
          }
        }
    }

    // sum up lengths for anchors and headers and body
    for (Document doc : lengths.keySet()) {
        String url = doc.url;
        double anchorLen = 0;
        for (String anchor : anchors.get(url).keySet()) {
            anchorLen += (anchor.split("\\s+").length) * anchors.get(url).get(anchor);
        }
        lengths.get(doc).put("anchor", anchorLen);

        double headerLen = 0;
        for (String header : headers.get(url)) {
            headerLen += header.split("\\s+").length;
        }
        lengths.get(doc).put("header", headerLen);

//        double bodyLen = 0;
//        for (String word : bodyHits.get(url).keySet()) {
//            bodyLen += bodyHits.get(url).get(word);
//        }
//        lengths.get(doc).put("body", bodyLen);
    }

    double numDocs = lengths.size();
    for (String tfType : this.TFTYPES) {
    /*
     * TODO : Your code here
     * Normalize lengths to get average lengths for
     * each field (body, url, title, header, anchor)
     */
     double sumLengths = 0;
     for (Document doc : lengths.keySet()) {
         sumLengths += lengths.get(doc).get(tfType);
     }
     avgLengths.put(tfType, sumLengths / numDocs);
    }

    // extract the pagerank scores
    for (Document doc : lengths.keySet()) {
        pagerankScores.put(doc, (double) Math.log(pageRankLambdaPrime + doc.page_rank));
    }

  }

  /**
   * Get the net score.
   * @param tfs the term frequencies
   * @param q the Query
   * @param tfQuery
   * @param d the Document
   * @return the net score
   */
  public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery,Document d) {

    double score = 0.0;

    /*
     * TODO : Your code here
     * Use equation 5 in the writeup to compute the overall score
     * of a document d for a query q.
     */
    for (String qWord : q.queryWords) {
        double idf_weight = 0;
        if (this.idfs.containsKey(qWord))
            idf_weight = this.idfs.get(qWord);
        else
            idf_weight = Math.log(1 + this.idfs.get(LoadHandler.no_word_in_doc_idf_val));
        double wdt = 0.;
        if (tfs.get("url").containsKey(qWord)) {
            wdt += urlweight * tfs.get("url").get(qWord);
//            System.err.println("url: " + urlweight * tfs.get("url").get(qWord));
        }
        if (tfs.get("title").containsKey(qWord)) {
            wdt += titleweight * tfs.get("title").get(qWord);
//            System.err.println("title: " + titleweight * tfs.get("title").get(qWord));
        }
        if (tfs.get("body").containsKey(qWord)) {
            wdt += bodyweight * tfs.get("body").get(qWord);
//            System.err.println("body: " + bodyweight * tfs.get("body").get(qWord));
        }
        if (tfs.get("header").containsKey(qWord)) {
            wdt += headerweight * tfs.get("header").get(qWord);
//            System.err.println("header: " + headerweight * tfs.get("header").get(qWord));
        }
        if (tfs.get("anchor").containsKey(qWord)) {
            wdt += anchorweight * tfs.get("anchor").get(qWord);
//            System.err.println("anchor: " + anchorweight * tfs.get("anchor").get(qWord));
        }
//        System.err.println((wdt));
        score += (wdt * idf_weight) / (k1 + wdt);
    }
//    System.err.println(pageRankLambda * pagerankScores.get(d));
    score += pageRankLambda * pagerankScores.get(d);
    return score;
  }

  /**
   * Do BM25 Normalization.
   * @param tfs the term frequencies
   * @param d the Document
   * @param q the Query
   */
  public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
  /*
   * TODO : Your code here
   * Use equation 3 in the writeup to normalize the raw term frequencies
   * in fields in document d.
   */
      for (String tfType : this.TFTYPES) {
          for (String qWord : tfs.get(tfType).keySet()) {
              double numerator = tfs.get(tfType).get(qWord);
              double denominator = 0;
              if (tfType.equals("url")) {
                  denominator = 1. + burl * ((lengths.get(d).get(tfType) / avgLengths.get(tfType)) - 1.);
              } else if (tfType.equals("title")) {
                  denominator = 1. + btitle* ((lengths.get(d).get(tfType) / avgLengths.get(tfType)) - 1.);
              } else if (tfType.equals("body")) {
                  denominator = 1. + bbody * ((lengths.get(d).get(tfType) / avgLengths.get(tfType)) - 1.);
              } else if (tfType.equals("header")) {
                  denominator = 1. + bheader * ((lengths.get(d).get(tfType) / avgLengths.get(tfType)) - 1.);
              } else if (tfType.equals("anchor")) {
                  denominator = 1. + banchor * ((lengths.get(d).get(tfType) / avgLengths.get(tfType)) - 1.);
              }
              tfs.get(tfType).put(qWord, numerator / denominator);
          }
      }
  }

  /**
   * Write the tuned parameters of BM25 to file.
   * Only used for grading purpose, you should NOT modify this method.
   * @param filePath the output file path.
   */
  private void writeParaValues(String filePath) {
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter fw = new FileWriter(file.getAbsoluteFile());
      String[] names = {
        "urlweight", "titleweight", "bodyweight",
        "headerweight", "anchorweight", "burl", "btitle",
        "bheader", "bbody", "banchor", "k1", "pageRankLambda", "pageRankLambdaPrime"
      };
      double[] values = {
        this.urlweight, this.titleweight, this.bodyweight,
        this.headerweight, this.anchorweight, this.burl, this.btitle,
        this.bheader, this.bbody, this.banchor, this.k1, this.pageRankLambda,
        this.pageRankLambdaPrime
      };
      BufferedWriter bw = new BufferedWriter(fw);
      for (int idx = 0; idx < names.length; ++ idx) {
        bw.write(names[idx] + " " + values[idx]);
        bw.newLine();
      }
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  /**
   * Get the similarity score.
   * @param d the Document
   * @param q the Query
   * @return the similarity score
   */
  public double getSimScore(Document d, Query q) throws UnsupportedEncodingException {
    Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
    this.normalizeTFs(tfs, d, q);
    Map<String,Double> tfQuery = getQueryFreqs(q);

    // Write out the tuned BM25 parameters
    // This is only used for grading purposes.
    // You should NOT modify the writeParaValues method.
    writeParaValues("bm25Para.txt");

//    CosineSimilarityScorer scorer = new CosineSimilarityScorer(idfs);
    double score1 = getNetScore(tfs,q,tfQuery,d);
//    double score2 = scorer.getSimScore(d, q);
//    System.err.println(q + " "+ score1 + " " + score2);
    return 1 * score1;// + 000 * score2;
  }

}
