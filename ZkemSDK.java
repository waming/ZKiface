package com.aohu.iface;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class ZkemSDK {
	
	private final static Log logger = LogFactory.getLog(ZkemSDK.class);
	
	//静态加载zkemkeeper.dll,  zkemkeeper.ZKEM为注册表中的ProgID数值
	private static ActiveXComponent zkem = new ActiveXComponent("zkemkeeper.ZKEM");
	
	//考勤机的IP
	//private static String KQ_IP = "192.168.0.32";
	
	/**
	 * 连接考勤机
	 * @param address 考勤机地址
	 * @param port 端口号
	 * @return 
	 */
	public boolean connect(String address, int port)
	{
		boolean result = zkem.invoke("Connect_NET", address, port).getBoolean();
		return result;
	}
	
	/**
	 * 读取考勤记录到 PC 的内部缓冲区，同 ReadAllGLogData 
	 * @return boolean
	*/
	public boolean readGeneralLogData()
	{
		boolean result = zkem.invoke("ReadGeneralLogData", 100).getBoolean();
		return result;
	}
	
	/**
	 * 获取缓存中的考勤数据。配合readGeneralLogData / readLastestLogData使用。
	 * @return 返回的map中，包含以下键值：
	 	"EnrollNumber"   人员编号
		"Time"           考勤时间串，格式: yyyy-MM-dd HH:mm:ss
		"VerifyMode"
		"InOutMode"
		"Year"          考勤时间：年
		"Month"         考勤时间：月
		"Day"           考勤时间：日
		"Hour"			考勤时间：时
		"Minute"		考勤时间：分
		"Second"		考勤时间：秒
	*/
	public List<Map<String,Object>> getGeneralLogData()
	{
		Variant v0 			   = new Variant(1);
		Variant dwEnrollNumber = new Variant("",true);
		Variant dwVerifyMode   = new Variant(0,true);
		Variant dwInOutMode    = new Variant(0,true);
		Variant dwYear  	   = new Variant(0,true);
		Variant dwMonth 	   = new Variant(0,true);
		Variant dwDay   	   = new Variant(0,true);
		Variant dwHour         = new Variant(0,true);
		Variant dwMinute       = new Variant(0,true);
		Variant dwSecond       = new Variant(0,true);
		Variant dwWorkCode     = new Variant(0,true);
		List<Map<String,Object>> strList = new ArrayList<Map<String,Object>>();
		
		if (this.readGeneralLogData() == false) {
			return strList;
		}
		
		boolean newresult = false;
		do{
			Variant vResult = Dispatch.call(zkem, "SSR_GetGeneralLogData", v0,dwEnrollNumber,dwVerifyMode,dwInOutMode,dwYear,dwMonth,dwDay,dwHour,
					dwMinute,dwSecond,dwWorkCode);	
			newresult = vResult.getBoolean();
			if(newresult)
			{
				String enrollNumber = dwEnrollNumber.getStringRef();
				
				//如果没有编号，则跳过。
				if(enrollNumber == null || enrollNumber.trim().length() == 0)
					continue;
				int year     = dwYear.getIntRef();
				int month    = dwMonth.getIntRef();
				int day	     = dwDay.getIntRef();
				Calendar now = Calendar.getInstance();
				int nowyear  = now.get(Calendar.YEAR);
				int nowday   = now.get(Calendar.DAY_OF_MONTH);
				int nowmonth = now.get(Calendar.MONTH) + 1;
				
				/* 只记录今天的考勤数据 */
				if (year < nowyear || month < nowmonth || day < nowday) {
					continue;
				}
				
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("EnrollNumber", enrollNumber);
				m.put("Time", dwYear.getIntRef() + "-" + dwMonth.getIntRef() + "-" + day + " " + dwHour.getIntRef() + ":" + dwMinute.getIntRef() + ":" + dwSecond.getIntRef());
				m.put("VerifyMode", dwVerifyMode.getIntRef());
				m.put("InOutMode", dwInOutMode.getIntRef());
				m.put("Year", year);
				m.put("Month", month);
				m.put("Day", day);
				m.put("Hour", dwHour.getIntRef());
				m.put("Minute", dwMinute.getIntRef());
				m.put("Second", dwSecond.getIntRef());
				strList.add(m);
			}
		}while(newresult == true);
		return strList;
	}
	
	public static void main(String[] args) throws Exception
	{	
		Properties properties = new Properties();
		InputStream inStream  = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "/config/config.properties"));
		properties.load(inStream);
		
		ZkemSDK sdk       = new ZkemSDK();
		boolean  connFlag = sdk.connect(properties.getProperty("ip"), Integer.parseInt(properties.getProperty("port")));
		if (false == connFlag) {
			logger.info("连接考勤机失败，请检查！！");
		}
		
		List<Map<String, Object>> kqdata = sdk.getGeneralLogData();
		if (kqdata.isEmpty()) {
			logger.info("暂无考勤数据！！");
		} else {
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("content", kqdata);
			//发送接口数据
			
			String urlString      = properties.getProperty("api_url");
			HttpUtil httpUtil     = new HttpUtil();
			JSONObject testObject = httpUtil.postJson(urlString, JSON.toJSONString(params));
			logger.info(testObject);
		}
	}
}