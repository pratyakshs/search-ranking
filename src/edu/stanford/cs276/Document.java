package edu.stanford.cs276;

import java.util.List;
import java.util.Map;

/**
 * The class is used to store useful information for a document. 
 * You can also write the document to a string for debugging.
 */
public class Document {

  public String url = null;
  public String title = null;
  public List<String> headers = null;

  // term -> [list of positions]
  public Map<String, List<Integer>> body_hits = null; 
  public int body_length = 0;
  public int page_rank = 0;

  // anchor text -> anchor count
  // The anchor text could contain multiple words separated
  // by whitespace. You may want to perform some tokenization.
  public Map<String, Integer> anchors = null; 

  // debug string for you to debug your implementation
  public String debugStr = "";

  /** 
   * Constructs a document with a String url.
   * @param url the url associated with a document.
   */
  public Document(String url) {
    this.url=url;
  }
  
  @Override
  public int hashCode() {
	  return this.toString().hashCode();
  }
  
  @Override
  public boolean equals(Object d) {
	  return this.hashCode() == d.hashCode();
  }
  
  /**
   * Returns a String representation of a Document.
   * @return the String of fields representing a Document
   */
  public String toString() {
    StringBuilder result = new StringBuilder();
    String NEW_LINE = System.getProperty("line.separator");
    result.append("url: "+ url + NEW_LINE);
    if (title != null) result.append("title: " + title + NEW_LINE);
    if (headers != null) result.append("headers: " + headers.toString() + NEW_LINE);
    if (body_hits != null) result.append("body_hits: " + body_hits.toString() + NEW_LINE);
    if (body_length != 0) result.append("body_length: " + body_length + NEW_LINE);
    if (page_rank != 0) result.append("page_rank: " + page_rank + NEW_LINE);
    if (anchors != null) result.append("anchors: " + anchors.toString() + NEW_LINE);
    return result.toString();
  }
}
