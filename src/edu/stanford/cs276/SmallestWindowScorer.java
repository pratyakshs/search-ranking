package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 * Also, feel free to modify or add helpers inside this class.
 */
public class SmallestWindowScorer extends BM25Scorer {
  
  HashSet<String> q_unique_terms = new HashSet<String>();
	
  public SmallestWindowScorer(Map<String, Double> idfs, Map<Query,Map<String, Document>> queryDict) {
    super(idfs, queryDict);
  }

  /**
   * get smallest window of one document and query pair.
   * @param d: document
   * @param q: query
   */  
  private int getWindow(Document d, Query q) {
    /*
     * @//TODO : Your code here
     */
	  
	q_unique_terms.clear();
	
	for(String s : q.queryWords)
		q_unique_terms.add(s);

	int smallestWindow = Integer.MAX_VALUE;
	
	String[] types = {"url","title","body","header","anchor"};
	
	for (String type : types) {
		
		if (type.equals("url") && d.url != null) {
			int newSmallestWindow = findSmallestWindowURL(d.url, q);
			if (newSmallestWindow < smallestWindow)
				smallestWindow = newSmallestWindow;
		} else if (type.equals("title") && d.title != null) {
			int newSmallestWindow = findSmallestWindowTitle(d.title, q);
			if (newSmallestWindow < smallestWindow)
				smallestWindow = newSmallestWindow;
		} else if (type.equals("body") && d.body_hits != null) {
			int newSmallestWindow = findSmallestWindowBody(d.body_hits, q);
			if (newSmallestWindow < smallestWindow)
				smallestWindow = newSmallestWindow;
		} else if (type.equalsIgnoreCase("header") && d.headers != null) {
			int newSmallestWindow = findSmallestWindowHeaders(d.headers, q);
			if (newSmallestWindow < smallestWindow)
				smallestWindow = newSmallestWindow;
		} else if (type.equals("anchor") && d.anchors != null) {
			int newSmallestWindow = findSmallestWindowAnchors(d.anchors, q);
			if (newSmallestWindow < smallestWindow)
				smallestWindow = newSmallestWindow;
		}
	}
	
    return smallestWindow;
  }

  private int findSmallestWindowURL(String url, Query q) {
	  url = url.toLowerCase();
	  return findSmallestWindowString(url.split("[^A-Za-z0-9 ]"), q);
  }
  
  private int findSmallestWindowTitle(String title, Query q) {
	return findSmallestWindowString(title.split(" "), q);
  }
  
  private int findSmallestWindowBody(Map<String, List<Integer>> body, Query q) {
	return -1;
  }
  
  private int findSmallestWindowHeaders(List<String> headers, Query q) {
	int smallest = Integer.MAX_VALUE;
	
	for(String str : headers) {
		int small = findSmallestWindowString(str.split(" "), q);
		if (small < smallest)
			smallest = small;
	}	  
	  
	return smallest;
  }
  
  private int findSmallestWindowAnchors(Map<String, Integer> anchors, Query q) {
	int smallest = Integer.MAX_VALUE;
	
	for(String str : anchors.keySet()) {
		int small = findSmallestWindowString(str.split(" "), q);
		if (small < smallest)
			smallest = small;
	}	  
	  
	return smallest;
  }
  
  private int findSmallestWindowString(String[] words, Query q) {
	  return -1;
  }
  
  /**
   * get boost score of one document and query pair.
   * @param d: document
   * @param q: query
   */  
  private double getBoostScore (Document d, Query q) {
    int smallestWindow = getWindow(d, q);
    double B = 5;
    /*
     * @//TODO : Your code here, calculate the boost score.
     *
     */
    
    return 1 + (double)(B - 1) / (double)(smallestWindow - q_unique_terms.size() + 1);
  }
  
  @Override
  public double getSimScore(Document d, Query q) {
    Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
    this.normalizeTFs(tfs, d, q);
    Map<String,Double> tfQuery = getQueryFreqs(q);
    double boost = getBoostScore(d, q);
    double rawScore = this.getNetScore(tfs, q, tfQuery, d);
    return boost * rawScore;
  }

}
