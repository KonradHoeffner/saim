package de.uni_leipzig.simba.saim.gui.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;

import de.uni_leipzig.simba.io.ConfigReader;
import de.uni_leipzig.simba.saim.SAIMApplication;
import de.uni_leipzig.simba.saim.core.Configuration;

public class ConfigUploader extends CustomComponent 
	implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver{

	public static final String UPLOAD_FOLDER = "C:/tmp/uploads/";
	private Panel root;
	private File file;
	private ConfigReader cR = new ConfigReader();
	private Button proceed = new Button("Execute fuile");
	private Window mainWindow;
	
	public ConfigUploader(final Window mainWindow) {
		this.mainWindow = mainWindow;
		root = new Panel("Limes Configuration Upload");
		root.setWidth("100%");
	    setCompositionRoot(root);
        // Create the Upload component.
        final Upload upload =
        new Upload("", this);
        // Listen for events regarding the success of upload.
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        root.getContent().addComponent(upload);
        //add Button to proceed
        proceed.setEnabled(false);
        proceed.addListener(new Button.ClickListener() {			
			@Override
			public void buttonClick(ClickEvent event) {
				Configuration config = Configuration.getInstance();
				config.setFromConfigReader(cR);
				file.delete();
				mainWindow.removeWindow(getWindow());
				SAIMApplication appl = (SAIMApplication) mainWindow.getApplication();
				appl.executeLimes();
			}
		});
        
        
        root.getContent().addComponent(proceed);
        
	}
	
	
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		FileOutputStream fos = null; // Output stream to write to
	    file = new File(UPLOAD_FOLDER + filename);
        try {
            // Open the file for writing.
            fos = new FileOutputStream(file);
            proceed.setEnabled(false);
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
			 proceed.setEnabled(true);
		 }
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

}
