package com.utilities;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

	String browser="";
	String mode="";
	public Properties prop;
	public String proppath= "./src/test/resources/data/config.properties";

	public Properties loadConfig()throws Throwable {

		try {
			FileInputStream ip= new FileInputStream(proppath);

			prop=new Properties();
			try {
				prop.load(ip);
			} catch (IOException e) {

				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("config.properties not found at " + proppath);
		}
		return prop;
	}


	public  String getBrowser() {
		return prop.getProperty("browser");
	}
	
	public  String getBrowserMode() {
		return prop.getProperty("mode");
	}

}