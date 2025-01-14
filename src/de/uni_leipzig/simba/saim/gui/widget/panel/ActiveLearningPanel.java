package de.uni_leipzig.simba.saim.gui.widget.panel;

import org.jgap.InvalidConfigurationException;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;
import de.uni_leipzig.simba.data.Mapping;
import de.uni_leipzig.simba.genetics.learner.GeneticActiveLearner;
import de.uni_leipzig.simba.genetics.learner.GeneticCorrelationActiveLearner;
import de.uni_leipzig.simba.genetics.learner.SupervisedLearnerParameters;
import de.uni_leipzig.simba.genetics.util.PropertyMapping;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.gui.widget.InstanceMappingTable;
import de.uni_leipzig.simba.saim.gui.widget.form.LearnerConfigurationBean;
/** Contains instances of ClassMatchingForm and lays them out vertically.*/
@SuppressWarnings("serial")
public class ActiveLearningPanel extends MetricLearnPanel
{
	/**
	 * Enum to choose the Active Learning approach.
	 * @author Klaus Lyko
	 *
	 */
	public static enum LEARNER {
		AL_EAGLE, AL_CLUSTER, AL_WD
	}
	LEARNER method = LEARNER.AL_EAGLE;

	private final Messages messages;

	/**
	 * 
	 * @param application
	 * @param learnerConfigBean
	 * @param learner LEARNER enum 
	 * @param messages
	 */
	public ActiveLearningPanel(SAIMApplication application, LearnerConfigurationBean learnerConfigBean, LEARNER learner,final Messages messages)
	{
		super(application, learnerConfigBean);
		this.messages=messages;
		this.method = learner;
		learn.addListener(new ActiveLearnButtonClickListener(learnLayout));
	}
	@Override
	public void attach() {
		config = ((SAIMApplication)getApplication()).getConfig();
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
			if(config.propertyMapping != null)
				params = new SupervisedLearnerParameters(config.getLimesConfiReader(), config.propertyMapping);
			else
				params = new SupervisedLearnerParameters(config.getLimesConfiReader(), new PropertyMapping());
			params.setGenerations(50);
			params.setPopulationSize(10);
			params.setMutationRate(0.5f);
			params.setTrainingDataSize(10);
		}

		params.setPreserveFittestIndividual(true);
	
		params.setGranularity(2);
		
		switch(method) {
			case AL_EAGLE: learner = new GeneticActiveLearner(); break;
			case AL_CLUSTER: learner = new GeneticCorrelationActiveLearner(GeneticCorrelationActiveLearner.CLUSTERING); break;
			case AL_WD: learner = new GeneticCorrelationActiveLearner(GeneticCorrelationActiveLearner.WEIGHT_DECAY); break;
			default: learner = new GeneticActiveLearner(); break;
		}
		
		try {
			learner.init(config.getSource(), config.getTarget(), params);
		} catch (InvalidConfigurationException e) {
			layout.setComponentError(new UserError(e.getMessage()));
			e.printStackTrace();
		}
		Mapping map = learner.learn(new Mapping());
		iMapTable = new InstanceMappingTable
				(getApplication(), config, map, learner.getFitnessFunction().getSourceCache(), learner.getFitnessFunction().getTargetCache(), true,messages);
		if (map.size()>0)
		{
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
				try {
					map = learner.learn(new Mapping());
				} catch(Exception e) {
					application.getMainWindow().showNotification("Error getting intial training data. Please adjust your metric.");//$NON-NLS-1$
					showWarning(messages.getString("MetricLearnPanel.errorMessageIntialTrainingData"));//$NON-NLS-1$
					map = new Mapping();
				}
			}
			else
			{
				try {
					logger.info("Starting round"); //$NON-NLS-1$
					map = iMapTable.tabletoMapping();
				} catch(Exception e) {
					application.getMainWindow().showNotification("Error geting intial training data. Please adjust your metric.");//$NON-NLS-1$
					showWarning(messages.getString("MetricLearnPanel.errorMessageIntialTrainingData"));//$NON-NLS-1$
					map = new Mapping();
				}
				if(map.size()==0)
					getApplication().getMainWindow().showNotification(messages.getString("ActiveLearningPanel.learningwithoutnotification")); //$NON-NLS-1$
				map = learner.learn(map);
			}

			//iMapTable = new DetailedInstanceMappingTable(map,learner.getFitnessFunction().getSourceCache(),learner.getFitnessFunction().getTargetCache());
			iMapTable = new InstanceMappingTable(getApplication(), config, map, learner.getFitnessFunction().getSourceCache(), learner.getFitnessFunction().getTargetCache(), true,messages);

			l.removeAllComponents();
			l.addComponent(iMapTable.getTable());
			if (map.size()>0)
			{
				terminate.setEnabled(true);
			}
		}
	}
}
