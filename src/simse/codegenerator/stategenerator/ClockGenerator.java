/*
 * This class is responsible for generating all of the code for the state's
 * clock component
 */

package simse.codegenerator.stategenerator;

import simse.codegenerator.CodeGeneratorConstants;

import simse.modelbuilder.ModelOptions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class ClockGenerator implements CodeGeneratorConstants {
  private File directory; // directory to generate into

  public ClockGenerator(ModelOptions options) {
    directory = options.getCodeGenerationDestinationDirectory();
  }

  public void generate() {
    File clockFile = new File(directory, ("simse\\state\\Clock.java"));
    if (clockFile.exists()) {
      clockFile.delete(); // delete old version of file
    }
    try {
    	FileReader reader = new FileReader(new File("src/simse/codegenerator/"
    			+ "resources/Clock.txt"));
    	FileWriter writer = new FileWriter(clockFile);
    	char[] buf = new char[256];
    	int length;
    	while ((length = reader.read(buf)) > 0) {
    		writer.write(buf, 0, length);
    	}
    	reader.close();
    	writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + clockFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}