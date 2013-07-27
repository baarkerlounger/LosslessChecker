//Processes the output of the auCDtect command line program
import java.io.InputStream;
import java.util.Scanner;

public class StreamGobbler implements Runnable { 
	
	InputStream inputStream; 
	String type;
	private String processingLog = null;
	private String output = null;
	private String summary = null;
	protected boolean finished = false;
	private auCDtect auCDtect;
	private int progress = 0;

	StreamGobbler(InputStream inputStream, String type, auCDtect auCDtect){this.inputStream = inputStream; this.type = type; this.auCDtect = auCDtect;} 

	public void run(){ 

		Scanner scanner = new Scanner(inputStream);

		while (((scanner.hasNextLine())&&(type == "OUTPUT"))){

			String line = scanner.nextLine();
			
			if(line.contains("Processing file:")){
				processingLog = line.substring(line.indexOf("P"), (line.indexOf("]")+1));
			}
			if(line.contains("This track looks like")){
				output = line.substring(line.indexOf("This track"), (line.indexOf("%")+1));
				progress = progress + 1;
			}
			if(line.contains("These")){
				summary = line.substring(line.indexOf("These tracks"));
			}
			
			if((type == "OUTPUT")&&(progress > 0)){

				auCDtect.update(progress, processingLog, output, summary);
				processingLog = null;
				output = null;
				summary = null;
			}
		}
		
	}
}

