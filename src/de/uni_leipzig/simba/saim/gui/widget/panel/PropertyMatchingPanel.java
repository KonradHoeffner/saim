package de.uni_leipzig.simba.saim.gui.widget.panel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import de.konrad.commons.sparql.PrefixHelper;
import de.konrad.commons.sparql.SPARQLHelper;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.learning.query.PropertyMapper;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.core.Configuration;
import de.uni_leipzig.simba.saim.gui.widget.PropertyComboBox;
import de.uni_leipzig.simba.saim.gui.widget.panel.MetricPanel.AccordionLayoutClickListener;

/** Contains instances of ClassMatchingForm and lays them out vertically.*/
@SuppressWarnings("serial")
public class PropertyMatchingPanel extends Panel
{		
	private final Messages messages;
	private static final boolean CACHING = true;
	private final Layout mainLayout;
	private static final Logger logger = LoggerFactory.getLogger(PropertyMatchingPanel.class);
	private List<Object[]> rows = new Vector<Object[]>();
	private ClassResource closeImageResource;
	private Table table = new Table();
	private List<String> sourceProperties;
	private List<String> targetProperties;	
	private final ProgressIndicator progress = new ProgressIndicator();
	private Label progressLabel;
	private boolean listenerActive = true;
	// to display computed ones
	HorizontalLayout selectionLayout = new HorizontalLayout();
	ListSelect select = new ListSelect("Computed Property Mappings");
	Button useAll  = new Button("Use them all");
	
	
	public PropertyMatchingPanel(final Messages messages)
	{
		this.messages=messages;
		progressLabel = new Label(messages.getString("generatingpropertymatching"));	
		mainLayout = new VerticalLayout();
		
		selectionLayout.addComponent(select);
		selectionLayout.addComponent(useAll);
		useAll.setEnabled(false);
		mainLayout.addComponent(selectionLayout);
		
		setContent(mainLayout);
		getContent().setWidth("100%");
		/* Create the table with a caption. */

		//	setupContextHelp();
		Layout progressLayout = new HorizontalLayout();
		mainLayout.addComponent(progressLayout);
		progress.setIndeterminate(true);
		progressLayout.addComponent(progressLabel);
		progressLayout.addComponent(progress);
		propMapper.start();
	}

	Cache cache = null;

	Thread propMapper = new Thread()
	{
		@Override
		public void run()
		{
			progress.setVisible(true);
			progressLabel.setVisible(true);
			Map<String,HashMap<String,Double>> map = performAutomaticPropertyMapping().map;
			//			try{Thread.sleep(4000);} catch (InterruptedException e) {e.printStackTrace();}
			//			Map<String,HashMap<String,Double>> map = mockPropertyMap();
			displayPropertyMapping(map);
			progress.setVisible(false);
			progressLabel.setVisible(false);
		}
		
		private Map<String, HashMap<String, Double>> mockPropertyMap()
		{
			Map<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
			HashMap<String,Double> value = new HashMap<String,Double>();
			value.put("rdfs:label",0.337);
			map.put("rdfs:label",value);
			return map;
		}
	};

	private Object columnValue(Object o)
	{
		return ((PropertyComboBox)o).getValue();
	}

	public boolean isValid() // empty and full pairs ok, half full ones are not
	{		
		for(Object[] row: rows)
		{if(columnValue(row[0])==null^columnValue(row[1])==null) {return false;}}
		return true;
	}

	//	protected void setupContextHelp()
	//	{
	//		ContextHelp contextHelp = new ContextHelp();
	//		getContent().addComponent(contextHelp);
	//	}

	private String classRestrictionToClass(String classRestriction)
	{
		return classRestriction.substring((classRestriction.lastIndexOf(' ')+1)).replace("<","").replace(">","");
	}

	/**	returns all properties (not just the ones from the property matching) that instances of the knowledge base of the 
	 * class restriction specified in the KBInfo have. <b>May break if the class restriction is not set.</b>*/
	private List<String> allPropertiesFromKBInfo(KBInfo kb)
	{
		return SPARQLHelper.properties(
				kb.endpoint,
				kb.graph,
				classRestrictionToClass(kb.getClassRestriction()));
	}

	private List<String> mockAllPropertiesFromKBInfo(KBInfo kb)
	{
		return Arrays.asList(new String[] {"rdfs:label","rdfs:schmabel"});
	}

	private class RowChangeListener implements ValueChangeListener
	{
		final Object[] row;

		public RowChangeListener(Object[] source)
		{			
			if(source.length!=3) throw new IllegalArgumentException();
			this.row=source;
		}

