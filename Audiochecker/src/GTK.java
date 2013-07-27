//The user interface
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.gnome.gdk.Event;
import org.gnome.gdk.Pixbuf;
import org.gnome.glib.Glib;
import org.gnome.glib.Handler;
import org.gnome.gtk.Alignment;
import org.gnome.gtk.Button;
import org.gnome.gtk.FileChooserAction;
import org.gnome.gtk.FileChooserDialog;
import org.gnome.gtk.FileFilter;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.HBox;
import org.gnome.gtk.Label;
import org.gnome.gtk.ProgressBar;
import org.gnome.gtk.ScrolledWindow;
import org.gnome.gtk.TextBuffer;
import org.gnome.gtk.TextTag;
import org.gnome.gtk.TextView;
import org.gnome.gtk.VBox;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.gtk.Window.DeleteEvent;
import org.gnome.gtk.WindowPosition;

public class GTK extends Window implements DeleteEvent {
	
	private String directoryPath = null;
	private String filePath = null;
	private Collection<String> fileList = new ArrayList<String>();
	private auCDtect auCDtect;
	private TextBuffer resultsBuffer;
	private ProgressBar progressBar;
	private double progress = 0;
	private String output;
	private String summary;
	boolean outputUpdated = false;

	public GTK() {
    
    	//Set window title
        setTitle("Dan's AudioChecker");
        
        //Initialise user interface
        initUI(this);
    
        //Exit GUI cleanly if close pressed
        connect(this);
        
        //Set default size, position and make window visible
        setDefaultSize(800, 800);
        setPosition(WindowPosition.CENTER);
        showAll();
    }
    
