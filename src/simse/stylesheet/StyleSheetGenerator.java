package simse.stylesheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StyleSheetGenerator {

	private File dir;
	
	public StyleSheetGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File stylesheetFile = new File(dir,
	            ("style.css"));
	    
	        if (stylesheetFile.exists()) {
	        	stylesheetFile.delete(); // delete old version of file
	        }
		
		
		FileReader reader = null;
		FileWriter writer = null;
		try {
			reader = new FileReader("res\\static\\stylesheet\\staticstylesheet.txt");
			writer = new FileWriter(stylesheetFile);

		
	
		String fileContents = "";
		int index;
		
		while ((index = reader.read()) != -1) {
			fileContents += (char)index;
		}
		writer.write(fileContents);
		
		reader.close();
		writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
