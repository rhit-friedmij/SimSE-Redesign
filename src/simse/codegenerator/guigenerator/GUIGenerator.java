/*
 * This class is responsible for generating all of the code for the SimSEGUI
 * component of the simulation
 */

package simse.codegenerator.guigenerator;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.objectbuilder.WarningListDialog;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.SimSEObject;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.mapeditor.ImageLoader;
import simse.modelbuilder.mapeditor.TileData;
import simse.modelbuilder.mapeditor.UserData;
import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JOptionPane;;

public class GUIGenerator implements CodeGeneratorConstants {
  private ModelOptions options;
  private ImageLoaderGenerator imageLoaderGen; // generates the image loader
  private ClockPanelGenerator clockPanelGen; // generates the clock panel
  private TabPanelGenerator tabPanelGen; // generates the tab panel
  private LogoPanelGenerator logoPanelGen; // generates the logo panel
  private InformationPanelGenerator attPanelGen; // generates the attribute panel
  private EmployeesPanelGenerator actPanelGen; // generates the action panel
  private PopupListenerGenerator popupListGen; // generates the PopupListener
                                               // class
  private DisplayedEmployeeGenerator dispEmpGen; // generates the
                                                 // DisplayedEmployee class
  private MapDataGenerator mapDataGen; // generates the MapData class
  private SimSEMapGenerator ssmGen; // generates the SimSEMap class
  private WorldGenerator worldGen; // generates the world
  private OverviewScreensGenerator glanceFramesGen; // generates the At-A-Glance
                                                    // frames for each object
                                                    // meta-type
  private TableModelGenerator tblModGen; // generates the
                                                        // tables
                                                        // for each object type
  private SimSEAboutDialogGenerator aboutDialogGen; // generates the About
                                                    // Dialog when you click the
                                                    // SimSE logo
  
  private TrackGenerator trackGen;
  
  private MelloGenerator melloGen;
  
  private PanelsGenerator panelsGen;
  
  private InfoScreenGenerator infoGen;
  
  private ObjectGraphPanesGenerator objGraphGen;
  
  private JavaFXHelpersGenerator fxHelpGen;
  
  private RulesInfoScreenGenerator ruleInfoGen;

  public GUIGenerator(ModelOptions options, DefinedObjectTypes objTypes, 
      CreatedObjects objs, DefinedActionTypes acts, Hashtable<SimSEObject, 
      String> stsObjs, Hashtable<SimSEObject, String> ruleObjs, 
      TileData[][] map, ArrayList<UserData> userDatas) {
    this.options = options;
    Hashtable<SimSEObject, String> allObjsToImages = 
    	new Hashtable<SimSEObject, String>();
    allObjsToImages.putAll(stsObjs);
    allObjsToImages.putAll(ruleObjs);
    imageLoaderGen = new ImageLoaderGenerator(
        options.getCodeGenerationDestinationDirectory());
    tabPanelGen = new TabPanelGenerator(objTypes, allObjsToImages, 
        options.getCodeGenerationDestinationDirectory(), 
        options.getIconDirectory());
    clockPanelGen = new ClockPanelGenerator(
        options.getCodeGenerationDestinationDirectory());
    logoPanelGen = new LogoPanelGenerator(
        options.getCodeGenerationDestinationDirectory());
    attPanelGen = new InformationPanelGenerator(objTypes, 
        options.getCodeGenerationDestinationDirectory());
    actPanelGen = new EmployeesPanelGenerator(objTypes, acts, 
        options.getCodeGenerationDestinationDirectory());
    popupListGen = new PopupListenerGenerator(
        options.getCodeGenerationDestinationDirectory());
    dispEmpGen = new DisplayedEmployeeGenerator(
        options.getCodeGenerationDestinationDirectory());
    mapDataGen = new MapDataGenerator(
        options.getCodeGenerationDestinationDirectory());
    ssmGen = new SimSEMapGenerator(objTypes, allObjsToImages, map, userDatas,
        options.getCodeGenerationDestinationDirectory());
    worldGen = new WorldGenerator(
        options.getCodeGenerationDestinationDirectory());
    glanceFramesGen = new OverviewScreensGenerator(objTypes, 
        options.getCodeGenerationDestinationDirectory());
    tblModGen = new TableModelGenerator(objTypes, 
        options.getCodeGenerationDestinationDirectory());
    aboutDialogGen = new SimSEAboutDialogGenerator(
        options.getCodeGenerationDestinationDirectory());
    trackGen = new TrackGenerator(
            options.getCodeGenerationDestinationDirectory());
    melloGen = new MelloGenerator(
    		options.getCodeGenerationDestinationDirectory());
    panelsGen = new PanelsGenerator(
    		options.getCodeGenerationDestinationDirectory());
    infoGen = new InfoScreenGenerator(objTypes,
    		options.getCodeGenerationDestinationDirectory());
    objGraphGen = new ObjectGraphPanesGenerator(objs, objTypes, 
    		options.getCodeGenerationDestinationDirectory());
    fxHelpGen = new JavaFXHelpersGenerator(options.getCodeGenerationDestinationDirectory());
    ruleInfoGen = new RulesInfoScreenGenerator(acts,
    		options.getCodeGenerationDestinationDirectory());
  }

