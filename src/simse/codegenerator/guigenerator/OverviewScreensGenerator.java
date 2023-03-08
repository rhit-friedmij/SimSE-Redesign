/*
 * This class is responsible for generating all of the code for the different
 * At-A-Glance Frames in the GUI
 */

package simse.codegenerator.guigenerator;

import simse.modelbuilder.objectbuilder.Attribute;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.NumericalAttribute;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import java.util.Scanner;
import java.util.Vector;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

public class OverviewScreensGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into
  private DefinedObjectTypes objTypes; // holds all of the defined object types
                                       // from an sso file

  public OverviewScreensGenerator(DefinedObjectTypes objTypes, File directory) {
    this.objTypes = objTypes;
    this.directory = directory;
  }

  public void generate() {
	  generateEmployeesOverviewScreen();
	  generateArtifactsOverviewScreen();
	  generateProjectOverviewScreen();
  }
  
  private void generateEmployeesOverviewScreen() {
	  File eosFile = new File(directory, ("simse\\gui\\EmployeesOverviewScreen.java"));
	    if (eosFile.exists()) {
	      eosFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\EmployeesOverviewScreen.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(eosFile);
	      
	      while (s.hasNextLine()) {
	      	  writer.write(s.nextLine() + "\n");
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + eosFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
  }
  
  private void generateArtifactsOverviewScreen() {
	  File aosFile = new File(directory, ("simse\\gui\\ArtifactsOverviewScreen.java"));
	    if (aosFile.exists()) {
	      aosFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\ArtifactsOverviewScreen.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(aosFile);
	      
	      while (s.hasNextLine()) {
	      	  writer.write(s.nextLine() + "\n");
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + aosFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
  }
  
  private void generateProjectOverviewScreen() {
	  File posFile = new File(directory, ("simse\\gui\\ProjectOverviewScreen.java"));
	    if (posFile.exists()) {
	      posFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\ProjectOverviewScreen.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(posFile);
	      
	      while (s.hasNextLine()) {
	      	  writer.write(s.nextLine() + "\n");
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + posFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
  }


}