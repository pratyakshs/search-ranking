package edu.stanford.cs276;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeleton code for the implementation of a
 * Cosine Similarity Scorer in Task 1.
 */
public class CosineSimilarityScorer extends AScorer {

  /*
   * TODO: You will want to tune the values for
   * the weights for each field.
   */
  double urlweight = 0.1;
  double titleweight  = 0.3;
  double bodyweight = 0.2;
  double headerweight = 0.3;
  double anchorweight = 0.2;
  double smoothingBodyLength = 5000.0;

  HashMap<String, Double> weights = new HashMap<String, Double>();

  /**
   * Construct a Cosine Similarity Scorer.
   * @param idfs the map of idf values
   */
  public CosineSimilarityScorer(Map<String,Double> idfs) {
    super(idfs);

    weights.put("url", urlweight);
    weights.put("title", titleweight);
    weights.put("body", bodyweight);
    weights.put("header", headerweight);
    weights.put("anchor", anchorweight);
  }

  /**
   * Get the net score for a query and a document.
   * @param tfs the term frequencies
   * @param q the Query
   * @param tfQuery the term frequencies for the query
   * @param d the Document
   * @return the net score
   */
  public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {
    double score = 0.0;

    /*
     * TODO : Your code here
     * See Equation 2 in the handout regarding the net score
     * between a query vector and the term score vectors
     * for a document.
     */

    for(String type : tfs.keySet()) {
      score += weights.get(type) * dotProduct(tfs.get(type), tfQuery);
    }

    return score;
  }

  //every term inside doc keyset should also be inside query
  private double dotProduct(Map<String, Double> doc, Map<String, Double> query) {
	  double ret = 0.0;
	  for(String term : doc.keySet())
		  ret += doc.get(term) * query.get(term);
	  return ret;
  }

  /**
   * Normalize the term frequencies.
   * @param tfs the term frequencies
   * @param d the Document
   * @param q the Query
   */
  public void normalizeTFs(Map<String,Map<String, Double>> tfs,Document d, Query q) {
    /*
     * TODO : Your code here
     * Note that we should give uniform normalization to all
     * fields as discussed in the assignment handout.
     */

    for(String type : tfs.keySet()) {
      for(String term : tfs.get(type).keySet()) {
        double tf = tfs.get(type).get(term);

        if(tf > 0) {
          tfs.get(type).put(term, (1.0 + Math.log(tf)) / (smoothingBodyLength + d.body_length));
        } else {
          //actually this isn't necessary, don't think you can have negative
          tfs.get(type).put(term, 0.0);
        }
      }
    }
  }

  /**
   * Write the tuned parameters of cosineSimilarity to file.
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
        "urlweight", "titleweight", "bodyweight", "headerweight",
        "anchorweight", "smoothingBodyLength"
      };
      double[] values = {
        this.urlweight, this.titleweight, this.bodyweight,
    this.headerweight, this.anchorweight, this.smoothingBodyLength
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
  /** Get the similarity score between a document and a query.
   * @param d the Document
   * @param q the Query
   * @return the similarity score.
   */
  public double getSimScore(Document d, Query q) throws UnsupportedEncodingException {
    Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
    this.normalizeTFs(tfs, d, q);
    Map<String,Double> tfQuery = getQueryFreqs(q);

    // Write out tuned cosineSimilarity parameters
    // This is only used for grading purposes.
    // You should NOT modify the writeParaValues method.
    writeParaValues("cosinePara.txt");
    return getNetScore(tfs,q,tfQuery,d);
  }
}
