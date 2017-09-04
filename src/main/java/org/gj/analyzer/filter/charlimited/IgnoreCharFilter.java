package org.gj.analyzer.filter.charlimited;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.FilteringTokenFilter;

public class IgnoreCharFilter extends FilteringTokenFilter{
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  private final String ignoreCharCase;
	  /**
	   * Constructs a filter which removes words from the input TokenStream that are
	   * named in the Set.
	   * 
	   * @param in
	   *          Input stream
	   * @param stopWords
	   *          A {@link CharArraySet} representing the stopwords.
	   * @see #makeStopSet(java.lang.String...)
	   */
	  public IgnoreCharFilter(TokenStream in,String ignoreCharCase) {
	    super(in);
	    this.ignoreCharCase=ignoreCharCase;
	  }
	  
	  /**
	   * Returns the next input Token whose term() is not a stop word.
	   */
	  @Override
	  protected boolean accept() {
	    return !(termAtt.length()<2);
	  }
}
