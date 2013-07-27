//Checks the wav files using auCDtect command line
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class auCDtect implements Runnable { 

	private String processingLog;
	private String output;
	private String summary;
	private Collection<String> fileList;
	private Collection<String> tempWavList = new ArrayList<String>();
	private double progress = 0.0;
	private GTK gtk;
	
	auCDtect(Collection<String> fileList, GTK gtk){this.fileList = fileList; this.gtk = gtk;}

    public void run () {
    	
    	List<String> command = new ArrayList<String>();
    	command.add("./auCDtect");
    	command.add("-d");
    	command.add("-m10");
    	
    	//Add each song passed to this class to the auCDtect command
    	for(String file:fileList){
    		if(file.toLowerCase().contains("flac")){
    			AudioConverter audioConverter = new AudioConverter();
				String tempWav = "tempWav.wav"+fileList.iterator();
				tempWavList.add(tempWav);
    			try {
					audioConverter.decode(file, tempWav);
				} catch (IOException e) {
					e.printStackTrace();
				}
    			file = tempWav;
    		}
    		command.add(file);
    	}
    	ProcessBuilder processBuilder = new ProcessBuilder(command);

			Process process = null;
			try {
				process = processBuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Set up error stream thread 
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", this); 

			//Set up output stream thread
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", this); 

			// Start error and input stream threads
			new Thread(errorGobbler).start(); 
			new Thread(outputGobbler).start(); 
      }
    
    public void update(double progress, String processingLog, String output, String summary){
    	
    	this.processingLog = processingLog;
    	this.output = output;
    	this.summary = summary;
    	this.progress = progress/(fileList.size());
    	gtk.setOutputUpdated(true);
    	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	if(this.progress == 1){
    		deleteTempTracks();
    	}
    }
    
    public double getProgress(){
    	
    	return progress;
    }
    
    public String getProcessingLog(){
    	
    	return processingLog;
    }
    
    public String getOutput(){
    	
    	return output;
    }
    
    public String getSummary(){
    	
    	return summary;
    }
    
    public void deleteTempTracks(){
    	
		for(String tempFileName:tempWavList){
			File tempFile = new File(tempFileName);
			if(tempFile.exists()){
				tempFile.deleteOnExit();
			}
		}
    }
}



