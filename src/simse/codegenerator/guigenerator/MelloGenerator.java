package simse.codegenerator.guigenerator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class MelloGenerator {
	
	private File directory; // directory to save generated code into

	  public MelloGenerator(File directory) {
	    this.directory = directory;
	  }
	  
	  public void generate() {
		  generateMelloPanel();
		  generateMelloItem();
	  }

	  private void generateMelloPanel() {
	    File mPaneFile = new File(directory, ("simse\\gui\\MelloPanel.java"));
	    if (mPaneFile.exists()) {
	    	mPaneFile.delete(); // delete old version of file
	    }
	    try {
	      FileReader reader = new FileReader("resources\\MelloPanel.txt");
	      Scanner s = new Scanner(reader);
	      FileWriter writer = new FileWriter(mPaneFile);
	      
	      while (s.hasNextLine()) {
	      	  writer.write(s.nextLine() + "\n");
	      }
	      
	      writer.close();
	      s.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + mPaneFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	  }
	  
	  private void generateMelloItem() {
		    File mItemFile = new File(directory, ("simse\\gui\\MelloItem.java"));
		    if (mItemFile.exists()) {
		    	mItemFile.delete(); // delete old version of file
		    }
		    try {
		      FileReader reader = new FileReader("resources\\MelloItem.txt");
		      Scanner s = new Scanner(reader);
		      FileWriter writer = new FileWriter(mItemFile);
		      
		      while (s.hasNextLine()) {
		      	  writer.write(s.nextLine() + "\n");
		      }
		      
		      writer.close();
		      s.close();
		    } catch (IOException e) {
		      JOptionPane.showMessageDialog(null, ("Error writing file "
		          + mItemFile.getPath() + ": " + e.toString()), "File IO Error",
		          JOptionPane.WARNING_MESSAGE);
		    }
		  }

}
