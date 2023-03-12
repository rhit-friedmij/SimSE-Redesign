package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterIdleFrontGenerator {

	private File dir;
	
	public CharacterIdleFrontGenerator(File dir) {
		this.dir = dir;

	}

	public void generate() {
		// TODO Auto-generated method stub
	    File characterIdleFrontFile = new File(dir,
	            ("animations\\CharacterIdleFront.java"));
	    
	        if (characterIdleFrontFile.exists()) {
	        	characterIdleFrontFile.delete(); // delete old version of file
	        }
	        
	    
		try {
		
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\CharacterIdleFront.txt");
		FileWriter writer = new FileWriter(characterIdleFrontFile);
	
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
