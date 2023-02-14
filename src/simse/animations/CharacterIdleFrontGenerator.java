package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CharacterIdleFrontGenerator {

	private File dir;
	
	public static void main(String[] args) {
		File directory = new File("C:\\Users\\localmgr\\Documents\\TestFiles");
		CharacterIdleFrontGenerator gen = new CharacterIdleFrontGenerator(directory);
	}
	
	public CharacterIdleFrontGenerator(File dir) {
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
	    File characterIdleFrontFile = new File(dir,
	            ("animations\\CharacterIdleFront.java"));
	    
	        if (characterIdleFrontFile.exists()) {
	        	characterIdleFrontFile.delete(); // delete old version of file
	        }
		
		
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
		
	}
	
}