		@Override
		public void valueChange(ValueChangeEvent event)
		{
			// TODO: find a better solution that is not succeptible to rare possible timing problems
			// (user may insert at the same time as insertion from displayPropertyMapping() takes place)
			if(!listenerActive) {return;}
			if(row==rows.get(rows.size()-1)) // complete last row -> create new
			{
				if(!(((PropertyComboBox)row[0]).getValue()==null||((PropertyComboBox)row[1]).getValue()==null))
				{
					Object[] row = createTableRow();
					table.addItem(row,row);
				}
			}
			else if(columnValue(row[0])==null&&columnValue(row[1])==null) // remove empty rows at non-last position 
			{
				removeRow(row);
			}
		}
	}

	private Object[] createTableRow()
	{
		PropertyComboBox sourceBox = new PropertyComboBox(sourceProperties);		
		PropertyComboBox targetBox = new PropertyComboBox(targetProperties);
		//Embedded closeImage = new Embedded("",closeImageResource);
		//		CSSInject css = new CSSInject();
		//		css.setValue(".center {margin-left:auto;margin-right:auto;}");
		//		this.getContent().addComponent(css);
		// TODO:  make the button smaller but keep the cross in the middle 
		Button closeRowButton = new Button();
		//		closeRowButton.setWidth("24px");
		closeRowButton.setIcon(closeImageResource);
		//		closeRowButton.setStyleName("center");

		final Object[] row = {sourceBox,targetBox,closeRowButton};
		sourceBox.addListener(new RowChangeListener(row));
		targetBox.addListener(new RowChangeListener(row));
		closeRowButton.addListener(new ClickListener()
		{			
			@Override
			public void buttonClick(ClickEvent event)
			{
				if(rows.size()>1) {removeRow(row);}
			}
		});
		rows.add(row);
		return row;
	}

	private void removeRow(Object[] row)
	{
		rows.remove(row);
		if(!table.removeItem(row));		
	}

	@Override
	public void attach()
	{
		super.attach();
		getAllProperties();
		//		List<String> sourcePropertiesFull = new LinkedList<String>();
		//		List<String> targetPropertiesFull = new LinkedList<String>();
		//		sourceProperties = new LinkedList<String>();
		//		targetProperties = new LinkedList<String>();
		//		if(Configuration.getInstance().isLocal) {
		//			for(String prop : Configuration.getInstance().getSource().properties) {
		//				sourcePropertiesFull.add(prop);
		//			}
		//			
		//			for(String prop : Configuration.getInstance().getTarget().properties) {
		//				targetPropertiesFull.add(prop);
		//			}
		//		} else {
		//			sourcePropertiesFull = allPropertiesFromKBInfo(source);
		//			targetPropertiesFull = allPropertiesFromKBInfo(target);
		//		}		
		//		// abbreviate
		//		for(String prop : sourcePropertiesFull) {
		//			String s_abr=PrefixHelper.abbreviate(prop);
		//			sourceProperties.add(s_abr);
		//		}
		//		
		//		for(String prop : targetPropertiesFull) {
		//			String s_abr=PrefixHelper.abbreviate(prop);
		//			targetProperties.add(s_abr);
		//		}
		table.setWidth("100%");
		addComponent(table);		
		closeImageResource = new ClassResource("img/no_crystal_clear_16.png",getApplication());		
		/* Define the names and data types of columns.
		 * The "default value" parameter is meaningless here. */		
		table.addContainerProperty(messages.getString("sourceproperty"), PropertyComboBox.class,  null);
		table.addContainerProperty(messages.getString("targetproperty"), PropertyComboBox.class,  null);
		table.addContainerProperty("", Button.class,  null);
		//table.setColumnWidth("",48);

		/* Add a few items in the table. */

		//		Button closeButton = new Button();
		//		closeButton.setWidth("16px");
		//		closeButton.setHeight("16px");		
		//		closeButton.setIcon(resource);
		Object[] row = createTableRow();
		table.addItem(row,row);
		//addComponent(new PropertyComboBox(mockAllPropertiesFromKBInfo(null)));

	}

	//	private class PropertyPairPanel extends Panel
	//	{
	//		public PropertyPairPanel()
	//		{
	//			setContent(new HorizontalLayout());
	//			ComboBox sourcePropertyComboBox = new ComboBox();
	//			sourcePropertyComboBox.setWidth("50%");
	//			ComboBox targetPropertyComboBox = new ComboBox();
	//			targetPropertyComboBox.setWidth("50%");
	//			addComponent(sourcePropertyComboBox);
	//			addComponent(targetPropertyComboBox);
	//		}
	//	}

