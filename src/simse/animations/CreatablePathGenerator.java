package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CreatablePathGenerator {

	private File dir;
	
	public CreatablePathGenerator(File dir) {
		this.dir = dir;
	}

	private void generate() {
		// TODO Auto-generated method stub
	    File creatablePathFile = new File(dir,
	            ("animations\\CreatablePath.java"));
	    
	        if (creatablePathFile.exists()) {
	        	creatablePathFile.delete(); // delete old version of file
	        }
		
	    try {
		
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\CreatablePath.txt");
		FileWriter writer = new FileWriter(creatablePathFile);
	
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
