package de.uni_leipzig.simba.saim.gui.widget.step;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.vaadin.teemu.wizards.WizardStep;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

import de.konrad.commons.sparql.PrefixHelper;
import de.konrad.commons.sparql.SPARQLHelper;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.core.Configuration;
import de.uni_leipzig.simba.saim.gui.widget.panel.ClassMatchingPanel;

public class ClassMatchingStep implements WizardStep
{
	SAIMApplication app;
	public ClassMatchingStep(SAIMApplication app) {
		this.app = app;
	}
	ClassMatchingPanel panel;
	@Override
	public String getCaption() {return Messages.getString("classmatching");}

	@Override
	public Component getContent()
	{
		if(!app.getConfig().isLocal) {
			return  panel = new ClassMatchingPanel();
		} else {
			return new Panel("No class Matching required.");
		}
			
	}

	@Override
	public boolean onAdvance()
	{
		Configuration config = app.getConfig();
		KBInfo source = config.getSource();
		KBInfo target = config.getTarget();
		if( config.isLocal )
			return true;
		else {
			if(panel.sourceClassForm.isValid() && panel.targetClassForm.isValid())
			{
				
				source.restrictions.clear();
				target.restrictions.clear();
				//source
				source.prefixes.put("rdf", PrefixHelper.getURI("rdf"));
				String restr = source.var+" rdf:type ";		
				String value1 = SPARQLHelper.wrapIfNecessary(PrefixHelper.expand(panel.sourceClassForm.getField("textfield").getValue().toString()));
				if(value1 != null && value1.length()>0)
					source.restrictions.add(restr + value1);
				//target
				restr = target.var+" rdf:type ";
				String value2 = SPARQLHelper.wrapIfNecessary(PrefixHelper.expand(panel.targetClassForm.getField("textfield").getValue().toString()));
				if(value2 != null && value2.length()>0)
					target.restrictions.add(restr + value2);
				target.prefixes.put("rdf", PrefixHelper.getURI("rdf"));
				if(value1 != null && value2 !=  null && value1.length()>0 && value2.length()>0) {
					System.out.println("Added class restrictions:\nSource: "+source.restrictions.get(0)+" \nTarget: "+target.restrictions.get(0));
					
	//				// we may add the selected result to the cache
	//				if(ClassMatchingPanel.CACHING) {
	//					Cache cache = CacheManager.getInstance().getCache("classmatching");
	//					List<Object> parameters = Arrays.asList(new Object[] {config.getSource().endpoint,config.getTarget().endpoint,config.getSource().id,config.getTarget().id});
	//				try {
	//						Logger.getLogger("SAIM").info("Trying to add selected values to classMapping cache.");
	//						if(cache.isKeyInCache(parameters)) {	
	//							HashMap<String,HashMap<String,Double>> map;
	//							map = ((HashMap<String,HashMap<String,Double>>) cache.get(parameters).getValue());
	//							if(map == null) {
	//								map = new HashMap<String,HashMap<String,Double>>();
	//							}
	//							HashMap<String, Double> targetMap = map.remove(panel.sourceClassForm.getField("textfield").getValue().toString());
	//							if(targetMap == null) 
	//								targetMap = new HashMap<String, Double>();
	//							if(!targetMap.containsKey(panel.targetClassForm.getField("textfield").getValue().toString()))
	//						    	targetMap.put(panel.targetClassForm.getField("textfield").getValue().toString(), 1.0d);
	//							map.put(panel.sourceClassForm.getField("textfield").getValue().toString(), targetMap);
	//							Logger.getLogger("SAIM").info("map is now "+map);
	//							cache.remove(parameters);
	//							cache.put(new Element(parameters, map));
	//							cache.flush();
	//							cache =null;
	//						}
	//					}catch(Exception e){
	//						Logger.getLogger("SAIM").info("Exception addding selected elements to classMatching cache: "+e.getMessage());
	//					}
	//				}				
					panel.close();			
					return true;
				}
			}	
		}//if sparql endpoints
		return false;
	}

	@Override
	public boolean onBack()
	{
		panel.close();
		return true;
	}

}