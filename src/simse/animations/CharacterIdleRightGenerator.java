package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterIdleRightGenerator {

	private File dir;
	
	public CharacterIdleRightGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterIdleRightFile = new File(dir,
	            ("animations\\CharacterIdleRight.java"));
	    
	        if (characterIdleRightFile.exists()) {
	        	characterIdleRightFile.delete(); // delete old version of file
	        }
		
	    try {
		
		FileReader reader = new FileReader("res\\static\\animations\\CharacterIdleRight.txt");
		FileWriter writer = new FileWriter(characterIdleRightFile);
	
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
