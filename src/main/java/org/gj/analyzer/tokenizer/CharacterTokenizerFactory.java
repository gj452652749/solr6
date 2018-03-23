package org.gj.analyzer.tokenizer;

import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

public class CharacterTokenizerFactory extends TokenizerFactory {
	public CharacterTokenizerFactory(Map<String, String> args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Tokenizer create(AttributeFactory factory) {
		// TODO Auto-generated method stub
		return new CharacterTokenizer(factory);
	}

}
