package de.uni_leipzig.simba.saim.gui.widget.panel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

import de.uni_leipzig.simba.saim.core.Configuration;
import de.uni_leipzig.simba.saim.gui.widget.form.KBInfoForm;
import de.uni_leipzig.simba.saim.Messages;
public class EndpointPanel extends Panel implements PropertyChangeListener
{
	public KBInfoForm kbISource;
	public KBInfoForm kbITarget;

	public EndpointPanel()
	{
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		this.setContent(layout);
		Configuration config = Configuration.getInstance();
		config.addPropertyChangeListener(this);
		kbISource = new KBInfoForm(Messages.getString("EndpointPanel.configuresourceendpoint"), config.getSource()); //$NON-NLS-1$
		kbITarget = new KBInfoForm(Messages.getString("EndpointPanel.configuretargetendpoint"), config.getTarget()); //$NON-NLS-1$
		this.addComponent(kbISource);
		this.addComponent(kbITarget);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Configuration.SETTING_CONFIG)) {
			kbISource.setValuesFromKBInfo(Configuration.getInstance().getSource());
			kbITarget.setValuesFromKBInfo(Configuration.getInstance().getTarget());
		}
	}
}