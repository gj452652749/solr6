 package org.gj.analyzer.filter.synonym.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
/**
 * 改为享元模式，延迟载入词库
 * @author gaojun
 *
 */
public class DistributedDicSynonymData{
	private static final Logger logger = LoggerFactory.getLogger(DistributedDicSynonymData.class);
	private static DistributedDicSynonymData instance;
	private ZkClient zkClient;
	//Map<String,String[]> synonymMap=new ConcurrentHashMap<>();
	//key为远程词典路径，value为词典内容
	private Map<String,Map<String,String[]>> synonymDicMap=new ConcurrentHashMap<>();
	private DistributedDicSynonymData() {
		init();
	}
	public static DistributedDicSynonymData getInstance() {
		if(null==instance) {
			synchronized(DistributedDicSynonymData.class) {
				if(null==instance) {
					instance=new DistributedDicSynonymData();
				}
			}
		}
		return instance;
	}
	private void init() {
		Properties config = new Properties();
		try {
			config.load(new BufferedReader(
			        new InputStreamReader(new BufferedInputStream(
			        		getClass().getResourceAsStream("/context.properties")), 
			        		StandardCharsets.UTF_8)));

			String zkHosts=config.getProperty("zk.hosts");
			zkClient = new ZkClient(zkHosts,10000,10000,new SerializableSerializer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//logger.info("同义词本地词库加载成功！"+synonymMap.size());
	}
	private void subscibe(String dicPath) {
		/**
		 * 监听节点数据的变化，子节点数据变化不会监听到
		 */
		zkClient.subscribeDataChanges(dicPath, new IZkDataListener() {
			// 数据变化时触发
			@Override
			public void handleDataChange(String dicPath, Object data) throws Exception {
				logger.info("同义词词库更新 "+dicPath + ":" + data);
				reloadDicData(dicPath, data);
			}

			// 节点删除时触发
			@Override
			public void handleDataDeleted(String dataPath) throws Exception {

			}
		});
	}
	private void reloadDicData(String dicPath,Object data) {
		Map<String,String[]> synonymMap=synonymDicMap.get(dicPath);
		synonymMap.clear();
		String dicDataStr=(String) data;
		String[] lines=dicDataStr.split("\r\n");
		String token;
		String[] words;
		for(int i=0;i<=lines.length-1;i++) {
			token=lines[i];
			words=token.split("\\t");
			for(String word:words) {
				synonymMap.put(word, words);
			}
		}
	}
	private void reloadDic(String dicPath) {
		Map<String,String[]> synonymMap=new HashMap<>();
		synonymDicMap.put(dicPath, synonymMap);
		Object data=zkClient.readData(dicPath);
		reloadDicData(dicPath,data);
		subscibe(dicPath);
	}
	public String[] getSynonyms(String dicPath,String key) throws IOException {
		// TODO Auto-generated method stub
		if(!synonymDicMap.containsKey(dicPath))
			reloadDic(dicPath);
		return synonymDicMap.get(dicPath).get(key);
	}

}
