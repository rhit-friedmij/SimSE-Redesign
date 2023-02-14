package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterIdleLeftGenerator {

	private File dir;
	
	public CharacterIdleLeftGenerator(File dir) {
		this.dir = dir;
	}

	private void generate() {
		// TODO Auto-generated method stub
	    File characterIdleLeftFile = new File(dir,
	            ("animations\\CharacterIdleLeft.java"));
	    
	        if (characterIdleLeftFile.exists()) {
	        	characterIdleLeftFile.delete(); // delete old version of file
	        }
		
		try {
			
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\CharacterIdleLeft.txt");
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
