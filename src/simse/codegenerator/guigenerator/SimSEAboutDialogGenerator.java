/*
 * This class is responsible for generating all of the code for the SimSE About
 * Dialog in the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class SimSEAboutDialogGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public SimSEAboutDialogGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File aboutDialogFile = new File(directory,
        ("simse\\gui\\SimSEAboutDialog.java"));
    if (aboutDialogFile.exists()) {
      aboutDialogFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(aboutDialogFile);
      FileReader reader = new FileReader("resources\\SimSeAboutDialog.txt");
      Scanner s = new Scanner(reader);
      
      while (s.hasNextLine()) {
      	  writer.write(s.nextLine() + "\n");
      }
      
      writer.close();
      s.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + aboutDialogFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}