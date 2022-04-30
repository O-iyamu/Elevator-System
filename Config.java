/**
 * 
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Ryan
 *
 */
public class Config{
	
	private static final String CONFIG_FILE = "config.properties";
	
	private final Properties prop;
	
	public Config() throws IOException{
		InputStream input = new FileInputStream("config.properties");
		
        prop = new Properties();
        prop.load(input);
    }
	
	public String getStrProperty(String key){
        return this.prop.getProperty(key);
    }
	
	public int getIntProperty(String key){
        return Integer.parseInt(getStrProperty(key));
    }
	
	public float getFloatProperty(String key){
        return Float.parseFloat(getStrProperty(key));
    }

}
