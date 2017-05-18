package edu.stanford.cs276;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.stanford.cs276.util.Pair;

/**
 * A skeleton for implementing the Smallest Window scorer in Task 3.
 * Note: The class provided in the skeleton code extends BM25Scorer in Task 2. However, you don't necessarily
 * have to use Task 2. (You could also use Task 1, in which case, you'd probably like to extend CosineSimilarityScorer instead.)
 * Also, feel free to modify or add helpers inside this class.
 */
public class SmallestWindowScorer extends CosineSimilarityScorer/*BM25Scorer*/ {
  
  HashSet<String> q_terms = new HashSet<String>();
  double B = 15;
  int windowSize = -1;
  double boostScore = -1;
  
  public SmallestWindowScorer(Map<String, Double> idfs, Map<Query,Map<String, Document>> queryDict) {
    //super(idfs, queryDict);
	  super(idfs);
  }

  /**
   * get smallest window of one document and query pair.
   * @param d: document
   * @param q: query
   */  
  private int getWindow(Document d, Query q) throws UnsupportedEncodingException {
    /*
     * @//TODO : Your code here
     */
	  
	q_terms.clear();
	
	for(String s : q.queryWords)
		q_terms.add(s);

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
	
	windowSize = smallestWindow;
	
    return smallestWindow;
  }

  private int findSmallestWindowURL(String url, Query q) throws UnsupportedEncodingException {
	  url = url.toLowerCase();
	  return findSmallestWindowString(urlSplit(url), q);
  }
  
  private int findSmallestWindowTitle(String title, Query q) {
	return findSmallestWindowString(title.split(" "), q);
  }
  
  private int findSmallestWindowBody(Map<String, List<Integer>> body, Query q) {
	  
    List<Pair<Integer, String>> q_list = new ArrayList<Pair<Integer, String>>();
    
    for(String q_word : body.keySet()) {
    	for(int i : body.get(q_word)) {
        	q_list.add(new Pair<Integer, String>(i, q_word));
    	}
    }

    Collections.sort(q_list, new Comparator<Pair<Integer, String>>(){
    	   @Override
    	   public int compare(final Pair<Integer, String> lhs, Pair<Integer, String> rhs) {
    	     //     return 1 if rhs should be before lhs 
    	     //     return -1 if lhs should be before rhs
    	     //     return 0 otherwise
    		   
    		   return lhs.getFirst() - rhs.getFirst();
    	     }
    	 });
    

    HashMap<String, Integer> freq = new HashMap<String, Integer>();
    int smallestWindow = Integer.MAX_VALUE;
    int start = 0;
    int num_terms = 0;

    for(int i = 0; i < q_list.size(); i++) {
    	Pair<Integer, String> p = q_list.get(i);
    	int index = p.getFirst();
    	String word = p.getSecond();
    	
    	if (!freq.containsKey(word) || freq.get(word) == 0) {
    		freq.put(word, 1);
    		num_terms++;
    	} else {
    		freq.put(word,  freq.get(word) + 1);
    		
    		if (num_terms > q_terms.size()) {
    			//can try to remove some terms from the front
    			
    			while(start < i) {
    				String start_word = q_list.get(start).getSecond();
    				
    				if (freq.get(start_word) > 1) {
    					freq.put(start_word,  freq.get(start_word) - 1);
    					start++;
    					num_terms--;
    					
    					if(smallestWindow > i - start + 1) {
    						smallestWindow = i - start + 1;
    					}
    				} else {
    					break;
    				}
    			}
    		}
    	}
    }
    
    if (num_terms >= q_terms.size() && q_list.size() - start < smallestWindow)
    	smallestWindow = q_list.size() - start;
    
	return smallestWindow;
  }
  
  private int findSmallestWindowString(String[] words, Query q) {
	  HashMap<String, Integer> freq = new HashMap<String, Integer>();
	  
	  int smallestWindow = Integer.MAX_VALUE;
	  
	  int start = 0;
	  
	  int num_terms = 0;
	  
	  for(int i = 0; i < words.length; i++) {
		  String word = words[i];
		  
		  if (!q_terms.contains(word))
			  continue;
		  
		  if(!freq.containsKey(word) || freq.get(word) == 0) {
			  freq.put(word, 1);
			  num_terms++;
		  } else {
			  freq.put(word, freq.get(word) + 1);
			  
			  if(num_terms > q_terms.size()) {
				 //can try to remove some terms from the front
				  
				  while(start < i) {
					  if (freq.get(words[start]) > 1) {
						  freq.put(words[start], freq.get(words[start]) - 1);
						  start++;
						  num_terms--;
						  
						  if (smallestWindow > i - start + 1) {
							  smallestWindow = i - start + 1;
						  }
					  } else {
						  break;
					  }
				  }
			  }
		  }
	  }
	  
	  if (num_terms >= q_terms.size() && words.length - start < smallestWindow)
		  smallestWindow = words.length - start;
	  
	  return smallestWindow;
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
  
  /**
   * get boost score of one document and query pair.
   * @param d: document
   * @param q: query
   */  
  private double getBoostScore (Document d, Query q) throws UnsupportedEncodingException {
    int smallestWindow = getWindow(d, q);
    /*
     * @//TODO : Your code here, calculate the boost score.
     *
     */
    
    //e^-x
    boostScore = (B - 1) * Math.exp(q_terms.size()) * Math.exp(-smallestWindow) + 1;
    
    // 1/x
    //boostScore = 1 + (double)(B - 1) / (double)(smallestWindow - q_terms.size() + 1);
    return boostScore;
  }
  
  @Override
  public String getDebugStr(Document d, Query q)
  {
    String b = "boost score: " + Double.toString(boostScore);
	String w = "window size: " + Integer.toString(windowSize);
	return w + "\n" + b;
    //return "window size: " + Integer.toString(d.page_rank);
  }
  
  @Override
  public double getSimScore(Document d, Query q) throws UnsupportedEncodingException {
    Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
    this.normalizeTFs(tfs, d, q);
    Map<String,Double> tfQuery = getQueryFreqs(q);
    double boost = getBoostScore(d, q);
    double rawScore = this.getNetScore(tfs, q, tfQuery, d);
    return boost * rawScore;
  }

}
