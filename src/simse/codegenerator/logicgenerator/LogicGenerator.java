/*
 * This class is responsible for generating all of the code for the logic
 * component of the simulation
 */

package simse.codegenerator.logicgenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.logicgenerator.dialoggenerator.ChooseActionToDestroyDialogGenerator;
import simse.codegenerator.logicgenerator.dialoggenerator.ChooseActionToJoinDialogGenerator;
import simse.codegenerator.logicgenerator.dialoggenerator.ChooseRoleToPlayDialogGenerator;
import simse.codegenerator.logicgenerator.dialoggenerator.EmployeeParticipantSelectionDialogGenerator;
import simse.codegenerator.logicgenerator.dialoggenerator.NonEmployeeParticipantSelectionDialogGenerator;
import simse.codegenerator.logicgenerator.dialoggenerator.ParticipantSelectionDialogsDriverGenerator;

import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class LogicGenerator implements CodeGeneratorConstants {
  private MiscUpdaterGenerator miscUGen; // generates MiscUpdater component
  private TriggerCheckerGenerator trigGen; // generates trigger checker component
  private DestroyerCheckerGenerator destGen; // generates destroyer checker component
  private MenuInputManagerGenerator menuGen; // generates menu input manager component
  private EmployeeParticipantSelectionDialogGenerator epsdGen; // generates employee participant selection dialogs
  private NonEmployeeParticipantSelectionDialogGenerator nepsdGen; // generates non-employee participant selection dialogs
  private ParticipantSelectionDialogsDriverGenerator psdDriverGen; // generates participant selection dialogs driver
  private ChooseActionToDestroyDialogGenerator catddGen; // generates choose action to destroy dialog
  private ChooseActionToJoinDialogGenerator catjdGen; // generates choose action to join dialog
  private ChooseRoleToPlayDialogGenerator crtpdGen; // generates choose role to play dialog
  private RuleExecutorGenerator ruleGen; // generates rule executor component
  private File directory; // directory to generate into
  private File logicFile; // file to generate

  public LogicGenerator(ModelOptions options, DefinedObjectTypes objTypes, DefinedActionTypes actTypes) {
    directory = options.getCodeGenerationDestinationDirectory();
    miscUGen = new MiscUpdaterGenerator(directory, actTypes);
    trigGen = new TriggerCheckerGenerator(actTypes, directory);
    destGen = new DestroyerCheckerGenerator(actTypes, directory);
    menuGen = new MenuInputManagerGenerator(options, actTypes, objTypes, directory);
    epsdGen = new EmployeeParticipantSelectionDialogGenerator(actTypes, objTypes, directory);
    nepsdGen = new NonEmployeeParticipantSelectionDialogGenerator(actTypes, objTypes, directory);
    psdDriverGen = new ParticipantSelectionDialogsDriverGenerator(actTypes, directory);
    catddGen = new ChooseActionToDestroyDialogGenerator(actTypes, directory);
    catjdGen = new ChooseActionToJoinDialogGenerator(actTypes, directory);
    crtpdGen = new ChooseRoleToPlayDialogGenerator(actTypes, directory);
    ruleGen = new RuleExecutorGenerator(actTypes, directory);
  }

  /*
   * causes all of this component's sub-components to generate code; returns
   * true if generation successful, false otherwise
   */
  public boolean generate() { 
    miscUGen.generate();
    trigGen.generate();
    destGen.generate();
    menuGen.generate();
    epsdGen.generate();
    nepsdGen.generate();
    psdDriverGen.generate();
    catddGen.generate();
    catjdGen.generate();
    crtpdGen.generate();
    boolean success = ruleGen.generate();

     
    // generate outer logic component:
    try {    
    	logicFile = new File(directory, ("simse\\logic\\Logic.java"));
        if (logicFile.exists()) {
          logicFile.delete(); // delete old version of file
        }
        FileWriter writer = new FileWriter(logicFile);
        FileReader reader = new FileReader("simse\\codegenerator\\resources\\Logic.txt");
        Scanner s = new Scanner(reader);
        
        while (s.hasNext()) {
      	  writer.write(s.next());
        }
        
        s.close();
		writer.close();
		return success;
    } catch (IOException e) {
    	JOptionPane.showMessageDialog(null, ("Error writing file " + logicFile.getPath() + ": " + e.toString()), 
    		  "File IO Error", JOptionPane.WARNING_MESSAGE);
    	return false;
    }
  }
}