  /*
   * causes all of this component's sub-components to generate code; returns
   * true if no errors, false otherwise
   */
  public boolean generate() {
	  if (!options.getIconDirectory().exists() || 
			  !options.getIconDirectory().isDirectory()) { // icon dir doesn't exist
      Vector<String> warnings = new Vector<String>();
	  	warnings.add(0, "ERROR! Incomplete simulation generated!!");
	  	warnings.add("Cannot find icon directory " + 
	  			options.getIconDirectory().getAbsolutePath());
      new WarningListDialog(warnings, "Code Generation Errors");
      return false;
	  } else {
	    CodeGeneratorUtils.copyDir(options.getIconDirectory().getPath(),
	        options.getCodeGenerationDestinationDirectory().getPath() + 
	            "\\simse\\gui\\icons\\");
	
	    ImageLoader.copyImagesToDir(
	    		options.getCodeGenerationDestinationDirectory().getPath() + 
	    		"\\simse\\gui\\");
	
	    imageLoaderGen.generate();
	    clockPanelGen.generate();
	    tabPanelGen.generate();
	    logoPanelGen.generate();
	    attPanelGen.generate();
	    actPanelGen.generate();
	    popupListGen.generate();
	    dispEmpGen.generate();
	    mapDataGen.generate();
	    ssmGen.generate();
	    worldGen.generate();
	    glanceFramesGen.generate();
	    tblModGen.generate();
	    aboutDialogGen.generate();
	    trackGen.generate();
	    melloGen.generate();
	    panelsGen.generate();
	    infoGen.generate();
	    objGraphGen.generate();
	    fxHelpGen.generate();
	    ruleInfoGen.generate();
	    generateMainGUI();
	    return true;
	  }
  }

  // generates the SimSEGUI class
  private void generateMainGUI() { 
    File mainGUIFile = new File(
        options.getCodeGenerationDestinationDirectory(), 
        ("simse\\gui\\SimSEGUI.java"));
    if (mainGUIFile.exists()) {
      mainGUIFile.delete(); // delete old version of file
    }
    try {
      FileWriter writer = new FileWriter(mainGUIFile);
      String readfile = (!options.getExplanatoryToolAccessOption()) ? "resources\\SimSEGUIFalse.txt" : "resources\\SimSEGUITrue.txt";
      FileReader reader = new FileReader(readfile);
      Scanner s = new Scanner(reader);
      
      while (s.hasNextLine()) {
      	  writer.write(s.nextLine() + "\n");
      }
      
      writer.close();
      s.close();

      writer.close();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + mainGUIFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
}