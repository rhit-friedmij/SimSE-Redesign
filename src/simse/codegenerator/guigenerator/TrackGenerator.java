package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;;

public class TrackGenerator {
	
	private File directory; // directory to save generated code into

	  public TrackGenerator(File directory) {
	    this.directory = directory;
	  }
	  
	  public void generate() {
		  generateTrackPanel();
		  generateTrackMessage();
	  }

	  private void generateTrackPanel() {
	    File tPaneFile = new File(directory, ("simse\\gui\\TrackPanel.java"));
	    if (tPaneFile.exists()) {
	    	tPaneFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\TrackPanel.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(tPaneFile);
	      
	      while (s.hasNext()) {
	    	  writer.write(s.next());
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + tPaneFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private void generateTrackMessage() {
		    File tItemFile = new File(directory, ("simse\\gui\\TrackMessage.java"));
		    if (tItemFile.exists()) {
		    	tItemFile.delete(); // delete old version of file
		    }
		    try {
		      FileReader reader = new FileReader("resources\\TrackMessage.txt");
		      Scanner s = new Scanner(reader);
		      FileWriter writer = new FileWriter(tItemFile);
		      
		      while (s.hasNext()) {
		    	  writer.write(s.next());
		      }
		      
		      writer.close();
		      s.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + tItemFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
		    }
		  }

}
