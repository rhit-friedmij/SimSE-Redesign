package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacteWalkRightGenerator {

	private File dir;
	
	public CharacteWalkRightGenerator(File dir) {
		this.dir = dir;
	}

	private void generate() {
		// TODO Auto-generated method stub
	    File characterWalkRightFile = new File(dir,
	            ("animations\\CharacterWalkRight.java"));
	    
	        if (characterWalkRightFile.exists()) {
	        	characterWalkRightFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\CharacterWalkRight.txt");
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
