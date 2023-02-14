package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DisplayableCharacterGenerator {

	private File dir;
	
	public static void main(String[] args) {
		File directory = new File("C:\\Users\\localmgr\\Documents\\TestFiles");
		DisplayableCharacterGenerator gen = new DisplayableCharacterGenerator(directory);
	}
	
	public DisplayableCharacterGenerator(File dir) {
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
	    File displayableCharacterFile = new File(dir,
	            ("animations\\DisplayableCharacter.java"));
	    
	        if (displayableCharacterFile.exists()) {
	        	displayableCharacterFile.delete(); // delete old version of file
	        }
		
		
		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\animations\\DisplayableCharacter.txt");
		FileWriter writer = new FileWriter(displayableCharacterFile);
	
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
