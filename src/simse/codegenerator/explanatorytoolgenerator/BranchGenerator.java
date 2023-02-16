/*
 * This class is responsible for generating all of the code for the Branch
 * class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class BranchGenerator implements CodeGeneratorConstants {
	private File dir;

	public BranchGenerator(File dir) {
		this.dir = dir;
	}

	public void generate() {
		// TODO Auto-generated method stub
	    File branchFile = new File(dir,
	            ("simse\\explanatorytool\\Branch.java"));

	        if (branchFile.exists()) {
	        	branchFile.delete(); // delete old version of file
	        }

		try {

		FileReader reader = new FileReader("res\\static\\explanatorytool\\Branch.txt");
		FileWriter writer = new FileWriter(branchFile);

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
