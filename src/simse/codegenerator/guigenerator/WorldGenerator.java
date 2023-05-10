/*
 * This class is responsible for generating all of the code for the World class
 * for the GUI
 */

package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;;

public class WorldGenerator {

  private File directory; // directory to save generated code into

  public WorldGenerator(File directory) {
    this.directory = directory;
  }

  public void generate() {
    File worldFile = new File(directory, ("simse\\gui\\World.java"));
    if (worldFile.exists()) {
      worldFile.delete(); // delete old version of file
    }
    try {
      FileReader reader = new FileReader("res\\static\\gui\\World.txt");
      Scanner s = new Scanner(reader);
      FileWriter writer = new FileWriter(worldFile);
      
      while (s.hasNextLine()) {
      	  writer.write(s.nextLine() + "\n");
      }
      
      writer.close();
      s.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + worldFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}