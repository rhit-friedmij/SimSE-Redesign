/*
 * This class is responsible for generating all of the code for the Clock panel
 * in the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class ClockPanelGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public ClockPanelGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File clockPanelFile = new File(directory, ("simse\\gui\\ClockPanel.java"));
    if (clockPanelFile.exists()) {
      clockPanelFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(clockPanelFile);
      FileReader reader = new FileReader("resources\\ClockPanel.txt");
      Scanner s = new Scanner(reader);
      
      while (s.hasNextLine()) {
      	  writer.write(s.nextLine() + "\n");
      }
      
      s.close();
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + clockPanelFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}