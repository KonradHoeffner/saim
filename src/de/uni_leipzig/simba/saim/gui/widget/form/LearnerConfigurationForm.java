package de.uni_leipzig.simba.saim.gui.widget.form;

import org.vaadin.risto.stepper.IntStepper;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.Slider;

import de.uni_leipzig.simba.saim.Messages;
/**
 * Form used to configure genetic metric learner.
 * @author Lyko
 *
 */
public class LearnerConfigurationForm extends Form{

	public static final String elementsWidth = "100px"; //$NON-NLS-1$
	
	public LearnerConfigurationForm(LearnerConfigurationBean bean) {
		
		setCaption(Messages.getString("LearnerConfigurationForm.learnerconfigcaption")); //$NON-NLS-1$
//		setDescription("Specify parameters for the genetic metric learner. Note that higher values for the number of generations and the population size will lead in longer execution time.");
		 
		setFormFieldFactory(new LearnerCofigurationFormFieldFactory());
		
		 BeanItem item = new BeanItem(bean);
		
		 setItemDataSource(item);
		 this.setWriteThrough(true);
	}
	
	/**
	 * We use a factory to build Form Fields.
	 * @author Lyko
	 *
	 */
	class LearnerCofigurationFormFieldFactory implements FormFieldFactory {
	    public Field createField(Item item, Object propertyId,
	                             Component uiContext) {
	        // Identify the fields by their Property ID.
	        String pid = (String) propertyId;
	        if ("generations".equals(pid)) { //$NON-NLS-1$
	        	IntStepper intStepper = new IntStepper();
	            intStepper.setStepAmount(5);
	            intStepper.setMaxValue(100);
	            intStepper.setMinValue(5);
	            intStepper.setCaption(Messages.getString("LearnerConfigurationForm.generations")); //$NON-NLS-1$
	            intStepper.setWidth(elementsWidth);
	            intStepper.setPropertyDataSource(item.getItemProperty(pid));
	            return intStepper;
	        } 
	        else if ("population".equals(pid)) { //$NON-NLS-1$
	        	IntStepper intStepper = new IntStepper();
	            intStepper.setStepAmount(5);
	            intStepper.setMaxValue(100);
	            intStepper.setMinValue(5);
	            intStepper.setCaption(Messages.getString("LearnerConfigurationForm.population")); //$NON-NLS-1$
	            intStepper.setWidth(elementsWidth);
	            intStepper.setPropertyDataSource(item.getItemProperty(pid));
	            return intStepper;
	        }
	        else if("mutationRate".equals(pid)) { //$NON-NLS-1$
	        	Slider slider = new Slider(0d, 1d, 1);
	        	slider.setCaption(Messages.getString("LearnerConfigurationForm.mutation")); //$NON-NLS-1$
	        	slider.setWidth(elementsWidth);
	        	slider.setOrientation(Slider.ORIENTATION_HORIZONTAL);
	        	slider.setPropertyDataSource(item.getItemProperty(pid));
	        	return slider;
	        }
	        else if(Messages.getString("LearnerConfigurationForm.8").equals(pid)) { //$NON-NLS-1$
	        	IntStepper intStepper = new IntStepper();
	            intStepper.setStepAmount(10);
	            intStepper.setMaxValue(100);
	            intStepper.setMinValue(10);
	            intStepper.setCaption(Messages.getString("LearnerConfigurationForm.inquiries")); //$NON-NLS-1$
	            intStepper.setWidth(elementsWidth);
	            intStepper.setPropertyDataSource(item.getItemProperty(pid));
	            return intStepper;
	        }
	        
	        return null; // Invalid field (property) name.
	    }
	}
}
