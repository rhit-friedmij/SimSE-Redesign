package simse.codegenerator.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterWalkRightGenerator {

	private File dir;
	
	public CharacterWalkRightGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterWalkRightFile = new File(dir,
	            ("simse\\animation\\CharacterWalkRight.java"));
	    
	        if (characterWalkRightFile.exists()) {
	        	characterWalkRightFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("res\\static\\animations\\CharacterWalkRight.txt");
		FileWriter writer = new FileWriter(characterWalkRightFile);
	
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
