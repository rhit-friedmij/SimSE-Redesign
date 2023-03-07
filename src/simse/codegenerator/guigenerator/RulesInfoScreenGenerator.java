package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class RulesInfoScreenGenerator {
	
	private File directory; // directory to save generated code into

	  public RulesInfoScreenGenerator(File directory) {
	    this.directory = directory;
	  }

	  public void generate() {
	    File worldFile = new File(directory, ("simse\\gui\\RulesInfoScreen.java"));
	    if (worldFile.exists()) {
	      worldFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\RulesInfoScreen.txt");
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
