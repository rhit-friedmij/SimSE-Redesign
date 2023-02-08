/*
 * This class is responsible for generating all of the code for the state
 * component of the simulation
 */

package simse.codegenerator.stategenerator;

import simse.codegenerator.CodeGeneratorConstants;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class StateGenerator implements CodeGeneratorConstants {
  private ModelOptions options;
  private ADTGenerator adtGen; // generates ADTs
  private RepositoryGenerator repGen; // generates state repositories
  private ClockGenerator clockGen; // generates clock
  private LoggerGenerator loggerGen; // generates logger

  public StateGenerator(ModelOptions options, DefinedObjectTypes objTypes,
      DefinedActionTypes actTypes) {
    this.options = options;
    adtGen = new ADTGenerator(options, objTypes, actTypes);
    repGen = new RepositoryGenerator(options, objTypes, actTypes);
    clockGen = new ClockGenerator(options);
    loggerGen = new LoggerGenerator(options);
  }

  // causes all of this component's sub-components to generate code
  public void generate() {
    adtGen.generate();
    repGen.generate();
    clockGen.generate();
    loggerGen.generate();

    // generate outer state component:
    File stateFile = new File(options.getCodeGenerationDestinationDirectory(), 
        ("simse\\state\\State.java"));
    if (stateFile.exists()) {
      stateFile.delete(); // delete old version of file
    }
    try {
    	FileReader reader = new FileReader(new File("src/simse/codegenerator/"
    			+ "resources/State.txt"));
    	FileWriter writer = new FileWriter(stateFile);
    	char[] buf = new char[256];
    	int length;
    	while ((length = reader.read(buf)) > 0) {
    		writer.write(buf, 0, length);
    	}
    	reader.close();
    	writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + stateFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}