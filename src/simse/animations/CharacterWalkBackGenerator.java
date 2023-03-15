package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterWalkBackGenerator {

	private File dir;
	
	public CharacterWalkBackGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterWalkBackFile = new File(dir,
	            ("simse\\animations\\CharacterWalkBack.java"));
	    
	        if (characterWalkBackFile.exists()) {
	        	characterWalkBackFile.delete(); // delete old version of file
	        }
		
	    try {
		
		FileReader reader = new FileReader("res\\static\\animations\\CharacterWalkBack.txt");
		FileWriter writer = new FileWriter(characterWalkBackFile);
	
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
