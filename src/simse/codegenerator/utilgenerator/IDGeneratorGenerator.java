/*
 * This class is responsible for generating all of the code for the IDGenerator
 * class
 */

package simse.codegenerator.utilgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import simse.modelbuilder.ModelOptions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class IDGeneratorGenerator implements CodeGeneratorConstants {
	private File dir;

	public IDGeneratorGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File idGeneratorFile = new File(dir,
	            ("simse\\util\\IDGenerator.java"));

	        if (idGeneratorFile.exists()) {
	        	idGeneratorFile.delete(); // delete old version of file
	        }

		try {

		FileReader reader = new FileReader("res\\static\\util\\IDGenerator.txt");
		FileWriter writer = new FileWriter(idGeneratorFile);

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