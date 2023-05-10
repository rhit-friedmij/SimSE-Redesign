package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimSESpriteGenerator {

	private File dir;

	public SimSESpriteGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File simSESpriteFile = new File(dir,
	            ("simse\\animation\\SimSESprite.java"));
	    
	        if (simSESpriteFile.exists()) {
	        	simSESpriteFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("res\\static\\animations\\SimSESprite.txt");
		FileWriter writer = new FileWriter(simSESpriteFile);
	
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
