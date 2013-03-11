package de.uni_leipzig.simba.saim;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import de.uni_leipzig.simba.saim.cytoprocess.CytoprocessProperties;
/**
 * @author rspeck
 */
public class SAIMCytoprocessProperties {

	public final static String MEASURE_COLOR = "measureColor";
	public final static String OPERATOR_COLOR = "operatorColor";
	public final static String SOURCE_COLOR = "sourceColor";
	public final static String TARGET_COLOR = "targetColor";
	public final static String OUTPUT_COLOR = "outputColor";

	public final static String OUTPUT_DEFAULT_THRESHOLD_1 = "outputT1";
	public final static String OUTPUT_DEFAULT_THRESHOLD_2 = "outputT2";

	public final static String OPERATOR_DEFAULT_THRESHOLD_1 = "operatorT1";
	public final static String OPERATOR_DEFAULT_THRESHOLD_2 = "operatorT2";

	public static final String resource = "de/uni_leipzig/simba/saim/SAIMCytoprocessProperties.properties";
	private static Properties properties = null;
	static{
		InputStream in=CytoprocessProperties.class.getClassLoader().getResourceAsStream(resource);
		if(in != null){
			properties = new Properties();
			try {
				properties.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	  }

	public static String getProperty(String key){
		return properties.getProperty(key).trim();
	}
}
