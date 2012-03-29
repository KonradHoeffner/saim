package de.uni_leipzig.simba.saim.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.konrad.commons.sparql.PrefixHelper;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.io.KBInfo;

/**Class holds all configuration settings for a linking process. */
public class Configuration {	
	private static Configuration instance = new Configuration();	
	private PropertyChangeSupport changes = new PropertyChangeSupport( this ); 
	public static final String SETTING_CONFIG = "setting from xml";
	protected String id = null;
	protected String name;

	protected double acceptanceThreshold;
	protected double verificationThreshold;
	protected int granularity;

	private ConfigReader cR = new ConfigReader();

	protected KBInfo source = null;
	protected KBInfo target = null;
	protected String metricExpression;
	public String getMetricExpression() {
		return metricExpression;
	}
	
	public void setMetricExpression(String metricExpression) {
		this.metricExpression = metricExpression;
	}

	public double getAcceptanceThreshold() {
		return acceptanceThreshold;
	}

	public void setAcceptanceThreshold(double acceptanceThreshold) {
		this.acceptanceThreshold = acceptanceThreshold;
	}

	private Configuration() {
		
	}
	
	public void store() {
		
	}
	
	/**
	 * Implements Singleton pattern.
	 * @return
	 */
	public static Configuration getInstance() {	
		return instance;
	}
	
	public void setSourceEndpoint(KBInfo source) {
		this.source = source;
	}
	public void setTargetEndpoint(KBInfo target) {
		this.target = target;
	}
	public KBInfo getSource() {
		return source;
	}
	public KBInfo getTarget() {
		return target;
	}
	
	public void setFromConfigReader(ConfigReader cR) {
		this.cR = cR;
		source = cR.sourceInfo;
		target = cR.targetInfo;
		metricExpression = cR.metricExpression;
		acceptanceThreshold = cR.acceptanceThreshold;
		verificationThreshold = cR.verificationThreshold;
		granularity = cR.granularity;		
		changes.firePropertyChange(SETTING_CONFIG, null, this);
	}
	
	public void addPropertyChangeListener( PropertyChangeListener l ) 
	  { 
	    changes.addPropertyChangeListener( l ); 
	  } 
	 
	  public void removePropertyChangeListener( PropertyChangeListener l ) 
	  { 
	    changes.removePropertyChangeListener( l ); 
	  }
	  
	  /**Set default namespaces in both source and target KBInfo  */
	  public void setDefaultNameSpaces() {
		  source.prefixes = getDefaultNameSpaces();
		  target.prefixes = getDefaultNameSpaces();
	  }
	  
	  /**
	   * Function returns HashMap of label and uri of often used namespaces.
	   * @return HashMap<label,uri>
	   */
	  public HashMap<String, String> getDefaultNameSpaces() {
		  HashMap<String, String> defs = new HashMap<String, String>();
//		  defs.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
//		  defs.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
//		  defs.put("foaf", "http://xmlns.com/foaf/0.1/");
//		  defs.put("owl", "http://www.w3.org/2002/07/owl#");
//		  defs.put("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
//		  defs.put("dbpedia", "http://dbpedia.org/ontology/");
//		  defs.put("dbpedia-p", "http://dbpedia.org/property/");
//		  defs.put("dc", "http://purl.org/dc/terms/");
//		  defs.put("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
//		  defs.put("drugbank", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
//		  defs.put("dailymed", "http://www4.wiwiss.fu-berlin.de/dailymed/resource/dailymed/");
		  Map<String, String> map = PrefixHelper.getPrefixes();	
		  for(Entry<String, String> e : map.entrySet())
			  defs.put(e.getKey(), e.getValue());
		  return defs;
	  }
	  
	  public String toString() {
		return source.toString()+"\n<br>\n"+target.toString();  
	  }
	  
	  public ConfigReader getLimesConfiReader() {
		  cR.sourceInfo = source;
		  cR.targetInfo = target;
		  cR.acceptanceRelation = metricExpression;
		  cR.acceptanceThreshold = acceptanceThreshold;
		  cR.verificationThreshold  = verificationThreshold;
		  cR.granularity = granularity;		 
		  return cR;
	  }
	  
}