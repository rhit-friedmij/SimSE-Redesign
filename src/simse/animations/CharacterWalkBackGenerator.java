package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterWalkBackGenerator {

	private File dir;
	
	public static void main(String[] args) {
		File directory = new File("C:\\Users\\localmgr\\Documents\\TestFiles");
		CharacterWalkBackGenerator gen = new CharacterWalkBackGenerator(directory);
	}
	
	public CharacterWalkBackGenerator(File dir) {
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
	    File characterWalkBackFile = new File(dir,
	            ("animations\\CharacterWalkBack.java"));
	    
	        if (characterWalkBackFile.exists()) {
	        	characterWalkBackFile.delete(); // delete old version of file
	        }
		
		
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\CharacterWalkBack.txt");
		FileWriter writer = new FileWriter(characterWalkBackFile);
	
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
