package de.uni_leipzig.simba.saim.gui.widget;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.jonatan.contexthelp.ContextHelp;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;

import de.uni_leipzig.simba.io.KBInfo;
import de.uni_leipzig.simba.saim.core.DefaultEndpointLoader;
import de.uni_leipzig.simba.saim.gui.validator.EndpointURLValidator;
import de.uni_leipzig.simba.saim.gui.validator.PageSizeValidator;

/** Allows the user to manually set the properties of a knowledge base, which are endpoint URL, graph URI, page size, restrictions */
@SuppressWarnings("serial")
public class KBInfoForm extends Form
{	
	protected final static String WIDTH = "35em";
	protected final ComboBox presetComboBox = new ComboBox("Preset");
	protected final TextField url = new TextField("Endpoint URL");
	protected final TextField id = new TextField("Id / Namespace");
	protected final TextField graph = new TextField("Graph");
	protected final TextField pageSize = new TextField("Page size", "-1");
	protected final TextField textFields[] = {graph, id, pageSize};	
	protected final Button next = new Button("OK" );
	protected final Component components[] = {url, graph, pageSize, next};

	/** the knowledge base presets*/
	protected final Map<String,KBInfo> presetToKB = new HashMap<>();

	public KBInfoForm(String title)
	{
		this.setImmediate(true);
		this.setCaption(title);
		this.setWidth(WIDTH);
		addFormFields();
		
		// Have a button bar in the footer.
		HorizontalLayout buttonBar = new HorizontalLayout();
		//buttonBar.setHeight("25px");
		getFooter().addComponent(buttonBar);		 
		// Add an Ok (commit), Reset (discard), and Cancel buttons
		setValidationVisible(true);
		buttonBar.addComponent(new Button("Reset", this,"reset"));
		getLayout().setMargin(true);
		for(TextField field: textFields)
		{
			field.setWidth("100%");
		}
		
		setupContextHelp();
	}

	protected void presets()
	{						
		presetComboBox.setRequired(false);
		presetComboBox.setWidth("100%");
		presetComboBox.setNewItemsAllowed(false);
		for(String preset : presetToKB.keySet())
		{
			presetComboBox.addItem(preset);
		}
		presetComboBox.addListener(new ValueChangeListener()
		{
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event)
			{
				if(presetToKB.containsKey(presetComboBox.getValue()))
				{
					KBInfo kb = presetToKB.get(presetComboBox.getValue());
					if(kb.endpoint!=null)	{url.setValue(kb.endpoint);}
					if(kb.id!=null)			{id.setValue(kb.id);}
					if(kb.graph!=null)		{graph.setValue(kb.graph);}
					pageSize.setValue(Integer.toString(kb.pageSize));
				}
			}
		});
	}
	
	private void addFormFields()
	{
		setDefaultEndpoints();
		presets();
		addField("Presets",presetComboBox);			
		
		addField("Endpoint URL",url);
		
		url.addValidator(new EndpointURLValidator(url));
		url.setRequired(true);
		url.setRequiredError("The endpoint URL may not be empty.");
		url.setWidth("100%");
		url.addListener(new BlurListener(){
			@Override
			public void blur(BlurEvent event) {
				if(url.isValid())
				{
					if(!presetToKB.containsKey(url.getValue()))
					{
						try {
							String val = (String) url.getValue();
							String idSuggestion = val.substring(val.indexOf("http://")+7);
							idSuggestion = idSuggestion.substring(0, idSuggestion.indexOf("/"));
							if(idSuggestion.indexOf(".") > 0)
								idSuggestion = idSuggestion.substring(0, idSuggestion.indexOf("."));
							id.setValue(idSuggestion);
							// if string is not long enough and thus substring fails
						} catch(IndexOutOfBoundsException e) {id.setValue(url.getValue());}
					}
				}
			}
		});
		addField("ID / Namespace", id);
		addField("Graph",graph);
		addField("Page size",pageSize);
		pageSize.addValidator(new PageSizeValidator("Page size needs to be an integer."));
	}

	protected void setupContextHelp()
	{
		ContextHelp contextHelp = new ContextHelp();
		getLayout().addComponent(contextHelp);
		contextHelp.addHelpForComponent(url, "Fill in the URL of the SPARQL endpoint, e.g. <b>http://dbpedia.org/sparql</b>.");
		contextHelp.addHelpForComponent(id, "Is used by the class matcher to find sameAs links. Only instances whose url contains the id are chosen to count as original instances of this endpoint.");
		contextHelp.addHelpForComponent(graph, "<em>(optional)</em> The Default Data Set Name (Graph IRI), e.g. <b>http://dbpedia.org</b>. " +
				"Providing a graph is optional and only needed if you want to exclude some data or speed up the process.");
		contextHelp.addHelpForComponent(pageSize, "<em>(optional)</em> Use a small page size if you get time outs while matching " +
				"and a big page size if you want more speed.");
		//contextHelp.setFollowFocus(true);
	}

	public void reset()
	{
		for(TextField field: textFields)
		{
			field.setValue("");
		}
	}

	public KBInfo getKBInfo() {
		KBInfo kbInfo = new KBInfo();
		kbInfo.id = id.getValue().toString();
		kbInfo.endpoint = url.getValue().toString();
		kbInfo.graph = graph.getValue().toString();
		int pageSizeInt = Integer.parseInt((String)pageSize.getValue());
		kbInfo.pageSize = pageSizeInt;
		return kbInfo;
	}

	private void setDefaultEndpoints()
	{
		presetToKB.clear();
		presetComboBox.removeAllItems();
		for(KBInfo kb : DefaultEndpointLoader.getDefaultEndpoints())
		{
			presetToKB.put(kb.endpoint,kb);
			presetComboBox.addItem(kb.endpoint);
		}		
	}

} 