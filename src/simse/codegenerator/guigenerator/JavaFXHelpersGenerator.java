package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

public class JavaFXHelpersGenerator {
	  private File directory; // directory to generate into

	  public JavaFXHelpersGenerator(File dir) {
	    directory = dir;
	  }

	  public void generate() {
	    File fxFile = new File(directory, ("simse\\gui\\util\\JavaFXHelpers.java"));
	    if (fxFile.exists()) {
	    	fxFile.delete(); // delete old version of file
	    }
	    try {
	    	FileReader reader = new FileReader("res\\static\\gui\\JavaFXHelpers.txt");
	    	FileWriter writer = new FileWriter(fxFile);
	    	char[] buf = new char[256];
	    	int length;
	    	while ((length = reader.read(buf)) > 0) {
	    		writer.write(buf, 0, length);
	    	}
	    	reader.close();
	    	writer.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + fxFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
}
