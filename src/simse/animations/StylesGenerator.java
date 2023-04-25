package simse.animations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StylesGenerator {

	private File dir;
	
	public StylesGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File stylesFile = new File(dir,
	            ("simse\\styles.css"));
	    
	        if (stylesFile.exists()) {
	        	stylesFile.delete(); // delete old version of file
	        }
		
		try {
	        
		FileReader reader = new FileReader("res\\static\\stylesheet\\staticstylesheet.txt");
		FileWriter writer = new FileWriter(stylesFile);
	
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
