package org.gj.analyzer.filter.synonym.data;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class DistributedDicSynonymDataTest {
	String zkHosts="127.0.0.1:2181";
	ZkClient zkClient = new ZkClient(zkHosts,10000,10000,new SerializableSerializer());
	@Test
	public void appendData() {
		String dicPath = "/solr/ik/yxdistrbuted.dic";
		String synPath = "/solr/ik/syn.dic"; 
		zkClient.delete(dicPath);
		zkClient.delete(synPath);
		if (!zkClient.exists(dicPath)) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("cfgCode", "sss1");
			jsonObj.put("data", "[]");
			zkClient.createPersistent(dicPath, jsonObj.toJSONString());
		}
		if (!zkClient.exists(synPath)) {
			zkClient.createPersistent(synPath,"[]");
		}
		Object data=zkClient.readData(synPath);
		JSONArray synDic=JSONArray.parseArray((String) data);
		synDic.add("合哦,合格");
		synDic.add("菲菲,很好");
        zkClient.writeData(synPath, synDic.toJSONString());  
        data=zkClient.readData(dicPath);
        JSONObject jsonObj = JSON.parseObject((String) data);
		JSONArray jsonArray = jsonObj.getJSONArray("data");
		jsonArray.add("合哦");
		jsonArray.add("合格");
		jsonArray.add("菲菲");
		jsonArray.add("很好");
		jsonObj.put("data", jsonArray.toJSONString());
        zkClient.writeData(dicPath, jsonObj.toJSONString());  
	}
	@Test
	public void getNodeData() {
		String path = "/solr/ik/syn.dic";  
		Object data=zkClient.readData(path);
		System.out.println("get data:"+data);
		data=zkClient.readData("/solr/ik/yxdistrbuted.dic");
		System.out.println("get data:"+data);
	}
}
