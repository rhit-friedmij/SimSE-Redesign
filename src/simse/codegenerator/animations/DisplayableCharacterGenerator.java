package simse.codegenerator.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DisplayableCharacterGenerator {

	private File dir;

	public DisplayableCharacterGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File displayableCharacterFile = new File(dir,
	            ("simse\\animation\\DisplayableCharacter.java"));
	    
	        if (displayableCharacterFile.exists()) {
	        	displayableCharacterFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("res\\static\\animations\\DisplayableCharacter.txt");
		FileWriter writer = new FileWriter(displayableCharacterFile);
	
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
