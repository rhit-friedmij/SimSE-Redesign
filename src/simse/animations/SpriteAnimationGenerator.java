package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SpriteAnimationGenerator {

	private File dir;
	
	public static void main(String[] args) {
		File directory = new File("C:\\Users\\localmgr\\Documents\\TestFiles");
		SpriteAnimationGenerator gen = new SpriteAnimationGenerator(directory);
	}
	
	public SpriteAnimationGenerator(File dir) {
		this.dir = dir;
		try {
			generate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generate() throws IOException {
		// TODO Auto-generated method stub
	    File spriteAnimationFile = new File(dir,
	            ("animations\\SpriteAnimation.java"));
	    
	        if (spriteAnimationFile.exists()) {
	        	spriteAnimationFile.delete(); // delete old version of file
	        }
		
		
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\SpriteAnimation.txt");
		FileWriter writer = new FileWriter(spriteAnimationFile);
	
		String fileContents = "";
		int index;
		
		while ((index = reader.read()) != -1) {
			fileContents += (char)index;
		}
		writer.write(fileContents);
		
		reader.close();
		writer.close();
		
	}
	
}
