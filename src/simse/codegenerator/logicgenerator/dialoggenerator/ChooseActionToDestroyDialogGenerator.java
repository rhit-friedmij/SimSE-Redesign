/*
 * This class is responsible for generating all of the code for the logic's
 * ChooseActionToDestroyDialog component
 */

package simse.codegenerator.logicgenerator.dialoggenerator;

import simse.codegenerator.CodeGeneratorConstants;
import simse.codegenerator.CodeGeneratorUtils;

import simse.modelbuilder.actionbuilder.ActionType;
import simse.modelbuilder.actionbuilder.ActionTypeDestroyer;
import simse.modelbuilder.actionbuilder.ActionTypeParticipant;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantAttributeConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantConstraint;
import simse.modelbuilder.actionbuilder.ActionTypeParticipantDestroyer;
import simse.modelbuilder.actionbuilder.DefinedActionTypes;
import simse.modelbuilder.actionbuilder.UserActionTypeDestroyer;
import simse.modelbuilder.objectbuilder.AttributeTypes;
import simse.modelbuilder.objectbuilder.SimSEObjectType;
import simse.modelbuilder.objectbuilder.SimSEObjectTypeTypes;
import simse.modelbuilder.rulebuilder.Rule;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Vector;

import javax.lang.model.element.Modifier;
import javax.swing.JOptionPane;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ChooseActionToDestroyDialogGenerator implements
    CodeGeneratorConstants {
  private File directory; // directory to generate into
  private File catddFile; // file to generate
  private DefinedActionTypes actTypes; // holds all of the defined action types

  public ChooseActionToDestroyDialogGenerator(DefinedActionTypes actTypes, 
  		File directory) {
    this.directory = directory;
    this.actTypes = actTypes;
  }

  public void generate() {
	  ClassName eventHandler = ClassName.get("javafx.event", "EventHandler");
	  ClassName mouseEvent = ClassName.get("javafx.scene.input", "MouseEvent");
	  ClassName vector = ClassName.get("java.util", "Vector");
	  ClassName stateClass = ClassName.get("simse.state", "State");
	  ClassName ruleExecClass = ClassName.get("simse.logic", "RuleExecutor");
	  ClassName employeeClass = ClassName.get("simse.adts.objects", "Employee");
	  ClassName actionClass = ClassName.get("simse.adts.actions", "Action");
	  ClassName stageClass = ClassName.get("javafx.stage", "Stage");
	  ClassName checkboxClass = ClassName.get("javafx.scene.control", "CheckBox");
	  ClassName buttonClass = ClassName.get("javafx.scene.control", "Button");
	  ClassName dialogClass = ClassName.get("javafx.scene.control", "Dialog");
	  ClassName vBoxClass = ClassName.get("javafx.scene.layout", "VBox");
	  ClassName labelClass = ClassName.get("javafx.scene.control", "Label");
	  ClassName paneClass = ClassName.get("javafx.scene.layout", "Pane");
	  ClassName gridPaneClass = ClassName.get("javafx.scene.layout", "GridPane");
	  ClassName point2DClass = ClassName.get("javafx.geometry", "Point2D");
	  ClassName alertClass = ClassName.get("javafx.scene.control", "Alert");
	  ClassName alertTypeClass = ClassName.get("javafx.scene.control.Alert", "AlertType");
	  TypeName mouseHandler = ParameterizedTypeName.get(eventHandler, mouseEvent);
	  TypeName actionWildcard = WildcardTypeName.subtypeOf(actionClass);
	  TypeName actionsType = ParameterizedTypeName.get(vector, actionWildcard);
	  TypeName checkboxType = ParameterizedTypeName.get(vector, checkboxClass);
	  
	  
      // make a Vector of all the action types with user destroyers:
      Vector<ActionType> userDestActs = new Vector<ActionType>();
      Vector<ActionType> allActs = actTypes.getAllActionTypes();
      for (int j = 0; j < allActs.size(); j++) {
        ActionType userAct = allActs.elementAt(j);
        Vector<ActionTypeDestroyer> allDests = userAct.getAllDestroyers();
        for (int k = 0; k < allDests.size(); k++) {
          ActionTypeDestroyer tempDest = allDests.elementAt(k);
          if (tempDest instanceof UserActionTypeDestroyer) {
            userDestActs.add(userAct);
            break;
          }
        }
      }
	  
	  MethodSpec destroyConstructor = MethodSpec.constructorBuilder()
			  .addModifiers(Modifier.PUBLIC)
			  .addParameter(stageClass, "parent")
			  .addParameter(actionsType, "acts")
			  .addParameter(stateClass, "s")
			  .addParameter(employeeClass, "e")
			  .addParameter(ruleExecClass, "r")
			  .addParameter(String.class, "mText")
			  .addStatement("$N = acts", "actions")
			  .addStatement("$N = s", "state")
			  .addStatement("$N = r", "ruleExec")
			  .addStatement("$N = parent", "gui")
			  .addStatement("$N = e", "emp")
			  .addStatement("$N = mText", "menuText")
			  .addStatement("$N = new $T()", "checkBoxes", checkboxType)
			  .addStatement("setTitle($S)", "Stop Action(s)")
			  .addStatement("$T mainPane = new $T()", vBoxClass, vBoxClass)
			  .addStatement("$T topPane = new $T()", paneClass, paneClass)
			  .addStatement("$T actionName = new $T()", String.class, String.class)
			  .addStatement("$T tempAct = $N.elementAt(0)", actionClass, "actions")
			  .addCode(generateNames(userDestActs))
			  .addStatement("topPane.getChildren().add(new $T($S + actionName + $S))",
					  labelClass, "Choose which ", " Action to stop:")
			  .addStatement("$T middlePane = new $T()", gridPaneClass, gridPaneClass)
			  .addCode(generateActionConstructor(userDestActs))
			  .addStatement("$T bottomPane = new $T()", paneClass, paneClass)
			  .addStatement("$N = new $T($S)", "okButton", buttonClass, "OK")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "okButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "okButton")
			  .addStatement("$N = new $T($S)", "cancelButton", buttonClass, "Cancel")
			  .addStatement("$N.addEventHandler($T.MOUSE_CLICKED, this)", "cancelButton", mouseEvent)
			  .addStatement("bottomPane.getChildren().add($N)", "cancelButton")
			  .addStatement("mainPane.getChildren().addAll(topPane, middlePane, bottomPane)")
			  .addStatement("$T ownerLoc = new $T(parent.getX(), parent.getY())", point2DClass, point2DClass)
			  .addStatement("$T thisLoc = new $T((ownerLoc.getX() + (parent.getWidth() / 2) - (this.getWidth() / 2)),"
			  		+ "(ownerLoc.getY() + (parent.getHeight() / 2) - (this.getHeight() / 2)))", point2DClass, point2DClass)
			  .addStatement("this.setX(thisLoc.getX())")
			  .addStatement("this.setY(thisLoc.getY())")
			  .addStatement("show()")
			  .build();
	  
	  MethodSpec handle = MethodSpec.methodBuilder("handle")
			  .addModifiers(Modifier.PUBLIC)
			  .returns(void.class)
			  .addParameter(mouseEvent, "evt")
			  .addAnnotation(Override.class)
			  .addStatement("$T source = evt.getSource()", Object.class)
			  .beginControlFlow("if (source == cancelButton)")
			  .addStatement("hide()")
			  .nextControlFlow("else if (source == $N)", "okButton")
			  .addStatement("$T numChecked = 0", int.class)
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "checkBoxes")
			  .addStatement("$T tempCBox = $N.elementAt(i)", checkboxClass, "checkBoxes")
			  .beginControlFlow("if (tempCBox.isSelected())")
			  .addStatement("numChecked++")
			  .endControlFlow()
			  .endControlFlow()
			  .beginControlFlow("if (numChecked == 0)")
			  .addStatement("$T alert = new $T($T.WARNING, $S)", alertClass, alertClass,
					  alertTypeClass, "You must choose at least one action")
			  .addStatement("alert.setTitle($S)", "Invalid Input")
			  .addStatement("alert.show()")
			  .nextControlFlow("else")
			  .beginControlFlow("for (int i = 0; i < $N.size(); i++)", "checkBoxes")
			  .addStatement("$T cBox = $N.elementAt(i)", checkboxClass, "checkBoxes")
			  .beginControlFlow("if (cBox.isSelected())")
			  .addStatement("$T tempAct = $N.elementAt(i)", actionClass, "actions")
			  .addCode(generateActionHandle(userDestActs))
			  .endControlFlow()
			  .endControlFlow()
			  .addStatement("hide()")
			  .endControlFlow()
			  .endControlFlow()
			  .build();
	  
	  TypeSpec destroyDialog = TypeSpec.classBuilder("ChooseActionToDestroyDialog")
			  .addModifiers(Modifier.PUBLIC)
  			.superclass(dialogClass)
  			.addSuperinterface(mouseHandler)
  			.addField(actionsType, "actions", Modifier.PRIVATE)
  			.addField(stateClass, "state", Modifier.PRIVATE)
  			.addField(ruleExecClass, "ruleExec", Modifier.PRIVATE)
  			.addField(employeeClass, "emp", Modifier.PRIVATE)
  			.addField(String.class, "menuText", Modifier.PRIVATE)
  			.addField(stageClass, "gui", Modifier.PRIVATE)
  			.addField(checkboxType, "checkBoxes", Modifier.PRIVATE)
  			.addField(buttonClass, "okButton", Modifier.PRIVATE)
  			.addField(buttonClass, "cancelButton", Modifier.PRIVATE)
  			.addMethod(destroyConstructor)
  			.addMethod(handle)
  			.build();
	  
	  JavaFile javaFile = JavaFile.builder("", destroyDialog)
			  .build();
	  
    try {
      catddFile = new File(directory,
          ("simse\\logic\\dialogs\\ChooseActionToDestroyDialog.java"));
      if (catddFile.exists()) {
        catddFile.delete(); // delete old version of file
      }

	  FileWriter writer = new FileWriter(catddFile);
      
	  String toAppend = "/* File generated by: simse.codegenerator.logicgenerator.dialoggenerator.ChooseActionToDestroyDialogGenerator */\n"
	  		+ "package simse.logic.dialogs;\n"
	  		+ "\n"
	  		+ "import simse.adts.actions.*;\n"
	  		+ "import simse.adts.objects.*;\n"
	  		+ "import javafx.scene.layout.BorderPane;\n";
	  
      writer.write(toAppend + javaFile.toString());
      writer.close();

    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, ("Error writing file "
          + catddFile.getPath() + ": " + e.toString()), "File IO Error",
          JOptionPane.WARNING_MESSAGE);
    }
  }
  
  private String generateNames(Vector<ActionType> userDestActs) {
	  String names = "";
      // go through each action type and generate code for it:
      for (int j = 0; j < userDestActs.size(); j++) {
        ActionType act = userDestActs.elementAt(j);
        if (j > 0) { // not on first element
          names += "else ";
        }
        names += "if(tempAct instanceof "
            + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
            "Action)\n{\n";
        names += "actionName = \"" + 
        		CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + "\";\n}\n";
      }
      
      return names;
  }
  
  private String generateActionConstructor(Vector<ActionType> userDestActs) {
	  String actions = "";
	  for (int j = 0; j < userDestActs.size(); j++) {
	        ActionType act = userDestActs.elementAt(j);
	        if (j > 0) { // not on first element
	          actions += "else ";
	        }
	        actions += "if(tempAct instanceof "
	            + CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
	            "Action)\n{\n";
	        actions += "for(int i=0; i<actions.size(); i++)\n{\n";
	        actions += CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
	        		"Action act = (" + 
	        		CodeGeneratorUtils.getUpperCaseLeading(act.getName()) + 
	        		"Action)actions.elementAt(i);\n";
	        actions += "if(act.getAllParticipants().contains(emp))\n{\n";
	        actions += "StringBuffer label = new StringBuffer();\n";
	        // go through all participants:
	        Vector<ActionTypeParticipant> parts = act.getAllParticipants();
	        for (int k = 0; k < parts.size(); k++) {
	          if (k > 0) { // not on first element
	            actions += "label.append(\"; \");\n";
	          }
	          ActionTypeParticipant tempPart = parts.elementAt(k);
	          actions += "label.append(\"" + tempPart.getName() + "(s); \");\n";
	          actions += "Vector<" + SimSEObjectTypeTypes.getText(tempPart
	              .getSimSEObjectTypeType()) + "> all" + tempPart.getName() + 
	              "s = act.getAll" + tempPart.getName() + "s();\n";
	          actions += "for(int j=0; j<all" + tempPart.getName()
	              + "s.size(); j++)\n{\n";
	          actions += "if(j > 0)\n{\n";
	          actions += "label.append(\", \");\n}\n";
	          actions += SimSEObjectTypeTypes.getText(tempPart
	              .getSimSEObjectTypeType())
	              + " a = all" + tempPart.getName() + "s.elementAt(j);\n";
	          // go through all allowable SimSEObjectTypes for this participant:
	          Vector<SimSEObjectType> ssObjTypes = 
	          	tempPart.getAllSimSEObjectTypes();
	          for (int m = 0; m < ssObjTypes.size(); m++) {
	            SimSEObjectType tempType = ssObjTypes.elementAt(m);
	            if (m > 0) { // not on first element
	              actions += "else ";
	            }
	            actions += "if(a instanceof "
	                + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	                ")\n{\n";
	            actions += "label.append(\""
	                + CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	                "(\" + ((" + 
	                CodeGeneratorUtils.getUpperCaseLeading(tempType.getName()) + 
	                ")a).get" + 
	                CodeGeneratorUtils.getUpperCaseLeading(
	                		tempType.getKey().getName()) + "() + \")\");\n}\n";
	          }
	          actions += "}\n";
	        }
	        actions += "BorderPane tempPane = new BorderPane();\n";
	        actions += "CheckBox tempCheckBox = new CheckBox(label.toString());\n";
	        actions += "tempPane.setLeft(tempCheckBox);\n";
	        actions += "checkBoxes.add(tempCheckBox);\n";
	        actions += "middlePane.getChildren().add(tempPane);\n}\n}\n}\n";
      }
	  
	  return actions;
  }
  
  private String generateActionHandle(Vector<ActionType> userDestActs) {
	  String actions = "";
      // go through all action types w/ user destroyers:
      for (int i = 0; i < userDestActs.size(); i++) {
        ActionType tempAct = userDestActs.elementAt(i);
        if (i > 0) { // not on first element
          actions += "else ";
        }
        actions += "if(tempAct instanceof "
            + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
            "Action)\n{\n";
        actions += CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) +
        		"Action " + tempAct.getName().toLowerCase() + "Act = (" + 
        		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
        		"Action)tempAct;\n";
        // go through all participants:
        Vector<ActionTypeParticipant> parts = tempAct.getAllParticipants();
        for (int j = 0; j < parts.size(); j++) {
          ActionTypeParticipant part = parts.elementAt(j);
          if (part.getSimSEObjectTypeType() == SimSEObjectTypeTypes.EMPLOYEE) {
            actions += tempAct.getName().toLowerCase() + "Act.remove"
                + part.getName() + "(emp);\n";
          }
        }

        // go through all of the action's user destroyers:
        Vector<ActionTypeDestroyer> dests = tempAct.getAllDestroyers();
        boolean putElse = false;
        for (int j = 0; j < dests.size(); j++) {
          ActionTypeDestroyer tempDest = dests.elementAt(j);
          if ((tempDest instanceof UserActionTypeDestroyer)
              && (tempDest.getDestroyerText() != null)
              && (tempDest.getDestroyerText().length() > 0)) {
            if (putElse) {
              actions += "else ";
            } else {
              putElse = true;
            }
            actions += "if(menuText.equals(\""
                + ((UserActionTypeDestroyer) tempDest).getMenuText() + "\"))\n{\n";
            actions += "emp.setOverheadText(\"" + tempDest.getDestroyerText()
                + "\");\n";

            // execute all destroyer rules that have executeOnJoins == true:
            Vector<Rule> destRules = tempAct.getAllDestroyerRules();
            for (int k = 0; k < destRules.size(); k++) {
              Rule dRule = destRules.elementAt(k);
              if (dRule.getExecuteOnJoins() == true) {
                actions += "ruleExec.update(gui, RuleExecutor.UPDATE_ONE, \""
                    + dRule.getName() + "\", "
                    + tempAct.getName().toLowerCase() + "Act);\n";
              }
            }
            actions += "}\n";
          }
        }

        // go through all parts again:
        for (int j = 0; j < parts.size(); j++) {
          if (j > 0) { // not on first element
            actions += "else ";
          }
          ActionTypeParticipant part = parts.elementAt(j);
          actions += "if(" + tempAct.getName().toLowerCase() + "Act.getAll"
              + part.getName() + "s().size() < ";
          if (part.getQuantity().isMinValBoundless()) {
            actions += "0";
          } else {
            actions += part.getQuantity().getMinVal().toString();
          }
          actions += ")\n{\n";
          actions += "Vector<SSObject> c = " + tempAct.getName().toLowerCase()
              + "Act.getAllParticipants();\n";
          actions += "for(int j=0; j<c.size(); j++)\n{\n";
          actions += "SSObject d = c.elementAt(j);\n";
          actions += "if(d instanceof Employee)\n{\n";

          // go through all user destroyers again:
          boolean putElse2 = false;
          for (int k = 0; k < dests.size(); k++) {
            ActionTypeDestroyer tempDest = dests.elementAt(k);
            if ((tempDest instanceof UserActionTypeDestroyer)
                && (tempDest.getDestroyerText() != null)
                && (tempDest.getDestroyerText().length() > 0)) {
              if (putElse2) {
                actions += "else \n";
              } else {
                putElse2 = true;
              }
              actions += "if(menuText.equals(\""
                      + ((UserActionTypeDestroyer) tempDest).getMenuText()
                      + "\"))\n{\n";
              actions += "((Employee)d).setOverheadText(\""
                  + tempDest.getDestroyerText() + "\");\n}\n";
            }
          }
          
          actions += "}\n";
          actions += "else if(d instanceof Customer)\n{\n";

          // go through all user destroyers again:
          boolean putElse3 = false;
          for (int k = 0; k < dests.size(); k++) {
            ActionTypeDestroyer tempDest = dests.elementAt(k);
            if ((tempDest instanceof UserActionTypeDestroyer)
                && (tempDest.getDestroyerText() != null)
                && (tempDest.getDestroyerText().length() > 0)) {
              if (putElse3) {
                actions += "else \n";
              } else {
                putElse3 = true;
              }
              actions += "if(menuText.equals(\""
                      + ((UserActionTypeDestroyer) tempDest).getMenuText()
                      + "\"))\n{\n";
              actions += "((Customer)d).setOverheadText(\""
                  + tempDest.getDestroyerText() + "\");\n}\n";
            }
          }
          
          actions += "}\n}\n";

          // execute all destroyer rules:
          Vector<Rule> destRules = tempAct.getAllDestroyerRules();
          for (int k = 0; k < destRules.size(); k++) {
            Rule dRule = destRules.elementAt(k);
            actions += "ruleExec.update(gui, RuleExecutor.UPDATE_ONE, \""
                + dRule.getName() + "\", " + tempAct.getName().toLowerCase()
                + "Act);\n";
          }
          actions += "state.getActionStateRepository().get"
              + CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName())
              + "ActionStateRepository().remove("
              + tempAct.getName().toLowerCase() + "Act);\n";

          // game-ending:
          if (tempAct.hasGameEndingDestroyer()) {
            // find all game-ending user destroyers:
            Vector<ActionTypeDestroyer> allDests = tempAct.getAllDestroyers();
            boolean putElse711 = false;
            for (int k = 0; k < allDests.size(); k++) {
              ActionTypeDestroyer tempDest = allDests.elementAt(k);
              if ((tempDest instanceof UserActionTypeDestroyer)
                  && (tempDest.isGameEndingDestroyer())) {
                if (putElse711) {
                  actions += "else ";
                } else {
                  putElse711 = true;
                }
                actions += "if(menuText.equals(\""
                    + ((UserActionTypeDestroyer) tempDest).getMenuText()
                    + "\"))\n{\n";
                actions += "// stop game and give score:\n";
                actions += CodeGeneratorUtils.getUpperCaseLeading(
                		tempAct.getName()) + "Action t111 = (" + 
                		CodeGeneratorUtils.getUpperCaseLeading(tempAct.getName()) + 
                		"Action)" + tempAct.getName().toLowerCase() + "Act;\n";
                // find the scoring attribute:
                ActionTypeParticipantDestroyer scoringPartDest = null;
                ActionTypeParticipantConstraint scoringPartConst = null;
                ActionTypeParticipantAttributeConstraint scoringAttConst = null;
                Vector<ActionTypeParticipantDestroyer> partDests = 
                	tempDest.getAllParticipantDestroyers();
                for (int m = 0; m < partDests.size(); m++) {
                  ActionTypeParticipantDestroyer partDest = 
                  	partDests.elementAt(m);
                  Vector<ActionTypeParticipantConstraint> partConsts = 
                  	partDest.getAllConstraints();
                  for (int n = 0; n < partConsts.size(); n++) {
                    ActionTypeParticipantConstraint partConst = 
                    	partConsts.elementAt(n);
                    ActionTypeParticipantAttributeConstraint[] attConsts = 
                    	partConst.getAllAttributeConstraints();
                    for (int p = 0; p < attConsts.length; p++) {
                      if (attConsts[p].isScoringAttribute()) {
                        scoringAttConst = attConsts[p];
                        scoringPartConst = partConst;
                        scoringPartDest = partDest;
                        break;
                      }
                    }
                  }
                }
                if ((scoringAttConst != null) && (scoringPartConst != null)
                    && (scoringPartDest != null)) {
                  actions += "if(t111.getAll"
                      + scoringPartDest.getParticipant().getName()
                      + "s().size() > 0)\n{\n";
                  actions += CodeGeneratorUtils.getUpperCaseLeading(
                  		scoringPartConst.getSimSEObjectType().getName())
                      + " t = ("
                      + CodeGeneratorUtils.getUpperCaseLeading(scoringPartConst
                          .getSimSEObjectType().getName())
                      + ")(t111.getAll"
                      + scoringPartDest.getParticipant().getName()
                      + "s().elementAt(0));\n";
                  actions += "if(t != null)\n{\n";
                  if (scoringAttConst.getAttribute().getType() == 
                  	AttributeTypes.INTEGER) {
                    actions += "int";
                  } else if (scoringAttConst.getAttribute().getType() == 
                  	AttributeTypes.DOUBLE) {
                    actions += "double";
                  } else if (scoringAttConst.getAttribute().getType() == 
                  	AttributeTypes.STRING) {
                    actions += "String";
                  } else if (scoringAttConst.getAttribute().getType() == 
                  	AttributeTypes.BOOLEAN) {
                    actions += "boolean";
                  }
                  actions += " v = t.get"
                      + scoringAttConst.getAttribute().getName() + "();\n";
                  actions += "state.getClock().stop();\n";
                  actions += "state.setScore(v);\n";
                  actions += "((SimSEGUI)gui).update();\n";
                  actions += "JOptionPane.showMessageDialog(null, (\"Your score is \" + v),"
                  		+ " \"Game over!\", JOptionPane.INFORMATION_MESSAGE);\n}\n}\n";
                }
                actions += "}\n";
              }
            }
          }
          actions += "}\n";
        }
        actions += "}\n";
      }
      
      return actions;
  }
  
}