    public void initUI(final GTK gtk) {
    	
    	setWindowIcon();
    	
    	//Create container to vertically stack widgets
    	VBox vBox = new VBox(false, 5);
    	
    	//Set alignment and size of action buttons container
        Alignment halign = new Alignment(0, 0, 1, 0);
        
        //Create hoziontal box for action buttons (homogenous spacing - false, default spacing 10)
        HBox actionButtons = new HBox(false, 10);
        
        //Create horizontal box for view panes
        HBox viewPanes = new HBox(false, 10);
        
        //Create horizontal box for progress bar
        HBox progressBarBox = new HBox(false, 10);

        //create scrollable text box for file queue
        final TextBuffer queueBuffer = new TextBuffer();
        TextTag label = new TextTag();
        label.setBackground("#EAE8E3");
        queueBuffer.insert(queueBuffer.getIterStart(), "Queue \n", label);
        queueBuffer.insert(queueBuffer.getIterEnd(), "\n");
        TextView queue = new TextView(queueBuffer);
        queue.setEditable(false);
        queue.setCursorVisible(false);
        ScrolledWindow queueWindow = new ScrolledWindow();
        queueWindow.add(queue);
        
        //create scrollable text box for results info
        resultsBuffer = new TextBuffer();
        resultsBuffer.insert(resultsBuffer.getIterStart(), "Results \n", label);
        resultsBuffer.insert(resultsBuffer.getIterEnd(), "\n");
        TextView results = new TextView(resultsBuffer);
        results.setEditable(false);
        results.setCursorVisible(false);
        ScrolledWindow resultsWindow = new ScrolledWindow();
        resultsWindow.add(results);
        
        //Create buttons to user interface
        final FileChooserDialog directoryDialog = new FileChooserDialog("Directory", null, FileChooserAction.SELECT_FOLDER);
        Button directory = new Button("Directory");
        final FileChooserDialog filesDialog = new FileChooserDialog("Files", null, FileChooserAction.OPEN);
        FileFilter allLosslessMusic = new FileFilter("Lossless Music");
        allLosslessMusic.addMimeType("audio/flac");allLosslessMusic.addMimeType("audio/wav");
        Button files = new Button("File(s)");
        filesDialog.addFilter(allLosslessMusic);
        Label emptyLabel = new Label("");
        Button start = new Button("Start");
        
        //Create progress bar and add to hbox
        progressBar = new ProgressBar();
        progressBarBox.add(progressBar);
        
        //Make fileChooser dialog come up when directories is clicked
        directory.connect(new Button.Clicked(){
			
			@Override
			public void onClicked(Button directory) {
				directoryDialog.run();
				directoryPath = directoryDialog.getFilename();
				String[] extensions = {".flac", ".wav"};
				if(!(directoryPath == null)){
					Iterator<File> files = FileUtils.iterateFiles(new File(directoryPath), new SuffixFileFilter(extensions, IOCase.INSENSITIVE),DirectoryFileFilter.DIRECTORY);
					while(files.hasNext()){
						File j =  files.next();
						fileList.add(j.getPath());
						queueBuffer.insert(queueBuffer.getIterEnd(), j+"\n");
					}
				}	
				directoryDialog.hide();
			}
		});
        
        //Make fileChooser dialog come up when files is clicked
        files.connect(new Button.Clicked(){
			
			@Override
			public void onClicked(Button files) {
				filesDialog.run();
				if (!(filesDialog.getFilename() == null)){
					filePath = filesDialog.getFilename();
					fileList.add(filePath);
					queueBuffer.insert(queueBuffer.getIterEnd(), filePath+"\n");
				}
				filesDialog.hide();
			}
		});
        
        //Make start button work
        start.connect(new Button.Clicked(){
        	
        	@Override
        	public void onClicked(Button start){
        		auCDtect = new auCDtect(fileList, gtk);
        		Thread auCDtectThread = new Thread(auCDtect);
        		auCDtectThread.start();
        		
        		Glib.idleAdd(new Handler(){
        	   	     public boolean run(){
        	   	    	 
        	   	    	 progress = auCDtect.getProgress();
        	   	    	 output = auCDtect.getOutput();
        	   	    	 summary = auCDtect.getSummary();

        	   	    	 if(summary == null){
        	   	    		 progressBar.setFraction(progress);
        	   	    		 
        	   	    	      if((outputUpdated == true)&&(output != null)){
        	      	    	    		resultsBuffer.insert(resultsBuffer.getIterEnd(), output+"\n");
        	      	    	    		setOutputUpdated(false);
        	      	    	    	}
        	  	    	    	 
        	   	    		 return true;
        	   	    	 }
        	   	    	 else{
        	   	    		 progressBar.setFraction(progress);
        	  	    	    	 if(summary != null){
        	   	    	    		resultsBuffer.insert(resultsBuffer.getIterEnd(), "\n"+"Results summary:"+"\n\n"+summary);
        	   	    	    	}
        	  	    	    	 
        	   	    		 return false;
        	   	    	 }
        	   	     }
        	   	 });
        	}
        });

        //Position buttons and add to fix
        actionButtons.packStart(directory, false, false, 0);
        actionButtons.packStart(files, false, false, 0);
        actionButtons.packStart(emptyLabel, true, true, 0);
        actionButtons.packStart(start, false, false, 0);
             
        //Add 
        viewPanes.add(queueWindow);
        viewPanes.add(resultsWindow);
        halign.add(actionButtons);
        vBox.packStart(halign, false, false, 10);
        vBox.packStart(viewPanes, true, true, 10);
        vBox.packStart(progressBarBox, false, false, 10);
        add(vBox);
        
        setBorderWidth(15);
    }
    
    //Method to exit application
    public boolean onDeleteEvent(Widget widget, Event event) {
        Gtk.mainQuit();
        return false;
    }
    
    //Method to set window icon
    public void setWindowIcon(){
    //Set the audiochecker icon that appears in the start bar
    Pixbuf icon = null;
    
    try {
       icon = new Pixbuf("audiochecker.png");
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    
    setIcon(icon);
    }
    
    public void setOutputUpdated(boolean outputUpdated){
    	
    	this.outputUpdated = outputUpdated;
    }
    
    public static void main(String[] args) {
    	//Initialise and run GUI
        Gtk.init(args);
        new GTK();
        Gtk.main();
    }
}
