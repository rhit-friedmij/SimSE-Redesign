/*
 * This class is responsible for generating all of the code for the state's
 * logger component
 */

package simse.codegenerator.stategenerator;

import simse.codegenerator.CodeGeneratorConstants;

import simse.modelbuilder.ModelOptions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class LoggerGenerator implements CodeGeneratorConstants {
  private File directory; // directory to generate into

  public LoggerGenerator(ModelOptions options) {
    directory = options.getCodeGenerationDestinationDirectory();
  }

  public void generate() {
    File loggerFile = new File(directory, 
    		("simse\\state\\Logger\\Logger.java"));
    if (loggerFile.exists()) {
      loggerFile.delete(); // delete old version of file
    }
    try {
    	FileReader reader = new FileReader(new File("src/simse/codegenerator/"
    			+ "resources/Logger.txt"));
    	FileWriter writer = new FileWriter(loggerFile);
    	char[] buf = new char[256];
    	int length;
    	while ((length = reader.read(buf)) > 0) {
    		writer.write(buf, 0, length);
    	}
    	reader.close();
    	writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + loggerFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}