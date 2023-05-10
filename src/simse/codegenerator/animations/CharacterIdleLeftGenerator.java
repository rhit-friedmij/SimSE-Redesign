package simse.codegenerator.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterIdleLeftGenerator {

	private File dir;
	
	public CharacterIdleLeftGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterIdleLeftFile = new File(dir,
	            ("simse\\animation\\CharacterIdleLeft.java"));
	    
	        if (characterIdleLeftFile.exists()) {
	        	characterIdleLeftFile.delete(); // delete old version of file
	        }
		
		try {
			
		FileReader reader = new FileReader("res\\static\\animations\\CharacterIdleLeft.txt");
		FileWriter writer = new FileWriter(characterIdleLeftFile);
	
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
