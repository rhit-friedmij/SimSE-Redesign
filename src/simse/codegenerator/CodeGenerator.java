/* This class is responsible for generating all of the code for the simulation */

package simse.codegenerator;

import simse.animations.CharacterIdleBackGenerator;
import simse.animations.CharacterIdleFrontGenerator;
import simse.animations.CharacterIdleLeftGenerator;
import simse.animations.CharacterIdleRightGenerator;
import simse.animations.CharacterWalkBackGenerator;
import simse.animations.CharacterWalkForwardGenerator;
import simse.animations.CharacterWalkLeftGenerator;
import simse.animations.CharacterWalkRightGenerator;
import simse.animations.CreatablePathGenerator;
import simse.animations.DisplayableCharacterGenerator;
import simse.animations.PathDataGenerator;
import simse.animations.SimSECharacterGenerator;
import simse.animations.SimSESpriteGenerator;
import simse.animations.SpriteAnimationGenerator;
import simse.codegenerator.enginegenerator.EngineGenerator;
import simse.codegenerator.explanatorytoolgenerator.ExplanatoryToolGenerator;
import simse.codegenerator.guigenerator.GUIGenerator;
import simse.codegenerator.logicgenerator.LogicGenerator;
import simse.codegenerator.stategenerator.StateGenerator;
import simse.codegenerator.utilgenerator.IDGeneratorGenerator;
import simse.codegenerator.utilgenerator.RuleCategoriesGenerator;
import simse.codegenerator.utilgenerator.RuleTypeGenerator;
import simse.modelbuilder.ModelOptions;
import simse.modelbuilder.objectbuilder.DefinedObjectTypes;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.startstatebuilder.CreatedObjects;
import simse.modelbuilder.startstatebuilder.SimSEObject;
import simse.modelbuilder.mapeditor.TileData;
import simse.modelbuilder.mapeditor.UserData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class CodeGenerator {
	public static boolean allowHireFire = false;
	
  private final char NEWLINE = '\n';
  private final char OPEN_BRACK = '{';
  private final char CLOSED_BRACK = '}';

  private ModelOptions options;
  
  private StateGenerator stateGen; // generates the state component
  private LogicGenerator logicGen; // generates the logic component
  private EngineGenerator engineGen; // generates the engine component
  private GUIGenerator guiGen; // generates the GUI component
  private ExplanatoryToolGenerator expToolGen; // generates the explanatory 
  																						 // tool
  private IDGeneratorGenerator idGen; // generates the IDGenerator
  private RuleCategoriesGenerator ruleCateGen;
  private RuleTypeGenerator ruleTypeGen;
  
  private CharacterIdleBackGenerator characterIdleBackGen;
  private CharacterIdleFrontGenerator characterIdleFrontGen;
  private CharacterIdleLeftGenerator characterIdleLeftGen;
  private CharacterIdleRightGenerator characterIdleRightGen;
  private CharacterWalkForwardGenerator characterWalkForwardGen;
  private CharacterWalkLeftGenerator characterWalkLeftGen;
  private CharacterWalkRightGenerator characterWalkRightGen;
  private CharacterWalkBackGenerator characterWalkBackGen;
  private CreatablePathGenerator creatablePathGen;
  private DisplayableCharacterGenerator displayableCharacterGen;
  private PathDataGenerator pathDataGen;
  private SimSECharacterGenerator simSECharacterGen;
  private SimSESpriteGenerator simSESpriteGen;
  private SpriteAnimationGenerator spriteAnimGen;

  public CodeGenerator(ModelOptions options, DefinedObjectTypes objTypes, 
      CreatedObjects objs, DefinedActionTypes actTypes, 
      Hashtable<SimSEObject, String> stsObjsToImages, 
      Hashtable<SimSEObject, String> ruleObjsToImages, TileData[][] map,
      ArrayList<UserData> userDatas) {
    this.options = options;
    stateGen = new StateGenerator(options, objTypes, actTypes);
    logicGen = new LogicGenerator(options, objTypes, actTypes);
    engineGen = new EngineGenerator(options, objs);
    guiGen = new GUIGenerator(options, objTypes, objs, actTypes, 
        stsObjsToImages, ruleObjsToImages, map, userDatas);
    expToolGen = new ExplanatoryToolGenerator(options, objTypes, objs, 
        actTypes);
    idGen = new IDGeneratorGenerator(options.getCodeGenerationDestinationDirectory());
    ruleCateGen = new RuleCategoriesGenerator(options.getCodeGenerationDestinationDirectory(), actTypes);
    ruleTypeGen = new RuleTypeGenerator(options.getCodeGenerationDestinationDirectory());
    characterIdleBackGen = new CharacterIdleBackGenerator(options.getCodeGenerationDestinationDirectory());
    characterIdleFrontGen = new CharacterIdleFrontGenerator(options.getCodeGenerationDestinationDirectory());
    characterIdleLeftGen = new CharacterIdleLeftGenerator(options.getCodeGenerationDestinationDirectory());
    characterIdleRightGen = new CharacterIdleRightGenerator(options.getCodeGenerationDestinationDirectory());
    characterWalkForwardGen = new CharacterWalkForwardGenerator(options.getCodeGenerationDestinationDirectory());
    characterWalkLeftGen = new CharacterWalkLeftGenerator(options.getCodeGenerationDestinationDirectory());
    characterWalkRightGen = new CharacterWalkRightGenerator(options.getCodeGenerationDestinationDirectory());
    characterWalkBackGen = new CharacterWalkBackGenerator(options.getCodeGenerationDestinationDirectory());
    creatablePathGen = new CreatablePathGenerator(options.getCodeGenerationDestinationDirectory());
    displayableCharacterGen = new DisplayableCharacterGenerator(options.getCodeGenerationDestinationDirectory());
    pathDataGen = new PathDataGenerator(options.getCodeGenerationDestinationDirectory());
    simSECharacterGen = new SimSECharacterGenerator(options.getCodeGenerationDestinationDirectory());
    simSESpriteGen = new SimSESpriteGenerator(options.getCodeGenerationDestinationDirectory());
    spriteAnimGen = new SpriteAnimationGenerator(options.getCodeGenerationDestinationDirectory());
  }

  public void setAllowHireFire(boolean b) {
    allowHireFire = false;
  }

  /*
   * causes all of this component's sub-components to generate code
   */
  public void generate() {
    File codeGenDir = options.getCodeGenerationDestinationDirectory();
    if ((codeGenDir != null) && 
        ((!codeGenDir.exists()) || (!codeGenDir.isDirectory()))) {
      JOptionPane.showMessageDialog(null, ("Cannot find code generation" +
      		" destination directory " + codeGenDir.getAbsolutePath()), 
      		"File Not Found Error", JOptionPane.ERROR_MESSAGE);
    } else {
	    // generate directory structure:
	    File simse = new File(options.getCodeGenerationDestinationDirectory(), 
	        "simse");
	    // if directory already exists, delete all files in it:
	    if (simse.exists() && simse.isDirectory()) {
	      File[] files = simse.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    simse.mkdir();
	    
	    File lib = new File(options.getCodeGenerationDestinationDirectory(), 
	        "lib");
	    // if directory already exists, delete all files in it:
	    if (lib.exists() && lib.isDirectory()) {
	      File[] files = lib.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    lib.mkdir();
	
	    File adts = new File(simse, "adts");
	    // if directory already exists, delete all files in it:
	    if (adts.exists() && adts.isDirectory()) {
	      File[] files = adts.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    adts.mkdir();
	
	    File objects = new File(adts, "objects");
	    // if directory already exists, delete all files in it:
	    if (objects.exists() && objects.isDirectory()) {
	      File[] files = objects.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    objects.mkdir();
	
	    File actions = new File(adts, "actions");
	    // if directory already exists, delete all files in it:
	    if (actions.exists() && actions.isDirectory()) {
	      File[] files = actions.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    actions.mkdir();
	
	    File state = new File(simse, "state");
	    // if directory already exists, delete all files in it:
	    if (state.exists() && state.isDirectory()) {
	      File[] files = state.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    state.mkdir();
	
	    File logger = new File(state, "logger");
	    // if directory already exists, delete all files in it:
	    if (logger.exists() && logger.isDirectory()) {
	      File[] files = logger.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    logger.mkdir();
	
	    File logic = new File(simse, "logic");
	    // if directory already exists, delete all files in it:
	    if (logic.exists() && logic.isDirectory()) {
	      File[] files = logic.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    logic.mkdir();
	
	    File dialogs = new File(logic, "dialogs");
	    // if directory already exists, delete all files in it:
	    if (dialogs.exists() && dialogs.isDirectory()) {
	      File[] files = dialogs.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    dialogs.mkdir();
	
	    File engine = new File(simse, "engine");
	    // if directory already exists, delete all files in it:
	    if (engine.exists() && engine.isDirectory()) {
	      File[] files = engine.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    engine.mkdir();
	
	    File gui = new File(simse, "gui");
	    // if directory already exists, delete all files in it:
	    if (gui.exists() && gui.isDirectory()) {
	      File[] files = gui.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    gui.mkdir();
	    
	    File guiUtil = new File(gui, "util");
	    // if directory already exists, delete all files in it:
	    if (guiUtil.exists() && guiUtil.isDirectory()) {
	      File[] files = guiUtil.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    guiUtil.mkdir();
	    
	    File expTool = new File(simse, "explanatorytool");
	    // if directory already exists, delete all files in it:
	    if (expTool.exists() && expTool.isDirectory()) {
	      File[] files = expTool.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    expTool.mkdir();
	
	    File util = new File(simse, "util");
	    // if directory already exists, delete all files in it:
	    if (util.exists() && util.isDirectory()) {
	      File[] files = util.listFiles();
	      for (File f : files) {
	        f.delete();
	      }
	    }
	    util.mkdir();
	
	    // generate main SimSE component:
	    File ssFile = new File(options.getCodeGenerationDestinationDirectory(), 
	        ("simse\\SimSE.java"));
	    if (ssFile.exists()) {
	      ssFile.delete(); // delete old version of file
	    }
    	ClassName branch = ClassName.get("simse.explanatorytool", "Branch");
    	ClassName simSEGui = ClassName.get("simse.gui", "SimSEGUI");
    	ClassName arrayList = ClassName.get("java.util", "ArrayList");
    	ClassName multipleTimelinesBrowser = ClassName.get("simse.explanatorytool", "MultipleTimelinesBrowser");
    	ClassName engineClass = ClassName.get("simse.engine", "Engine");
    	ClassName stateClass = ClassName.get("simse.state", "State");
    	ClassName logicClass = ClassName.get("simse.logic", "Logic");
    	ClassName ruleCategories = ClassName.get("simse.util", "RuleCategories");
    	TypeName listOfBranches = ParameterizedTypeName.get(arrayList, branch);
    	TypeName listOfGuis = ParameterizedTypeName.get(arrayList, simSEGui);
    	TypeName stringArray = ArrayTypeName.of(String.class);
    	
    	FieldSpec branches = FieldSpec.builder(listOfBranches, "branches")
    		    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    		    .initializer("new $T()", listOfBranches)
    		    .build();
    	
    	FieldSpec guis = FieldSpec.builder(listOfGuis, "guis")
    		    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    		    .initializer("new $T()", listOfGuis)
    		    .build();
    	
    	FieldSpec timelinesBrowser = FieldSpec.builder(multipleTimelinesBrowser, "timelinesBrowser")
    		    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    		    .initializer("new $T()", multipleTimelinesBrowser)
    		    .build();
    	
    	FieldSpec engineField = FieldSpec.builder(engineClass, "engine")
    		    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
    		    .build();
    	
    	MethodSpec startNewBranch = MethodSpec.methodBuilder("startNewBranch")
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    			.returns(void.class)
    			.addParameter(stateClass, "state")
    			.addParameter(branch, "branch")
    			.beginControlFlow("for (int i = 0; i < $N.size(); i++)", branches)
    			.beginControlFlow("if (branch.getName().equals($N.get(i).getName()))", branches)
    			.addStatement("$T alert = new $T($T.ERROR, $S, $T.OK)", Alert.class, Alert.class,
    					AlertType.class, "Please choose a unique name for your new branch", ButtonType.class)
    			.addStatement("alert.showAndWait()")
    			.addStatement("return")
    		    .endControlFlow()
    		    .endControlFlow()
    		    .addStatement("$T logic = new $T(state)", logicClass, logicClass)
    		    .addStatement("$N = new $T(logic, state)", engineField, engineClass)
    		    .addStatement("$T gui = new $T($N, state, logic, branch, $N)", simSEGui,
    		    		simSEGui, engineField, timelinesBrowser)
    		    .addStatement("state.getClock().setGUI(gui)")
    		    .addStatement("gui.setX(0)")
    		    .addStatement("gui.setY(0)")
    		    .addStatement("gui.setWidth(1180)")
    		    .addStatement("gui.setHeight(720)")
    		    .addStatement("$T.initializeRuleCategories()", ruleCategories)
    		    .addStatement("$N.giveGUI(gui)", engineField)
    		    .addStatement("logic.getTriggerChecker().update(false, gui)")
    		    .addStatement("$N.add(branch)", branches)
    		    .addStatement("$N.add(gui)", guis)
    		    .addStatement("$N.update()", timelinesBrowser)
    			.build();
    	
    	MethodSpec getBranches = MethodSpec.methodBuilder("getBranches")
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    			.returns(listOfBranches)
    			.addStatement("return $N", branches)
    			.build();
    			
    	MethodSpec getNumOpenBranches = MethodSpec.methodBuilder("getNumOpenBranches")
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    			.returns(int.class)
    			.addStatement("$T numOpen = 0", int.class)
    			.beginControlFlow("for ($T b : $N)", branch, branches)
    			.beginControlFlow("if (!b.isClosed())")
    			.addStatement("numOpen++")
    			.endControlFlow()
    			.endControlFlow()
    			.addStatement("return numOpen")
    			.build();
    	
    	MethodSpec getGUIs = MethodSpec.methodBuilder("getGUIs")
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    			.returns(listOfGuis)
    			.addStatement("return $N", guis)
    			.build();
    	
    	MethodSpec main = MethodSpec.methodBuilder("main")
    			.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    			.returns(void.class)
    			.addParameter(stringArray, "args")
    			.addStatement("launch(args)")
    			.build();
    	
    	MethodSpec start = MethodSpec.methodBuilder("start")
    			.addModifiers(Modifier.PUBLIC)
    			.returns(void.class)
    			.addParameter(Stage.class, "arg0")
    			.addException(Exception.class)
    			.addStatement("startNewBranch(new $T(), new $T(null, 0, 0, null, $S))",
    					stateClass, branch, "")
    			.build();
    	
    	TypeSpec simSE = TypeSpec.classBuilder("SimSE")
    			.superclass(Application.class)
    			.addModifiers(Modifier.PUBLIC)
    			.addField(branches)
    			.addField(guis)
    			.addField(timelinesBrowser)
    			.addField(engineField)
    			.addMethod(startNewBranch)
    			.addMethod(getBranches)
    			.addMethod(getNumOpenBranches)
    			.addMethod(getGUIs)
    			.addMethod(main)
    			.addMethod(start)
    			.build();
		
		JavaFile javaFile = JavaFile.builder("simse", simSE)
			    .build();

	    try {
	    	FileWriter writer = new FileWriter(ssFile);
	    	
			javaFile.writeTo(writer);
			writer.close();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, ("Error writing file "
	          + ssFile.getPath() + ": " + e.toString()), "File IO Error",
	          JOptionPane.WARNING_MESSAGE);
	    }
	
	    // generate other components:
	    stateGen.generate();
	    boolean logicGenSuccess = logicGen.generate();
	    engineGen.generate();
	    boolean guiGenSuccess = guiGen.generate();
	    expToolGen.generate();
	    idGen.generate();
	    ruleCateGen.generate();
	    ruleTypeGen.generate();
	    characterIdleBackGen.generate();
	    characterIdleFrontGen.generate();
	    characterIdleLeftGen.generate();
	    characterIdleRightGen.generate();
	    characterWalkForwardGen.generate();
	    characterWalkLeftGen.generate();
	    characterWalkRightGen.generate();
	    characterWalkBackGen.generate();
	    creatablePathGen.generate();
	    displayableCharacterGen.generate();
	    pathDataGen.generate();
	    simSECharacterGen.generate();
	    simSESpriteGen.generate();
	    spriteAnimGen.generate();
	    if (logicGenSuccess && guiGenSuccess) {
	      JOptionPane.showMessageDialog(null, "Simulation generated!",
	          "Generation Successful", JOptionPane.INFORMATION_MESSAGE);
	    }
    }
  }
}