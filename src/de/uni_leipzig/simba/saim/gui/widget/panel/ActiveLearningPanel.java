package de.uni_leipzig.simba.saim.gui.widget.panel;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jgap.InvalidConfigurationException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.genetics.core.Metric;
import de.uni_leipzig.simba.genetics.learner.GeneticActiveLearner;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.core.Configuration;
import de.uni_leipzig.simba.saim.gui.widget.InstanceMappingTable;
import de.uni_leipzig.simba.saim.gui.widget.form.LearnerConfigurationBean;

/** Contains instances of ClassMatchingForm and lays them out vertically.*/
@SuppressWarnings("serial")
public class ActiveLearningPanel extends MetricLearnPanel
{	
	
//	public ActiveLearningPanel() {
//		super();
//		learn.addListener(new ActiveLearnButtonClickListener(learnLayout));
//		init();
//	}
	
	public ActiveLearningPanel(LearnerConfigurationBean learnerConfigBean) {
		super(learnerConfigBean);
		learn.addListener(new ActiveLearnButtonClickListener(learnLayout));
		init();
	}

	/**
	 * Initialize the specific learner.
	 */
	private void init() {
		// configure
		if(learner != null) {
			learner.getFitnessFunction().destroy();
		}
		if(params == null) {
			 params = new HashMap<String, Object>();
			 params.put("populationSize", 20);
			 params.put("generations", 50);
			 params.put("mutationRate", 0.5f);
			 params.put("trainingDataSize", 10);
		}
		
		params.put("preserveFittest",true);
		params.put("propertyMapping", config.propertyMapping); 	
		params.put("granularity", 2); 
		params.put("config", config.getLimesConfiReader()); 
		learner = new GeneticActiveLearner();
		try {
			learner.init(config.getSource(), config.getTarget(), params);
		} catch (InvalidConfigurationException e) {
			layout.setComponentError(new UserError(e.getMessage()));
			e.printStackTrace();
		}
		Mapping map = learner.learn(new Mapping());
		iMapTable = new InstanceMappingTable(map, learner.getFitnessFunction().getSourceCache(), learner.getFitnessFunction().getTargetCache(), true);
		if (map.size()>0)
		{
			learnLayout.removeAllComponents();
			learnLayout.addComponent(iMapTable.getTable());

			learnLayout.removeAllComponents();
			learnLayout.addComponent(iMapTable.getTable());
		}
	}
	
	/** Listener for learn buttton @author Lyko */
	public class ActiveLearnButtonClickListener implements Button.ClickListener
	{
		Layout l;
		/** Constructor with the Component to hold the Table.*/
		public ActiveLearnButtonClickListener(Layout l) {this.l = l;}

		@Override
		public void buttonClick(ClickEvent event) {		
			Mapping map;
			if(iMapTable == null) // on start
			{
				logger.info("Starting Active Learning"); //$NON-NLS-1$
				map = learner.learn(new Mapping());
			}
			else
			{
				logger.info("Starting round"); //$NON-NLS-1$
				map = iMapTable.tabletoMapping();
				if(map.size()==0)
					SAIMApplication.getInstance().getMainWindow().showNotification(Messages.getString("ActiveLearningPanel.learningwithoutnotification")); //$NON-NLS-1$
				map = learner.learn(map);
			}
			
			//iMapTable = new DetailedInstanceMappingTable(map,learner.getFitnessFunction().getSourceCache(),learner.getFitnessFunction().getTargetCache());
			iMapTable = new InstanceMappingTable(map, learner.getFitnessFunction().getSourceCache(), learner.getFitnessFunction().getTargetCache(), true);

			l.removeAllComponents();
			l.addComponent(iMapTable.getTable());
			l.removeAllComponents();
			l.addComponent(iMapTable.getTable());
			if (map.size()>0)
			{
				terminate.setEnabled(true);	
			}
		}		
	}
}