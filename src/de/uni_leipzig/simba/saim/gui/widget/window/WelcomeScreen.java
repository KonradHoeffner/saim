package de.uni_leipzig.simba.saim.gui.widget.window;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Custum Component shown on start-up of SAIM. Should hold a short description, link to the screencast and some contact infos.
 * @author Klaus Lyko
 *
 */
public class WelcomeScreen extends CustomComponent {
	private static final long serialVersionUID = -3385469588495401435L;
	@AutoGenerated
	private AbsoluteLayout mainLayout;
	@AutoGenerated
	private Label headingLabel;
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public WelcomeScreen() {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		// TODO add user code here
	}

	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();
		mainLayout.setCaption("SAIM - Semi-Automatic Instance Matcher");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// headingLabel
		headingLabel = new Label();
		headingLabel.setCaption("SAIM - Semi-Automatic Instance Matcher");
		headingLabel.setImmediate(false);
		headingLabel.setWidth("860px");
		headingLabel.setHeight("-1px");
		headingLabel
				.setValue("<div align=\"center\"><h1>SAIM - Semi-Automatic Instance Matcher</h1></div>");
		headingLabel.setContentMode(3);
		mainLayout.addComponent(headingLabel, "top:2.0px;left:0.0px;");
		
		return mainLayout;
	}

}
