package de.uni_leipzig.simba.saim.gui.widget.panel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map.Entry;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.core.Configuration;
import de.uni_leipzig.simba.saim.core.LimesRunner;
import de.uni_leipzig.simba.saim.gui.widget.InstanceMappingTable;
import de.uni_leipzig.simba.saim.gui.widget.Listener.MetricPanelListeners;
/**
 * Panel to execute a Mapping.
 * @author Lyko
 */
public class ExecutionPanel extends PerformPanel implements PropertyChangeListener
{
	private final Messages messages;
	private LimesRunner lR;
	private Label progressLabel;
	private ProgressIndicator progress;
	private Mapping m = new Mapping();
	private float maxSteps = LimesRunner.MAX_STEPS;
//	Button start;
	//Button showResults;
//	Button startActiveLearning;
//	Button startBatchLearning;
//	Button startSelfConfig;
	Layout mainLayout = new VerticalLayout();
	Thread thread;
	@SuppressWarnings("serial")
	public ExecutionPanel(final Messages messages)
	{		
		super(messages.getString("ExecutionPanel.executelinkspecification")); //$NON-NLS-1$
		this.messages=messages;
//		Label l;
//		Configuration config = ((SAIMApplication)getApplication()).getConfig();
		lR = new LimesRunner();
		lR.addPropertyChangeListener(this);
		progressLabel = new Label(messages.getString("ExecutionPanel.initialized")); //$NON-NLS-1$
		progress = new ProgressIndicator();
		progress.setValue(0);

		setWidth("100%"); //$NON-NLS-1$
		this.setContent(mainLayout);
		mainLayout.addComponent(progressLabel);
		mainLayout.addComponent(progress);
	}	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(LimesRunner.MESSAGE)) {
			progressLabel.setValue(evt.getNewValue());
			progressLabel.requestRepaint();
		}
		if(evt.getPropertyName().equals(LimesRunner.STEP)) {
			float newV = Float.valueOf(evt.getNewValue().toString());
			progress.setValue(newV/maxSteps);
			progress.requestRepaint();
		}
	}
	
	
	private void runMapping() {
		thread = new Thread() {
			@Override
			public void run() {
				m = lR.runConfig(((SAIMApplication)getApplication()).getConfig());	
				progress.setValue(1f);
				progressLabel.setValue(messages.getString("ExecutionPanel.mappingperformed")); //$NON-NLS-1$
				InstanceMappingTable iT = new InstanceMappingTable(m, lR.getSourceCache(), lR.getTargetCache(), false,messages);
				ResultPanel results = new ResultPanel(iT,messages);
				mainLayout.addComponent(results);
//				mainLayout.removeComponent(start);
			}
		};
		thread.start();
	}
	
//	private Panel showPropertyMatching() {
@SuppressWarnings("deprecation")
	//		Panel p = new Panel();
//		if(!Configuration.getInstance().propertyMapping.wasSet()) {
//			p.setContent(new Panel(messages.getString("ExecutionPanel.nopropertymappingdefined"))); //$NON-NLS-1$
//		} else {
//			p.setCaption(messages.getString("ExecutionPanel.propertymapping")); //$NON-NLS-1$
//			VerticalLayout panelLayout = new VerticalLayout();
//			p.setContent(panelLayout);
//		
//			ListSelect stringSelect = new ListSelect(messages.getString("ExecutionPanel.stringproperties")); //$NON-NLS-1$
//			stringSelect.setNullSelectionAllowed(false);
//			stringSelect.setRows(Configuration.getInstance().propertyMapping.getStringPropMapping().map.size());
//			for(Entry<String, HashMap<String, Double>> entry : Configuration.getInstance().propertyMapping.getStringPropMapping().map.entrySet()) {
//				for(String t : entry.getValue().keySet()) {
//					stringSelect.addItem(entry.getKey() +" - "+t); //$NON-NLS-1$
//				}
//			}
//			ListSelect numberSelect = new ListSelect(messages.getString("ExecutionPanel.numberproperty")); //$NON-NLS-1$
//			numberSelect.setNullSelectionAllowed(false);
//			numberSelect.setRows(Configuration.getInstance().propertyMapping.getNumberPropMapping().map.size());
//			for(Entry<String, HashMap<String, Double>> entry : Configuration.getInstance().propertyMapping.getNumberPropMapping().map.entrySet()) {
//				for(String t : entry.getValue().keySet()) {
//					numberSelect.addItem(entry.getKey() +" - "+t); //$NON-NLS-1$
//				}
//			}
//			if(Configuration.getInstance().propertyMapping.getStringPropMapping().size>0)
//				panelLayout.addComponent(stringSelect);
//			if(Configuration.getInstance().propertyMapping.getNumberPropMapping().size>0)
//				panelLayout.addComponent(numberSelect);
//		}
//		return p;
//	}
	@Override
	public void onClose() {
		thread.stop();
	}
	@Override
	public void start() {
		runMapping();
	}
}
