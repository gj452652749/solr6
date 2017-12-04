package org.gj.analyzer.filter.synonym;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class DistributedSynonymFilterFactory extends TokenFilterFactory{
	private final boolean useSynonym;
	private String distributedSynDicPath=null;
	//根据配置文件的参数，初始化相关成员变量
	public DistributedSynonymFilterFactory(Map<String, String> args) {
		super(args);
		useSynonym = getBoolean(args, "useSynonym", false);
		if(useSynonym) 
			distributedSynDicPath=get(args,"distributedSynDicPath");
		System.out.println("get a para:"+useSynonym+"	"+distributedSynDicPath);
		//必須所有的參數都被取出，否则会抛未知参数异常，导致无法生成collection
		if (!args.isEmpty()) {
		      throw new IllegalArgumentException("Unknown parameters: " + args);
		    }
		// TODO Auto-generated constructor stub
	}

	//用来加载相关资源，参考StopFilterFactory
//	@Override
//	public void inform(ResourceLoader loader) throws IOException {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public TokenStream create(TokenStream input) {
		// TODO Auto-generated method stub
		return new DistributedSynonymFilter(input,useSynonym,distributedSynDicPath);
	}

}
