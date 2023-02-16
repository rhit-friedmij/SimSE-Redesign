/*
 * This class is responsible for generating all of the code for the
 * MapDataGenerator class for the GUI
 */

package simse.codegenerator.guigenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;;

public class MapDataGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public MapDataGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File mdFile = new File(directory, ("simse\\gui\\MapData.java"));
    if (mdFile.exists()) {
      mdFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(mdFile);
      FileReader reader = new FileReader("resources\\MapData.txt");
      Scanner s = new Scanner(reader);
      
      while (s.hasNext()) {
    	  writer.write(s.next());
      }
      
      s.close();
      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + mdFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}