package de.uni_leipzig.simba.saim.gui.widget.window;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TabSheet;

import de.uni_leipzig.simba.saim.LandingPage;
import de.uni_leipzig.simba.saim.Messages;

public class About extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	/**
	 * 
	 */
	private static final long serialVersionUID = -1635796226509916356L;
	@AutoGenerated
	private AbsoluteLayout mainLayout;
	@AutoGenerated
	private TabSheet About;
	@AutoGenerated
	private RichTextArea impressum;
	@AutoGenerated
	private Label about;
	final Messages messages;
	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public About(final Messages messages) {	
		this.messages = messages;
		this.setSizeFull();
	}

	@Override
	public void attach() {
		buildMainLayout();
		setCompositionRoot(mainLayout);
	}
	
	@AutoGenerated
	private AbsoluteLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new AbsoluteLayout();
		mainLayout.setImmediate(false);
//		mainLayout.setWidth("100%");
//		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
//		
//		// top-level component properties
//		setWidth("100.0%");
//		setHeight("100.0%");
		
		// About
		About = buildAbout();
		mainLayout.addComponent(About,
				"top:0.0px;right:1.0px;bottom:-4.0px;left:1.0px;");
		
		return mainLayout;
	}

	@AutoGenerated
	private TabSheet buildAbout() {
		// common part: create layout
		About = new TabSheet();
		About.setImmediate(true);
		About.setWidth("100.0%");
		About.setHeight("100.0%");
		
		// about
		about = new Label(this.getAboutContent());
		about.setVisible(true);

//		about.setWidth("100.0%");
//		about.setHeight("100.0%");
		
		about.setContentMode(Label.CONTENT_XHTML);
		About.addTab(about, "About", null);
		
		Panel man = new Panel("Manual");
//		man.setHeight("500px");
		man.addComponent(getManualLink());
		About.addTab(man);
		return About;
	}
	
	/**
	 * Generate content of the About RichTextField.
	 * @return RichText Content of the about component.
	 */
	private String getAboutContent() {
		String s = "<h1>SAIM Instance Matching Application</h1>" +
				"SAIM is an web interface for the <a href='http://aksw.org/Projects/LIMES.html'>LIMES</a> linking framework.</br>" +
				"<h1>Team</h1>" +
				"<ul>" +
				" <li><a href='http://aksw.org/AxelNgonga.html'>Dr. Axel-C. Ngonga Ngomo</a></li>" +
				" <li><a href='http://aksw.org/KonradHoeffner.html'>Konrad H�ffner</a></li>" +
				" <li>Ren� Speck</li>" +
				" <li>Klaus Lyko</li>" +
				"</ul>";		
		return s;
	}

	public Component getManualLink() {
		URL url;
		url = getClass().getClassLoader().getResource(LandingPage.manual);		
		File f = null;
		try {
			f = new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if(f!= null) {
			Embedded pdf = new Embedded(null, new FileResource(f, getApplication()));
			pdf.setMimeType("application/pdf");
			pdf.setType(Embedded.TYPE_BROWSER);
			pdf.setHeight("500px");
			pdf.setWidth("100%");
//			pdf.setSizeFull();
			return pdf;
		}
		else
			return null;
	}
}
