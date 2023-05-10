/*
 * This class is responsible for generating all of the code for the ImageLoader
 * class for the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class ImageLoaderGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public ImageLoaderGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File ilFile = new File(directory, ("simse\\gui\\ImageLoader.java"));
    if (ilFile.exists()) {
      ilFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(ilFile);
      FileReader reader = new FileReader("resources\\ImageLoader.txt");
      Scanner s = new Scanner(reader);
      
      while (s.hasNextLine()) {
      	  writer.write(s.nextLine() + "\n");
      }
      
      s.close();
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + ilFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}