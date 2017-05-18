package edu.stanford.cs276;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for a scorer.
 * Needs to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {

  // Map: term -> idf
  Map<String,Double> idfs;

  // Various types of term frequencies that you will need
  String[] TFTYPES = {"url","title","body","header","anchor"};

  /**
   * Construct an abstract scorer with a map of idfs.
   * @param idfs the map of idf scores
   */
  public AScorer(Map<String,Double> idfs) {
    this.idfs = idfs;
  }

  /**
  * You can implement your own function to whatever you want for debug string
  * The following is just an example to include page information in the debug string
  * The string will be forced to be 1-line and truncated to only include the first 200 characters
  */
  public String getDebugStr(Document d, Query q)
  {
    return "Pagerank: " + Integer.toString(d.page_rank);
  }

    /**
     * Score each document for each query.
     * @param d the Document
     * @param q the Query
     * @throws UnsupportedEncodingException
     */
  public abstract double getSimScore(Document d, Query q) throws UnsupportedEncodingException;

  /**
   * Get frequencies for a query.
   * @param q the query to compute frequencies for
   */

  //Doing sublinear scaling for freq

  public Map<String,Double> getQueryFreqs(Query q) {

    // queryWord -> term frequency
    Map<String,Double> tfQuery = new HashMap<String, Double>();

    /*
     * TODO : Your code here
     * Compute the raw term (and/or sublinearly scaled) frequencies
     * Additionally weight each of the terms using the idf value
     * of the term in the query (we use the PA1 corpus to determine
     * how many documents contain the query terms which is stored
     * in this.idfs).
     */

    for(String word : q.queryWords) {
    	//word = clean(word);

		if(tfQuery.containsKey(word)) {
			tfQuery.put(word, tfQuery.get(word) + 1.0);
		} else {
			tfQuery.put(word, 1.0);
		}
    }

    for(String word : tfQuery.keySet()) {
    	double idf_weight = 0.0;

    	if (this.idfs.containsKey(word))
    		idf_weight = this.idfs.get(word);
    	else
    		idf_weight = Math.log(1 + this.idfs.get(LoadHandler.no_word_in_doc_idf_val));

    	tfQuery.put(word, (1 + Math.log(tfQuery.get(word))) * idf_weight);
    }

    return tfQuery;
  }


  /*
   * TODO : Your code here
   * Include any initialization and/or parsing methods
   * that you may want to perform on the Document fields
   * prior to accumulating counts.
   * See the Document class in Document.java to see how
   * the various fields are represented.
   */


  /**
   * Accumulate the various kinds of term frequencies
   * for the fields (url, title, body, header, and anchor).
   * You can override this if you'd like, but it's likely
   * that your concrete classes will share this implementation.
   * @param d the Document
   * @param q the Query
 * @throws UnsupportedEncodingException
   */
  public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) throws UnsupportedEncodingException {

    // Map from tf type -> queryWord -> score
    Map<String,Map<String, Double>> tfs = new HashMap<String,Map<String, Double>>();

    /*
     * TODO : Your code here
     * Initialize any variables needed
     */

    //Isn't this going to be a problem if queries and idfs in general aren't lowercased?
    //So when we build the idf, we have to lowercase it as well right?
    for (String queryWord : q.queryWords) {
      /*
       * Your code here
       * Loop through query terms and accumulate term frequencies.
       * Note: you should do this for each type of term frequencies,
       * i.e. for each of the different fields.
       * Don't forget to lowercase the query word.
       */

    	// Is this sufficient?
    	queryWord = queryWord.toLowerCase();

    	String[] types = {"url","title","body","header","anchor"};

    	for (String type : types) {
    		Map<String, Double> type_map = new HashMap<String, Double>();

    		if (type.equals("url") && d.url != null) {
    			type_map.put(queryWord, (double)num_occurances_url(d.url, queryWord));
    		} else if (type.equals("title") && d.title != null) {
    			type_map.put(queryWord, (double)num_occurances(d.title, queryWord));
    		} else if (type.equals("body") && d.body_hits != null) {
				if (d.body_hits.containsKey(queryWord))
					type_map.put(queryWord, (double) d.body_hits.get(queryWord).size());
    		} else if (type.equalsIgnoreCase("header") && d.headers != null) {
    			int count = 0;

    			for(String header : d.headers)
    				count += num_occurances(header, queryWord);

    			type_map.put(queryWord, (double)count);
    		} else if (type.equals("anchor") && d.anchors != null) {
    			int count = 0;

    			for(String anchor : d.anchors.keySet())
    				count += num_occurances(anchor, queryWord) * d.anchors.get(anchor);

				type_map.put(queryWord, (double)count);
    		}

    		tfs.put(type, type_map);
    	}

    }

    return tfs;
  }

  //how many times does shortStr occur in longStr
  private int num_occurances(String longStr, String shortStr) {
	  longStr = longStr.toLowerCase();

	  String[] words = longStr.split(" ");
	  int count = 0;

	  for(String word : words) {
		  if (word.equals(shortStr)) {
			  count++;
		  }
	  }

	  return count;
  }

  public String[] urlSplit(String url) throws UnsupportedEncodingException {
      String decoded = URLDecoder.decode(url, "UTF-8");
      return decoded.split("[^A-Za-z0-9]+");
  }

  private int num_occurances_url(String url, String shortStr) throws UnsupportedEncodingException {
      url = url.toLowerCase();
      String[] words = urlSplit(url);
      int count = 0;

      for(String word : words) {
          if (word.equals(shortStr)) {
              count++;
          }
      }
      return count;
  }

  //gets rid of all non alphanumeric and makes all white space a single " " and trims off leading/ending whitespace
  public String clean(String str) {
	  return str.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replace("\\s+", " ").trim();
  }
}
