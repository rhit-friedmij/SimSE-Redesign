/*
 * This class is responsible for generating all of the code for the 
 * MultipleTimelinesBrowser class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class MultipleTimelinesBrowserGenerator implements 
CodeGeneratorConstants {
	private File dir;

	public MultipleTimelinesBrowserGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File multipleTimelinesBrowserFile = new File(dir,
	            ("simse\\explanatorytool\\MultipleTimelinesBrowser.java"));

	        if (multipleTimelinesBrowserFile.exists()) {
	        	multipleTimelinesBrowserFile.delete(); // delete old version of file
	        }

		try {

		FileReader reader = new FileReader("res\\static\\explanatorytool\\MultipleTimelinesBrowser.txt");
		FileWriter writer = new FileWriter(multipleTimelinesBrowserFile);

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
