package org.gj.analyzer.filter.synonym.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDicSynonymData implements SynonymData{
	private static final Logger logger = LoggerFactory.getLogger(FileDicSynonymData.class);
	private static FileDicSynonymData instance;
	Map<String,String[]> synonymMap=new HashMap<>();
	private FileDicSynonymData() {
		init();
	}
	public static FileDicSynonymData getInstance() {
		if(null==instance) {
			synchronized(FileDicSynonymData.class) {
				if(null==instance) {
					instance=new FileDicSynonymData();
				}
			}
		}
		return instance;
	}
	public void init() {
		reloadDic();
		logger.info("同义词本地词库加载成功！"+synonymMap.size());
	}
	@Override
	public void reloadDic() {
		synonymMap.clear();
		BufferedReader in = new BufferedReader(
                new InputStreamReader(new BufferedInputStream(
                		getClass().getResourceAsStream("/synonym.dic")), 
                		StandardCharsets.UTF_8));
		String line;
		String[] tokens;
		try {
			while((line=in.readLine())!=null) {
				tokens=line.split("\\t");
				for(String token:tokens) {
					synonymMap.put(token, tokens);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public String[] getSynonyms(String key) throws IOException {
		// TODO Auto-generated method stub
		return synonymMap.get(key);
	}

}
