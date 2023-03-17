package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterWalkLeftGenerator {

	private File dir;
	
	public CharacterWalkLeftGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterWalkLeftFile = new File(dir,
	            ("animations\\CharacterWalkLeft.java"));
	    
	        if (characterWalkLeftFile.exists()) {
	        	characterWalkLeftFile.delete(); // delete old version of file
	        }
		
	    try {
		
		FileReader reader = new FileReader("res\\static\\animations\\CharacterWalkLeft.txt");
		FileWriter writer = new FileWriter(characterWalkLeftFile);
	
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
