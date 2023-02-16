package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class PanelsGenerator {
	
	private File directory; // directory to save generated code into

	  public PanelsGenerator(File directory) {
	    this.directory = directory;
	  }
	  
	  public void generate() {
		  generatePanelEnum();
		  generatePanelInterface();
		  generatePanelScreen();
	  }

	  private void generatePanelEnum() {
	    File panelFile = new File(directory, ("simse\\gui\\Panels.java"));
	    if (panelFile.exists()) {
	    	panelFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\Panels.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(panelFile);
	      
	      while (s.hasNext()) {
	    	  writer.write(s.next());
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + panelFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private void generatePanelInterface() {
		    File simSEPanelFile = new File(directory, ("simse\\gui\\SimSEPanel.java"));
		    if (simSEPanelFile.exists()) {
		    	simSEPanelFile.delete(); // delete old version of file
		    }
		    try {
		      FileReader reader = new FileReader("resources\\SimSEPanel.txt");
		      Scanner s = new Scanner(reader);
		      FileWriter writer = new FileWriter(simSEPanelFile);
		      
		      while (s.hasNext()) {
		    	  writer.write(s.next());
		      }
		      
		      writer.close();
		      s.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + simSEPanelFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
		    }
		  }
	  
	  private void generatePanelScreen() {
		    File panelScreenFile = new File(directory, ("simse\\gui\\PanelsScreen.java"));
		    if (panelScreenFile.exists()) {
		    	panelScreenFile.delete(); // delete old version of file
		    }
		    try {
		      FileReader reader = new FileReader("resources\\PanelsScreen.txt");
		      Scanner s = new Scanner(reader);
		      FileWriter writer = new FileWriter(panelScreenFile);
		      
		      while (s.hasNext()) {
		    	  writer.write(s.next());
		      }
		      
		      writer.close();
		      s.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + panelScreenFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
		    }
		  }

}
