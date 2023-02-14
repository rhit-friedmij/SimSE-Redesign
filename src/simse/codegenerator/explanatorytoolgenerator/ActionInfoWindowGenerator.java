/*
 * This class is responsible for generating all of the code for the 
 * ActionInfoWindow class in the explanatory tool
 */

package simse.codegenerator.explanatorytoolgenerator;

import simse.codegenerator.CodeGeneratorConstants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class ActionInfoWindowGenerator implements CodeGeneratorConstants {
  private File directory; // directory to save generated code into

  public ActionInfoWindowGenerator(File directory) {
    this.directory = directory;
  }
  
  
  public void generate() {
    File actWindowFile = new File(directory,
        ("simse\\explanatorytool\\ActionInfoWindow.java"));
    if (actWindowFile.exists()) {
      actWindowFile.delete(); // delete old version of file
    }	
	  
	  ClassName action = ClassName.get("simse.adts.actions", "Action");
	  ClassName scene = ClassName.get("javafx.scene", "Scene");
	  ClassName tab = ClassName.get("java.fx.scene.control", "Tab");
	  ClassName tabPane = ClassName.get("java.fx.scene.control", "TabPane");
	  ClassName stage = ClassName.get("javafx.stage", "Stage");
	  
	  
    	MethodSpec constructor = MethodSpec.constructorBuilder()
    			.addModifiers(Modifier.PUBLIC)
    			.addParameter(String.class, "actionName")
    			.addParameter(action, "action")
    			.addParameter(int.class, "clockTick")
    			.addStatement("this.setTitle(actionName + \" Info for Clock Tick \" + clockTick)")
    			.addStatement("$T mainPane = new $T()", tabPane, tabPane)
    		    .addStatement("ActionInfoPanel actionPanel = new ActionInfoPanel($T)", action)
    		    .addStatement("RuleInfoPanel rulePanel = new RuleInfoPanel(this, $T)", action)
    		    .addStatement("$T actionTab = new $T(\"Action Info\", actionPanel)", tab, tab)
    		    .addStatement("$T ruleTab = new $T(\"Rule Info\", rulePanel)", tab, tab)
    		    .addStatement("mainPane.getTabs().add(actionTab)")
    		    .addStatement("mainPane.getTabs().add(ruleTab)")
    		    .addStatement("$T newScene = new $T(mainPane, 900, 600)", scene, scene)
    		    .addStatement("this.setScene(newScene)")
    		    .addStatement("show()")
    		    .build();
    	
    	TypeSpec actionInfoWindow = TypeSpec.classBuilder("ActionInfoWindow")
    			.superclass(stage)
    			.addMethod(constructor)
    			.build();
    	
    	JavaFile javaFile = JavaFile.builder("simse.explanatorytool", actionInfoWindow)
    		    .build();

    		try {
				javaFile.writeTo(System.out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  }
 }