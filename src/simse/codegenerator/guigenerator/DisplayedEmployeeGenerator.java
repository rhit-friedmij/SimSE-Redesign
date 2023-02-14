/*
 * This class is responsible for generating all of the code for the
 * DisplayedEmployee class for the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class DisplayedEmployeeGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public DisplayedEmployeeGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File deFile = new File(directory, ("simse\\gui\\DisplayedEmployee.java"));
    if (deFile.exists()) {
      deFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(deFile);
      FileReader reader = new FileReader("\\resources\\DisplayedEmployee.txt");
      Scanner s = new Scanner(reader);
      
      while (s.hasNext()) {
    	  writer.write(s.next());
      }
      
      s.close();
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + deFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}