package com.aohu.iface;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpUtil {
	
	public JSONObject postJson(String url, String params) throws Exception{
		String result = "参数错误，请重试";
		if(url.isEmpty() || params.isEmpty()) {
			return JSON.parseObject(result);
		}
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost              = new HttpPost(url);	
		CloseableHttpResponse response = null;
		httpPost.setHeader("Accept", "application/json"); 
    	httpPost.setHeader("Content-Type", "application/json");
    	String charSet = "UTF-8";
    	String httpStr = null;
    	
    	try {
    		String paramString         = params;
	    	StringEntity stringEntity  = new StringEntity(paramString, charSet);
	    	stringEntity.setContentEncoding(charSet);
	    	stringEntity.setContentType("application/json");
	    	httpPost.setEntity(stringEntity);
	    	
	    	response = httpClient.execute(httpPost);
	    	HttpEntity entity = response.getEntity();
	    	httpStr  = EntityUtils.toString(entity, charSet);
    	} catch (Exception e) {
			// TODO: handle exception
    		System.out.println(e.getMessage());
		}
    	
    	return JSON.parseObject(httpStr);		
	}
}