package com.aohu.iface;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * properties 读取配置文件的工具类
 */

public class PropertyUtil
{
	public String path;
	private static Properties prop;

	public PropertyUtil(String path)
	{
		this.path = path;
		load();
	}
	
	public void load()
	{
		prop = new Properties();// 属性集合对象 
		InputStream inputStream = null;
		
		try { 
			inputStream = new BufferedInputStream(new FileInputStream(this.path));
			prop.load(inputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
	}
	
	 public String getProperty(String key)
	 {  
		InputStream inputStream = null;  
	    try {  
	    	inputStream = new BufferedInputStream(new FileInputStream(this.path));
	        prop.load(inputStream);// 将属性文件流装载到Properties对象中   
	        inputStream.close();// 关闭流  
	    } catch (FileNotFoundException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
	    return prop.getProperty(key);  
	 }
	
	//参数为要修改的文件路径  以及要修改的属性名和属性值 
	public boolean update(String key, String value)
	{
		prop.setProperty(key, value);
		try {  
            FileOutputStream fos = new FileOutputStream(this.path);   
            // 将Properties集合保存到流中   
            prop.store(fos, "");   
            fos.close();// 关闭流   
        } catch (FileNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            return false;  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            return false;  
        }
        return true;  
	}
}
