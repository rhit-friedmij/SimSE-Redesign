/*
 * This class is responsible for generating all of the code for the 
 * ActionInfoWindow class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class ActionInfoWindowGenerator implements CodeGeneratorConstants {
	private File dir;

	public ActionInfoWindowGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File actionInfoWindowFile = new File(dir,
	            ("simse\\explanatorytool\\ActionInfoWindow.java"));

	        if (actionInfoWindowFile.exists()) {
	        	actionInfoWindowFile.delete(); // delete old version of file
	        }

		try {

		FileReader reader = new FileReader("C:\\Users\\localmgr\\git\\simse-redesign\\simse-redesign\\res\\static\\explanatorytool\\ActionInfoWindow.txt");
		FileWriter writer = new FileWriter(actionInfoWindowFile);

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