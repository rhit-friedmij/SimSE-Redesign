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
import java.io.FileWriter;
import java.io.IOException;

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

    ClassName stage = ClassName.get("javafx.stage", "Stage");
    ClassName state = ClassName.get("simse.state", "State");
    ClassName menuInput = ClassName.get("simse.logic", "MenuInputManager");
    ClassName trigCheck = ClassName.get("simse.logic", "TriggerChecker");
    ClassName destCheck = ClassName.get("simse.logic", "DestroyerChecker");
    ClassName ruleExecuter = ClassName.get("simse.logic", "RuleExecuter");
    ClassName miscUpdater = ClassName.get("simse.logic", "MiscUpdater");
    
    MethodSpec logicConstructor = MethodSpec.constructorBuilder()
			.addModifiers(Modifier.PUBLIC)
			.addParameter(state, "s")
			.addStatement("state = s")
			.addStatement("updater = new MiscUpdater(state)")
			.addStatement("ruleEx = new RuleExecutor(state)")
			.addStatement("trigChecker = new TriggerChecker(state, ruleEx)")
			.addStatement("destChecker = new DestroyerChecker(state, ruleEx, trigChecker)")
			.addStatement("ruleEx.setTriggerChecker(trigChecker)")
			.addStatement("ruleEx.setDestroyerChecker(destChecker)")
			.addStatement("menInputMgr = new MenuInputManager(state, trigChecker, destChecker, ruleEx)")
			.build();
    
    MethodSpec menu = MethodSpec.methodBuilder("getMenuInputManager")
			.addModifiers(Modifier.PUBLIC)
			.returns(menuInput)
			.addStatement("return menInputMgr")
			.build();
    
    MethodSpec trigger = MethodSpec.methodBuilder("getTriggerChecker")
			.addModifiers(Modifier.PUBLIC)
			.returns(trigCheck)
			.addStatement("return trigChecker")
			.build();
    
    MethodSpec destroyer = MethodSpec.methodBuilder("getDestroyerChecker")
			.addModifiers(Modifier.PUBLIC)
			.returns(destCheck)
			.addStatement("return destChecker")
			.build();
    
    MethodSpec update = MethodSpec.methodBuilder("update")
			.addModifiers(Modifier.PUBLIC)
			.returns(void.class)
			.addParameter(stage, "mainGUI")
			.addStatement("updater.update()")
			.addStatement("trigChecker.update(false, mainGUI)")
			.addStatement("ruleEx.update(mainGUI, $T.UPDATE_ALL_CONTINUOUS, null, null)", ruleExecuter)
			.addStatement("destChecker.update(false, mainGUI)")
			.build();
    
    TypeSpec logic = TypeSpec.classBuilder("Logic")
			.addModifiers(Modifier.PUBLIC)
			.addField(state, "state", Modifier.PRIVATE)
			.addField(miscUpdater, "updater", Modifier.PRIVATE)
			.addField(trigCheck, "trigChecker", Modifier.PRIVATE)
			.addField(destCheck, "destChecker", Modifier.PRIVATE)
			.addField(menuInput, "menInputMgr", Modifier.PRIVATE)
			.addField(ruleExecuter, "ruleEx", Modifier.PRIVATE)
			.addMethod(logicConstructor)
			.addMethod(menu)
			.addMethod(trigger)
			.addMethod(destroyer)
			.addMethod(update)
			.build();
    
    JavaFile javaFile = JavaFile.builder("simse.logic", logic)
			.addFileComment("File generated by: simse.codegenerator.logicgenerator.LogicGenerator")
			.build();
    
    // generate outer logic component:
    try {
    	logicFile = new File(directory, ("simse\\logic\\Logic.java"));
        if (logicFile.exists()) {
          logicFile.delete(); // delete old version of file
        }
        FileWriter writer = new FileWriter(logicFile);
        System.out.println(javaFile.toString());
		javaFile.writeTo(writer);
		writer.close();
		return success;
    } catch (IOException e) {
    	JOptionPane.showMessageDialog(null, ("Error writing file " + logicFile.getPath() + ": " + e.toString()), 
    		  "File IO Error", JOptionPane.WARNING_MESSAGE);
    	return false;
    }
  }
}