	/**
	 * Method to add Properties to according KBInfo. 
	 * @param s URI of the property. May or may not be abbreviated.
	 * @param info KBInfo of endpoint property belongs to.
	 */
	private void addProperty(String s, KBInfo info) {
		String prop;
		System.out.println("Add property "+s+" to "+info.id);
		if(s.startsWith("http:")) {//do not have a prefix, so we generate one
			PrefixHelper.generatePrefix(s);
			prop = PrefixHelper.abbreviate(s);
		} else {// have the prefix already
			prop = s;
			s = PrefixHelper.expand(s);
		}
		if(!info.properties.contains(prop))
			info.properties.add(prop);
		info.functions.put(prop,"nolang->lowercase");
		String base = PrefixHelper.getBase(s);
		info.prefixes.put(PrefixHelper.getPrefix(base), PrefixHelper.getURI(PrefixHelper.getPrefix(base)));

		LoggerFactory.getLogger(AccordionLayoutClickListener.class).info(info.var+": adding property: "+prop+" with prefix "+PrefixHelper.getPrefix(base)+" - "+PrefixHelper.getURI(PrefixHelper.getPrefix(base)));
	}

	private void getAllProperties() {
		sourceProperties = new LinkedList<String>();
		targetProperties = new LinkedList<String>();
		Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
		if(config.isLocal) {
			logger.info("Local data - using specified properties");
			for(String prop : config.getSource().properties) {
				String s_abr=PrefixHelper.abbreviate(prop);
				sourceProperties.add(s_abr);
			}

			for(String prop : config.getTarget().properties) {
				String s_abr=PrefixHelper.abbreviate(prop);
				targetProperties.add(s_abr);
			}
			return;
		}
		List<String> propListSource = null;
		List<String> propListTarget = null;
		KBInfo info = config.getSource();
		String className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
		if(CACHING) {
			cache = CacheManager.getInstance().getCache("propertymapping");
			//			if(cache.getStatus()==net.sf.ehcache.Status.STATUS_UNINITIALISED) {cache.initialise();}					
			List<Object> parameters = Arrays.asList(new Object[] {info.endpoint, info.graph, className});
			try{
				if(cache.isKeyInCache(parameters))
				{		
					propListSource = (List<String>) cache.get(parameters).getValue();
					logger.info("Property List Cache hit: "+info.endpoint);
				}
			} catch(Exception e){logger.info("PropertyMapping cache exception:"+e.getMessage());}
			if(propListSource == null || propListSource.size()==0) {
				propListSource = SPARQLHelper.properties(info.endpoint, info.graph, className);
				cache.put(new Element(parameters, propListSource));
				cache.flush();	
			}
			// target
			info = config.getTarget();
			className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
			parameters = Arrays.asList(new Object[] {info.endpoint, info.graph, className});
			try{
				if(cache.isKeyInCache(parameters))
				{		
					propListTarget = (List<String>) cache.get(parameters).getValue();
					logger.info("Property List Cache hit: "+info.endpoint);
				}
			} catch(Exception e){logger.info("PropertyMapping cache exception:"+e.getMessage());}
			if(propListTarget == null || propListTarget.size()==0) {
				propListTarget = SPARQLHelper.properties(info.endpoint, info.graph, className);
				if(cache.getStatus()==net.sf.ehcache.Status.STATUS_UNINITIALISED) {cache.initialise();}					
				cache.put(new Element(parameters, propListTarget));
				cache.flush();	
			}

		}
		else {
			info = config.getSource();
			className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
			propListSource = SPARQLHelper.properties(info.endpoint, info.graph, className);
			logger.info("Got "+propListSource.size()+ " source props");
			info = config.getTarget();
			className = info.restrictions.get(0).substring(info.restrictions.get(0).indexOf("rdf:type")+8);
			propListTarget = SPARQLHelper.properties(info.endpoint, info.graph, className);
			logger.info("Got "+propListTarget.size()+ " target props");
		}
		for(String prop : propListSource) {
			String s_abr=PrefixHelper.abbreviate(prop);
			sourceProperties.add(s_abr);
		}

		for(String prop : propListTarget) {
			String s_abr=PrefixHelper.abbreviate(prop);
			targetProperties.add(s_abr);
		}
	}

	/**
	 * Method tries to getpropertyMapping
	 */
	private Mapping performAutomaticPropertyMapping() {
		Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
		PropertyMapper propMap = new PropertyMapper();
		return propMap.getPropertyMapping(config.getSource().endpoint, config.getTarget().endpoint, config.getSource().getClassOfendpoint(), config.getTarget().getClassOfendpoint());
	}

