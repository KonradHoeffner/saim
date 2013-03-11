package de.uni_leipzig.simba.saim.gui.widget;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.apache.log4j.Logger;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.saim.Messages;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.backend.ExampleConfig;
import de.uni_leipzig.simba.saim.backend.ExampleLoader;
import de.uni_leipzig.simba.saim.core.Configuration;
@SuppressWarnings("serial")
public class ConfigUploader extends CustomComponent
implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver
{
	//private final Messages messages;
	public static final String base = System.getProperty("user.home")+"/";
	public static final String store = "SAIM/EPStore";
	private final Panel root;
	private File file;
	private final ConfigReader cR = new ConfigReader();
	private final Button executeFileButton;
	private final NativeSelect localExamplesSelect;
	private final Button runExampleButton;
	static final String DEFAULT_LIMES_XML = "examples/dbpedia-linkedmdb.xml";
	Button add;
	ExampleLoader loader;
	Logger logger = Logger.getLogger("ConfigUploader.class");
	public ConfigUploader(Messages messages)
	{
		//this.messages=messages;
		executeFileButton = new Button(messages.getString("executefile"));
		localExamplesSelect = new NativeSelect(messages.getString("localexamples"));
		runExampleButton = new Button(messages.getString("runexample"));

		root = new Panel("limesupload");
		root.setWidth("100%");
		setCompositionRoot(root);
		// Create the Upload component.
		final Upload upload = new Upload("", this);
		// Listen for events regarding the success of upload.
		upload.addListener((Upload.SucceededListener) this);
		upload.addListener((Upload.FailedListener) this);
		root.getContent().addComponent(upload);
		//add Button to proceed
		executeFileButton.setEnabled(false);
		executeFileButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Configuration config = ((SAIMApplication) getApplication()).getConfig();//Configuration.getInstance();
				config = new Configuration();
				config.setFromConfigReader(cR);
				file.delete();
				SAIMApplication appl = (SAIMApplication) getApplication();
				appl.getMainWindow().requestRepaintAll();
			}
		});
		root.getContent().addComponent(executeFileButton);
	}

	public void attach() {
		loader = new ExampleLoader((SAIMApplication) getApplication());
		buildLocalExamplesSelection();
	}


	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		FileOutputStream fos = null; // Output stream to write to
		file = new File(base+store + filename);
		try {
			// Open the file for writing.
			fos = new FileOutputStream(file);
			executeFileButton.setEnabled(false);
		} catch (final java.io.FileNotFoundException e) {
			// Error while opening the file. Not reported here.
			e.printStackTrace();
			return null;
		}
		return fos; // Return the output stream to write to
	}

	@Override
	public void uploadFailed(FailedEvent event) {
		// Log the failure on screen.
		root.addComponent(new Label("Uploading "
				+ event.getFilename() + " of type '"
				+ event.getMIMEType() + "' failed."));
	}

	@Override
	public void uploadSucceeded(SucceededEvent event) {
		root.addComponent(new Label("File " + event.getFilename()
				+ " of type '" + event.getMIMEType()
				+ "' uploaded."));
		if(isValidFile(file))
		{
			executeFileButton.setEnabled(true);
		}
		
		Configuration config = ((SAIMApplication) getApplication()).getConfig();//Configuration.getInstance();

		ConfigReader cR = new ConfigReader();
	
		cR.validateAndRead(base+store+event.getFilename());
		System.out.println(cR);
		// setting location of limes.dtd
		// set paths to source and target
		if(cR.sourceInfo.type!=null && cR.targetInfo.type != null)
		try {
			URL url;String path;
			if(cR.sourceInfo.type.equalsIgnoreCase("CSV")) {
				logger.info("Trying to get resource..."+"examples/"+cR.sourceInfo.endpoint);
				url = getClass().getClassLoader().getResource("examples/"+cR.sourceInfo.endpoint);//dbpedia-linkedmdb.xml");
				path = new File(url.toURI()).getAbsolutePath();
				cR.sourceInfo.endpoint = path;
			}
			if(cR.targetInfo.type.equalsIgnoreCase("CSV")) {
				logger.info("Trying to get resource..."+"examples/"+cR.targetInfo.endpoint);
				url = getClass().getClassLoader().getResource("examples/"+cR.targetInfo.endpoint);//dbpedia-linkedmdb.xml");
				path = new File(url.toURI()).getAbsolutePath();
				cR.targetInfo.endpoint = path;
			}
		}catch(URISyntaxException e) {
			e.printStackTrace();
		}
		config.setFromConfigReader(cR);
		SAIMApplication appl = (SAIMApplication) getApplication();
		appl.refresh();
		appl.getMainWindow().removeWindow(getWindow());	
	}

	private boolean isValidFile(File f) {
		return cR.validateAndRead(f.getAbsolutePath());
	}

	public ConfigReader getReaderForUpload() {
		if(cR.validateAndRead(file.getAbsolutePath()))
			return cR;
		else
			return null;
	}


	/** Method to add components to run local examples: for testing e.g. learner or execution.*/
	private void buildLocalExamplesSelection() {
		HorizontalLayout subLayout = new HorizontalLayout();
		subLayout.addComponent(localExamplesSelect);
		subLayout.addComponent(runExampleButton);
		//@TODO generic way to load config.xmls.
		List<ExampleConfig> list = loader.getExamples();
		for(ExampleConfig ex : list) {
			localExamplesSelect.addItem(ex.getFilePath());
		}
		localExamplesSelect.select(list.get(1).getFilePath());
//		localExamplesSelect.addItem("examples/PublicationData.xml");
//		localExamplesSelect.addItem("examples/DBLP-Scholar.xml");
//		localExamplesSelect.addItem("examples/dailymed-drugbank.xml");
//		localExamplesSelect.addItem("examples/Abt-Buy.xml");
//		localExamplesSelect.addItem("examples/Amazon-GoogleProducts.xml");
//		localExamplesSelect.addItem("examples/astronauts-astronauts.xml");
//
//		localExamplesSelect.addItem(DEFAULT_LIMES_XML);
//		localExamplesSelect.select(DEFAULT_LIMES_XML);
		// Button to run a default spec locally
		runExampleButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Configuration config = ((SAIMApplication) getApplication()).getConfig();//Configuration.getInstance();

				ConfigReader cR = new ConfigReader();
				InputStream inStream;
				inStream = getClass().getClassLoader().getResourceAsStream(""+localExamplesSelect.getValue());
				cR.validateAndRead(inStream, (String) localExamplesSelect.getValue());
				System.out.println(cR);
				// setting location of limes.dtd
				// set paths to source and target
				if(cR.sourceInfo.type!=null && cR.targetInfo.type != null)
				try {
					URL url;String path;
					if(cR.sourceInfo.type.equalsIgnoreCase("CSV")) {
						logger.info("Trying to get resource..."+"examples/"+cR.sourceInfo.endpoint);
						url = getClass().getClassLoader().getResource("examples/"+cR.sourceInfo.endpoint);//dbpedia-linkedmdb.xml");
						path = new File(url.toURI()).getAbsolutePath();
						cR.sourceInfo.endpoint = path;
					}
					if(cR.targetInfo.type.equalsIgnoreCase("CSV")) {
						logger.info("Trying to get resource..."+"examples/"+cR.targetInfo.endpoint);
						url = getClass().getClassLoader().getResource("examples/"+cR.targetInfo.endpoint);//dbpedia-linkedmdb.xml");
						path = new File(url.toURI()).getAbsolutePath();
						cR.targetInfo.endpoint = path;
					}
				}catch(URISyntaxException e) {
					e.printStackTrace();
				}
				config.setFromConfigReader(cR);
				SAIMApplication appl = (SAIMApplication) getApplication();
				appl.refresh();
				appl.getMainWindow().removeWindow(getWindow());
			}
		});
		root.getContent().addComponent(subLayout);
	}
}
