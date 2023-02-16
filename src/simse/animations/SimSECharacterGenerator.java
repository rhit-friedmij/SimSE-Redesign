package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimSECharacterGenerator {

	private File dir;
	
	public SimSECharacterGenerator(File dir) {
		this.dir = dir;
	}

	private void generate() {
		// TODO Auto-generated method stub
	    File simSECharacterFile = new File(dir,
	            ("animations\\SimSECharacter.java"));
	    
	        if (simSECharacterFile.exists()) {
	        	simSECharacterFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\SimSECharacter.txt");
		FileWriter writer = new FileWriter(simSECharacterFile);
	
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