	/**
	 * Show computed Property mapping in select, activate Button to use them all.
	 * @param map
	 */
	private void displayPropertyMapping(Map<String, HashMap<String, Double>> map)
	{
		logger.info("Displaying property Mapping");
		useAll.addListener(new UseComputedClickListener(map));
		if(map.size()>0)
			useAll.setEnabled(true);
		for(String key : map.keySet()) {
			for(Entry<String, Double> e : map.get(key).entrySet())
			{				
				select.addItem(new ClassMatchItem(key, e.getKey(), e.getValue()));
			}
		}
		select.setImmediate(true);
		select.setNullSelectionAllowed(false);

		select.addListener(new Property.ValueChangeListener() {			
			@Override
			public void valueChange(ValueChangeEvent event) {
				ClassMatchItem item = (ClassMatchItem) select.getValue();
				addSingleMatchToTable(item.getSourceClass(), item.getTargetClass());
			}
		});
	}

	/**
	 * Public method to submit computed property mapping from the ButtonListener to the table.
	 * @param propertyMap
	 */
	public void addMapToTable(Map<String, HashMap<String, Double>> propertyMap) {
		for(String key : propertyMap.keySet()) {
			for(Entry<String, Double> e : propertyMap.get(key).entrySet())
			{
				addSingleMatchToTable(key, e.getKey());
			}
		}
		useAll.setEnabled(false);
	}
	/**
	 * Method to add a single match to table.
	 * @param sourceClass
	 * @param targetClass
	 */
	public void addSingleMatchToTable(String sourceClass, String targetClass) {
		if(sourceClass == null || sourceClass.length()==0 || targetClass == null || targetClass.length()==0)
			return;
		listenerActive=false;
		Object[] row = createTableRow();
		PropertyComboBox sourceBox = (PropertyComboBox) row[0];
		PropertyComboBox targetBox = (PropertyComboBox) row[1];
		sourceBox.addItem(sourceClass);
		sourceBox.select(sourceClass);
		targetBox.addItem(targetClass);
		targetBox.select(targetClass);
		table.addItem(row,row);
		listenerActive=true;
	}
	
	/**
	 * Called on next button click.
	 */
	public void submit() {
		propMapper.stop();
		Configuration config = ((SAIMApplication)getApplication()).getConfig();//Configuration.getInstance();
		for(Object[] row : rows) {
			if(((PropertyComboBox)row[0]).getValue() != null && ((PropertyComboBox)row[0]).getValue()!=null &&
					((PropertyComboBox)row[0]).getValue().toString().length()>0 && 
					((PropertyComboBox)row[1]).getValue().toString().length()>0) {
				addProperty(((PropertyComboBox)row[0]).getValue().toString(),config.getSource());
				addProperty(((PropertyComboBox)row[1]).getValue().toString(),config.getTarget());
				config.addPropertiesMatch(((PropertyComboBox)row[0]).getValue().toString(), ((PropertyComboBox)row[1]).getValue().toString(), true);
			}			
		}
	}
	
	/**
	 * Listener to add all computed property matches to the table.
	 * @author Lyko
	 *
	 */
	class UseComputedClickListener implements Button.ClickListener {
		Map<String, HashMap<String, Double>> propertyMap;
		public UseComputedClickListener(Map<String, HashMap<String, Double>> propertyMap) {
			this.propertyMap = propertyMap;
		}
		
		@Override
		public void buttonClick(ClickEvent event) {
			addMapToTable(propertyMap);		
		}
	}
	
	/**
	 * Bean for a single class match.
	 * @author Lyko
	 */
	class ClassMatchItem {
		private String sourceClass;
		private String targetClass;
		private double similarity;
		public ClassMatchItem(String sourceClass, String targetClass, double similarity) {
			this.sourceClass=sourceClass;
			this.targetClass=targetClass;
			this.similarity=similarity;
		}
		public ClassMatchItem(String sourceClass, String targetClass) {
			
		}
		public String getSourceClass() {
			return sourceClass;
		}
		public void setSourceClass(String sourceClass) {
			this.sourceClass = sourceClass;
		}
		public String getTargetClass() {
			return targetClass;
		}
		public void setTargetClass(String targetClass) {
			this.targetClass = targetClass;
		}
		public double getSimilarity() {
			return similarity;
		}
		public void setSimilarity(double similarity) {
			this.similarity = similarity;
		}
		@Override
		public String toString() {
			String ret = sourceClass + " - " +targetClass;
			if(!Double.isNaN(similarity))
				ret+= " (" + similarity + ")";
			return ret;
		}
	}
}