package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PathDataGenerator {

	private File dir;
	
	public PathDataGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File pathDataFile = new File(dir,
	            ("simse\\animation\\PathData.java"));
	    
	        if (pathDataFile.exists()) {
	        	pathDataFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("res\\static\\animations\\PathData.txt");
		FileWriter writer = new FileWriter(pathDataFile);
	
		String fileContents = "";
		int index;
		
		while ((index = reader.read()) != -1) {
			fileContents += (char)index;
		}
		writer.write(fileContents);
		
		reader.close();
		writer.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
