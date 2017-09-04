package org.gj.analyzer.filter.synonym;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;

public class PYSynonymAnalyzer extends Analyzer{

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		// TODO Auto-generated method stub
		Tokenizer tokenizer=new StandardTokenizer();
		TokenStream tokenStream=new PYSynonymFilter(tokenizer,true);
		tokenStream=new LowerCaseFilter(tokenStream);
		tokenStream=new StopFilter(tokenStream, StopAnalyzer.ENGLISH_STOP_WORDS_SET);		
		return new TokenStreamComponents(tokenizer, tokenStream);
	}

}
