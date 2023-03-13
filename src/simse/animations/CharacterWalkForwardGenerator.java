package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterWalkForwardGenerator {

	private File dir;
	
	public CharacterWalkForwardGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterWalkForwardFile = new File(dir,
	            ("animations\\CharacterWalkForward.java"));
	    
	        if (characterWalkForwardFile.exists()) {
	        	characterWalkForwardFile.delete(); // delete old version of file
	        }
		
		try { 
			
		FileReader reader = new FileReader("res\\static\\animations\\CharacterWalkForward.txt");
		FileWriter writer = new FileWriter(characterWalkForwardFile);
	
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
