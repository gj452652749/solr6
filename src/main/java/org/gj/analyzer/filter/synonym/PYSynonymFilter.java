package org.gj.analyzer.filter.synonym;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.gj.analyzer.filter.synonym.data.FileDicSynonymData;
import org.gj.analyzer.util.WordNormalizeUtils;


public class PYSynonymFilter extends TokenFilter{
	private final boolean useSynonym;
	private Stack<String> synonymStack;
	private AttributeSource.State current;
	
	private final CharTermAttribute termAttr;
	private final PositionIncrementAttribute posIncrAttr;

	protected PYSynonymFilter(TokenStream in,boolean useSynonym) {
		super(in);
		this.useSynonym=useSynonym;
		this.synonymStack=new Stack<String>();
		this.termAttr=addAttribute(CharTermAttribute.class);
		this.posIncrAttr=addAttribute(PositionIncrementAttribute.class);
		// TODO Auto-generated constructor stub
	}
    //加上同义词，去掉特殊符号
	@Override
	public boolean incrementToken() throws IOException {
		// TODO Auto-generated method stub

		//如果有同义词
		if(synonymStack.size()>0) {
			String syn=synonymStack.pop();
			//去特殊符号
			syn=WordNormalizeUtils.getInstance().normalizeStr(syn);
			restoreState(current);
			termAttr.copyBuffer(syn.toCharArray(), 0, syn.length());
			posIncrAttr.setPositionIncrement(0);
			return true;
		}
		if(!input.incrementToken())
			return false;
		//如果是非同义词模式，则直接进行字符规范化返回
		if(!useSynonym) {
			String termStr=termAttr.toString();
			termStr=WordNormalizeUtils.getInstance().normalizeStr(termStr);
			termAttr.copyBuffer(termStr.toCharArray(), 0, termStr.length());
			return true;
		}
		//如果有同义词
		if(addAliasesToStack()) {
			current=captureState();
		}
		//如果没有同义词或者本元词，则可调用standTokenizer进行分词，获得子tokenStream
		//去掉特殊符号、全角转半角、繁体转简体
		String termStr=termAttr.toString();
		termStr=WordNormalizeUtils.getInstance().normalizeStr(termStr);
		termAttr.copyBuffer(termStr.toCharArray(), 0, termStr.length());
		return true;
	}
	private boolean addAliasesToStack() throws IOException {
		String termStr=termAttr.toString();
		String[] synonyms=null;
		synonyms=FileDicSynonymData.getInstance().getSynonyms(termStr);
		//System.out.println(termAttr.toString());
		/*
		if((termAttr.toString()).equals("佰")) {
			synonyms=new String[]{"中华人民共和国","天朝","ch-ina"};
		}
		*/
		if (synonyms == null) {  
            return false;  
        }  
        for (String synonym : synonyms) {
        	if(synonym.equals(termStr))
        		continue;
            synonymStack.push(synonym);  
        }  
        return true; 
	}



}
