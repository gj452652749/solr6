package org.gj.analyzer.filter.charlimited;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class IgnoreCharFilterFactory extends TokenFilterFactory implements ResourceLoaderAware{
	  private final String ignoreCharCase;
	  
	  /** Creates a new StopFilterFactory */
	  public IgnoreCharFilterFactory(Map<String,String> args) {
	    super(args);
	    ignoreCharCase = get(args, "ignoreCharCase");
	    
	    if (!args.isEmpty()) {
	      throw new IllegalArgumentException("Unknown parameters: " + args);
	    }
	  }

	  @Override
	  public void inform(ResourceLoader loader) throws IOException {
		  
	  }
	  @Override
	  public TokenStream create(TokenStream input) {
	      return new IgnoreCharFilter(input,ignoreCharCase);
	  }
}
